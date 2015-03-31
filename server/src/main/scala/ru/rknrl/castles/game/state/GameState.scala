//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.Assertion
import ru.rknrl.castles.account.state.IJ
import ru.rknrl.castles.game.{GameMap, GameConfig}
import ru.rknrl.castles.game.points.Point
import ru.rknrl.castles.game.state.GameArea.PlayerIdToSlotsPositions
import ru.rknrl.castles.game.state.Moving._
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
  def next = BuildingId(nextInt)
}

class UnitIdIterator extends IdIterator {
  def next = UnitId(nextInt)
}

object GameState {

  def getPlayerBuildings(players: List[Player], playersSlotsPositions: PlayerIdToSlotsPositions, buildingIdIterator: BuildingIdIterator, config: GameConfig) =
    for (player ← players;
         (slotId, slot) ← player.slots.slots
         if slot.hasBuildingPrototype)
      yield {
        val ij = playersSlotsPositions(player.id.id)(slotId)
        val xy = ij.centerXY

        val prototype = slot.getBuildingPrototype
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
        SlotsPosDTO.newBuilder
          .setPlayerId(player.id.dto)
          .setPos(pos.centerXY.dto)
          .setOrientation(orientations(id))
          .build
      }

  def init(time: Long, players: List[Player], big: Boolean, isTutor: Boolean, config: GameConfig, gameMap: GameMap) = {
    if (big)
      Assertion.check(players.size == 4)
    else
      Assertion.check(players.size == 2)

    val gameArea = GameArea(big)

    val slotsPositions = gameArea.slotsPositions

    val playersSlotsPositions = gameArea.getPlayersSlotPositions(slotsPositions)

    val buildingIdIterator = new BuildingIdIterator

    val playersBuildings = getPlayerBuildings(players, playersSlotsPositions, buildingIdIterator, config)

    val buildings = playersBuildings ++ gameMap.buildings(gameArea, buildingIdIterator, config)

    val slotsPos = slotsPosDto(players, slotsPositions, gameArea.playerIdToOrientation)

    val playerStates = for (player ← players) yield player.id → new PlayerState(player.stat, 0)

    new GameState(
      time,
      gameArea.width,
      gameArea.height,
      players.map(p ⇒ p.id → p).toMap,
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
      gameArea.assistancePositions,
      config
    )
  }
}

class GameState(val time: Long,
                val width: Int,
                val height: Int,
                val players: Map[PlayerId, Player],
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
                val assistancePositions: Map[PlayerId, Point],
                val config: GameConfig) {

  def update(newTime: Long,
             moveActions: Map[PlayerId, MoveDTO],
             newFireballCasts: Map[PlayerId, PointDTO],
             newStrengtheningCasts: Map[PlayerId, BuildingId],
             newVolcanoCasts: Map[PlayerId, PointDTO],
             newTornadoCasts: Map[PlayerId, CastTorandoDTO],
             newAssistanceCasts: Map[PlayerId, BuildingId]) = {

    val deltaTime = newTime - time

    val fireballCasts = gameItems.checkCasts(newFireballCasts, ItemType.FIREBALL, config, time)
    val strengtheningCasts = gameItems.checkCasts(newStrengtheningCasts, ItemType.STRENGTHENING, config, time)
    val tornadoCasts = gameItems.checkCasts(newTornadoCasts, ItemType.TORNADO, config, time)
    val volcanoCasts = gameItems.checkCasts(newVolcanoCasts, ItemType.VOLCANO, config, time)
    val assistanceCasts = gameItems.checkCasts(newAssistanceCasts, ItemType.ASSISTANCE, config, time)

    val exitUnits = `moveActions→exitUnits`(moveActions, buildings, config)
    val assistanceUnits = Assistance.`casts→units`(assistanceCasts, buildings, config, playerStates, unitIdIterator, assistancePositions, time)
    val exitedUnits = `exitUnit→units`(exitUnits, buildings, config, unitIdIterator, playerStates, time)
    val createdUnits = assistanceUnits ++ exitedUnits
    val addUnitMessages = `units→addMessages`(createdUnits, time)
    val enterUnits = `units→enterUnit`(units.units, time)
    val killUnitMessages = units.`killed→killMessages`

    val createdFireballs = `casts→fireballs`(fireballCasts, config, time)
    val createdVolcanoes = `casts→volcanoes`(volcanoCasts, time, config, playerStates)
    val createdTornadoes = `casts→tornadoes`(tornadoCasts, time, config, playerStates)

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

    val finishedBullets = bullets.getFinished(time)

    val newUnits = units
      .add(createdUnits)
      .applyFireballs(finishedFireballs, playerStates, config, time)
      .applyVolcanoes(volcanoes.list, playerStates, config, time)
      .applyTornadoes(tornadoes.list, playerStates, config, time)
      .applyBullets(finishedBullets, playerStates, config, time)
      .applyEnterUnits(enterUnits)
      .applyKillMessages(killUnitMessages)

    val updateUnitMessages = getUpdateMessages(units.units, newUnits.units, time)

    val createdBullets = createBullets(buildings, newUnits, time, config)
    val addBulletsMessages = `bullets→addMessage`(createdBullets, time)

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

    val updateBuildingMessages = Buildings.getUpdateMessages(buildings.map, newBuildings.map)

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

    val newPlayerStates = playerStates.updateChurchesProportion(buildings.map)

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
      assistancePositions,
      config
    )

    val messages: Iterable[Msg] = addUnitMessages ++ updateUnitMessages ++ killUnitMessages ++
      updateBuildingMessages ++
      addFireballMessages ++ addVolcanoMessages ++ addTornadoMessages ++ addBulletsMessages

    val personalMessages = updateItemsStatesMessages

    (newGameState, messages, personalMessages)
  }

  def isPlayerLose(playerId: PlayerId) =
    !buildings.map.exists { case (buildingId, building) ⇒ building.owner == Some(playerId) }

  def dtoBuilder(id: PlayerId) =
    GameStateDTO.newBuilder
      .setWidth(width)
      .setHeight(height)
      .setSelfId(id.dto)
      .addAllSlots(slotsPos.asJava)
      .addAllBuildings(buildings.dto.asJava)
      .addAllUnits(units.dto(time).asJava)
      .addAllVolcanoes(volcanoes.dto(time).asJava)
      .addAllTornadoes(tornadoes.dto(time).asJava)
      .addAllBullets(bullets.dto(time).asJava)
      .setItemsState(gameItems.dto(id, time, config))
}
