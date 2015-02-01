package ru.rknrl.castles.game.state

import ru.rknrl.castles.account.state.IJ
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.Moving._
import ru.rknrl.castles.game.state.area.GameArea.PlayerIdToSlotsPositions
import ru.rknrl.castles.game.state.area.{GameArea, MapGenerator}
import ru.rknrl.castles.game.state.buildings.{Building, BuildingId, Buildings}
import ru.rknrl.castles.game.state.bullets.Bullets._
import ru.rknrl.castles.game.state.fireballs.Fireballs._
import ru.rknrl.castles.game.state.players._
import ru.rknrl.castles.game.state.tornadoes.Tornadoes._
import ru.rknrl.castles.game.state.units.GameUnits.{getUpdateMessages, _}
import ru.rknrl.castles.game.state.units.{GameUnits, UnitId}
import ru.rknrl.castles.game.state.volcanoes.Volcanoes._
import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto.CommonDTO.ItemType
import ru.rknrl.dto.GameDTO._
import ru.rknrl.utils.IdIterator

import scala.collection.JavaConverters._

class BuildingIdIterator extends IdIterator {
  def next = new BuildingId(nextInt)
}

class UnitIdIterator extends IdIterator {
  def next = new UnitId(nextInt)
}

object GameState {

  def getPlayerBuildings(players: List[Player], playersSlotsPositions: PlayerIdToSlotsPositions, buildingIdIterator: BuildingIdIterator, config: GameConfig) =
    for (player ← players;
         (slotId, slot) ← player.slots.slots
         if slot.buildingPrototype.isDefined)
    yield {
      val ij = playersSlotsPositions(player.id.id)(slotId)
      val xy = ij.toXY

      val prototype = slot.buildingPrototype.get
      new Building(
        buildingIdIterator.next,
        prototype,
        xy,
        population = config.getStartPopulation(prototype),
        owner = Some(player.id),
        strengthened = false,
        strengtheningStartTime = 0,
        lastShootTime = 0)
    }

  def slotsPosDto(players: List[Player], positions: Map[Int, IJ], orientations: Map[Int, SlotsOrientation]) =
    for (player ← players)
    yield {
      val id = player.id.id
      val pos = positions(id)
      SlotsPosDTO.newBuilder()
        .setPlayerId(player.id.dto)
        .setPos(pos.toXY.dto)
        .setOrientation(orientations(id))
        .build()
    }

  def mirrorBuildings2(gameArea: GameArea, topRandomBuildings: Iterable[Building], buildingIdIterator: BuildingIdIterator) =
    for (b ← topRandomBuildings)
    yield {
      val pos = gameArea.mirrorH(gameArea.mirrorV(b.pos))
      new Building(buildingIdIterator.next, b.prototype, pos, b.population, b.owner, b.strengthened, b.strengtheningStartTime, b.lastShootTime)
    }

  def mirrorBuildings4(gameArea: GameArea, topRandomBuildings: Iterable[Building], buildingIdIterator: BuildingIdIterator) =
    for (b ← topRandomBuildings;
         part ← List(1, 2, 3))
    yield {
      val pos = part match {
        case 1 ⇒ gameArea.mirrorH(b.pos)
        case 2 ⇒ gameArea.mirrorV(b.pos)
        case 3 ⇒ gameArea.mirrorH(gameArea.mirrorV(b.pos))
      }

      new Building(buildingIdIterator.next, b.prototype, pos, b.population, b.owner, b.strengthened, b.strengtheningStartTime, b.lastShootTime)
    }

  def init(time: Long, players: List[Player], big: Boolean, config: GameConfig) = {
    if (big)
      assert(players.size == 4)
    else
      assert(players.size == 2)

    val gameArea = GameArea(big)

    val slotsPositions = gameArea.randomSlotsPositions

    val playersSlotsPositions = gameArea.getPlayersSlotPositions(slotsPositions)

    val buildingIdIterator = new BuildingIdIterator

    val playersBuildings = getPlayerBuildings(players, playersSlotsPositions, buildingIdIterator, config)

    val slotsIJs = GameArea.toIJs(playersSlotsPositions)

    val randomBuildings = if (big)
      MapGenerator.getRandomBuildings(slotsIJs, Math.floor(gameArea.h / 2).toInt, Math.floor(gameArea.v / 2).toInt, buildingIdIterator, config)
    else
      MapGenerator.getRandomBuildings(slotsIJs, gameArea.h, Math.floor(gameArea.v / 2).toInt, buildingIdIterator, config)

    val mirrorRandomBuildings = if (big)
      mirrorBuildings4(gameArea, randomBuildings, buildingIdIterator)
    else
      mirrorBuildings2(gameArea, randomBuildings, buildingIdIterator)

    val buildings = playersBuildings ++ randomBuildings ++ mirrorRandomBuildings

    val slotsPos = slotsPosDto(players, slotsPositions, gameArea.playerIdToOrientation)

    val playerStates = for (player ← players) yield player.id → new PlayerState(player.skills.stat, 0)

    new GameState(
      time,
      gameArea.width,
      gameArea.height,
      new Players(players.map(p ⇒ p.id → p).toMap),
      new Buildings(buildings.map(b ⇒ b.id → b).toMap),
      new GameUnits(List.empty),
      new Fireballs(List.empty),
      new Volcanoes(List.empty),
      new Tornadoes(List.empty),
      new Bullets(List.empty),
      new GameItems(players.map(p ⇒ p.id → GameItems.init(p.id, p.items)).toMap),
      new UnitIdIterator,
      slotsPos,
      new PlayerStates(playerStates.toMap),
      config
    )
  }
}

class GameState(val time: Long,
                val width: Int,
                val height: Int,
                val players: Players,
                val buildings: Buildings,
                val units: GameUnits,
                val fireballs: Fireballs,
                val volcanoes: Volcanoes,
                val tornadoes: Tornadoes,
                val bullets: Bullets,
                val gameItems: GameItems,
                val unitIdIterator: UnitIdIterator,
                val slotsPos: Iterable[SlotsPosDTO],
                val playerStates: PlayerStates,
                val config: GameConfig) {

  def update(newTime: Long,
             moveActions: Map[PlayerId, MoveDTO],
             fireballCasts: Map[PlayerId, PointDTO],
             strengtheningCasts: Map[PlayerId, BuildingId],
             volcanoCasts: Map[PlayerId, PointDTO],
             tornadoCasts: Map[PlayerId, CastTorandoDTO],
             assistanceCasts: Map[PlayerId, BuildingId]) = {

    val deltaTime = newTime - time

    gameItems.assertCasts(fireballCasts, ItemType.FIREBALL, config, time)
    gameItems.assertCasts(strengtheningCasts, ItemType.STRENGTHENING, config, time)
    gameItems.assertCasts(tornadoCasts, ItemType.TORNADO, config, time)
    gameItems.assertCasts(volcanoCasts, ItemType.VOLCANO, config, time)
    gameItems.assertCasts(assistanceCasts, ItemType.ASSISTANCE, config, time)

    val exitUnits = `moveActions→exitUnits`(moveActions, buildings, config)
    val assistanceUnits = Assistance.`casts→units`(assistanceCasts, buildings, config, playerStates, unitIdIterator, time)
    val exitedUnits = `exitUnit→units`(exitUnits, buildings, config, unitIdIterator, playerStates, time)
    val createdUnits = assistanceUnits ++ exitedUnits
    val addUnitMessages = `units→addMessages`(createdUnits, time)
    val enterUnits = `units→enterUnit`(units.units, time)
    val removeUnitMessages = `enterUnit→removeUnitMsg`(enterUnits) ++ units.`killed→removeMessages`

    val createdBullets = List.empty; // createBullets(buildings, units, time, config, playerStates)

    val createdFireballs = `casts→fireballs`(fireballCasts, config, time)
    val createdVolcanoes = `casts→volcanoes`(volcanoCasts, time, config, playerStates)
    val createdTornadoes = `casts→tornadoes`(tornadoCasts, time, config, playerStates)

    val addBulletsMessages = `bullets→addMessage`(createdBullets, time)

    val addFireballMessages = `fireballs→addMessages`(createdFireballs, time)
    val addVolcanoMessages = `volcanoes→addMessages`(createdVolcanoes, time)
    val addTornadoMessages = `tornadoes→addMessages`(createdTornadoes, time)

    val newGameItems = gameItems
      .applyCasts(fireballCasts, ItemType.FIREBALL, time)
      .applyCasts(strengtheningCasts, ItemType.STRENGTHENING, time)
      .applyCasts(tornadoCasts, ItemType.TORNADO, time)
      .applyCasts(volcanoCasts, ItemType.VOLCANO, time)
      .applyCasts(assistanceCasts, ItemType.ASSISTANCE, time)

    val updateItemsStatesMessages = GameItems.getUpdateItemsStatesMessages(gameItems, newGameItems, config, time)

    val finishedFireballs = fireballs.getFinished(time)

    val newBuildings = buildings
      .updatePopulation(deltaTime, config)
      .applyStrengtheningCasts(strengtheningCasts, time)
      .cleanupStrengthening(time, config, playerStates)
      .applyExitUnits(exitUnits, config)
      .applyEnterUnits(enterUnits, config, playerStates)
      .applyShots(time, createdBullets)
      .applyFireballs(finishedFireballs, playerStates, config)
      .applyVolcanoes(volcanoes.list, playerStates, config)
      .applyTornadoes(tornadoes.list, playerStates, config, time)

    val finishedBullets = bullets.getFinished(time)

    val newUnits = units
      .add(createdUnits)
      .applyFireballs(finishedFireballs, playerStates, config, time)
      .applyVolcanoes(volcanoes.list, playerStates, config, time)
      .applyTornadoes(tornadoes.list, playerStates, config, time)
      .applyBullets(finishedBullets, playerStates, config, time)
      .applyRemoveMessages(removeUnitMessages)

    val updateBuildingMessages = Buildings.getUpdateMessages(buildings.map, newBuildings.map)
    val updateUnitMessages = getUpdateMessages(units.units, newUnits.units, time)

    val newFireballs = fireballs
      .add(createdFireballs)
      .cleanup(time)

    val newVolcanoes = volcanoes
      .add(createdVolcanoes)
      .cleanup(time)

    val newTornadoes = tornadoes
      .add(createdTornadoes)
      .cleanup(time)

    val newBullets = bullets
      .add(createdBullets)
      .cleanup(time)

    val newPlayerStates = playerStates.updateChurchesPopulation(buildings.map)

    val newGameState = new GameState(
      newTime,
      width,
      height,
      players,
      newBuildings,
      newUnits,
      newFireballs,
      newVolcanoes,
      newTornadoes,
      newBullets,
      newGameItems,
      unitIdIterator,
      slotsPos,
      newPlayerStates,
      config
    )

    val messages: Iterable[Msg] = addUnitMessages ++ updateUnitMessages ++ removeUnitMessages ++
      updateBuildingMessages ++
      addFireballMessages ++ addVolcanoMessages ++ addTornadoMessages ++ addBulletsMessages

    val personalMessages = updateItemsStatesMessages

    (newGameState, messages, personalMessages)
  }

  def isPlayerLose(playerId: PlayerId) =
    !buildings.map.exists { case (buildingId, building) ⇒ building.owner == Some(playerId)}

  def dtoBuilder(id: PlayerId) =
    GameStateDTO.newBuilder()
      .setWidth(width)
      .setHeight(height)
      .setSelfId(id.dto)
      .addAllPlayers(players.dto.asJava)
      .addAllSlots(slotsPos.asJava)
      .addAllBuildings(buildings.dto.asJava)
      .addAllUnits(units.dto(time).asJava)
      .addAllVolcanoes(volcanoes.dto(time).asJava)
      .addAllTornadoes(tornadoes.dto(time).asJava)
      .addAllBullets(bullets.dto(time).asJava)
      .setItemsState(gameItems.dto(id, time, config))
}