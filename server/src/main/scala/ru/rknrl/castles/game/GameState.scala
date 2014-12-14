package ru.rknrl.castles.game

import ru.rknrl.castles.account.objects.{IJ, StartLocation}
import ru.rknrl.castles.game.objects.Assistance
import ru.rknrl.castles.game.objects.Moving._
import ru.rknrl.castles.game.objects.buildings.{Building, BuildingId, Buildings}
import ru.rknrl.castles.game.objects.bullets.Bullets._
import ru.rknrl.castles.game.objects.fireballs.Fireballs._
import ru.rknrl.castles.game.objects.players._
import ru.rknrl.castles.game.objects.tornadoes.Tornadoes._
import ru.rknrl.castles.game.objects.units.GameUnits
import ru.rknrl.castles.game.objects.units.GameUnits.{getUpdateMessages, _}
import ru.rknrl.castles.game.objects.volcanoes.Volcanoes._
import ru.rknrl.dto.CommonDTO.{ItemType, SlotId}
import ru.rknrl.dto.GameDTO._
import ru.rknrl.utils.{BuildingIdIterator, Point, UnitIdIterator}

import scala.collection.JavaConverters._

class GameArea(big: Boolean) {
  val smallH = 8
  val smallV = 12
  val bigH = 16
  val bigV = 16

  val h = if (big) bigH else smallH
  val v = if (big) bigV else smallV


  val width = h * CellSize.SIZE_VALUE
  val height = v * CellSize.SIZE_VALUE

  val hForRandom = h - 1 - StartLocation.left - StartLocation.right
  val vForRandom = Math.floor((v - 1) / 2).toInt - StartLocation.top - StartLocation.bottom

  def mirror(pos: Point) = new Point(width - pos.x, height - pos.y)

  def mirror(pos: IJ) = new IJ(h - 1 - pos.i, v - 1 - pos.j)

  def randomStartLocationPositions = {
    val i = Math.round(Math.random() * hForRandom).toInt + StartLocation.left // [2,5]

    val j = 0; //Math.round(Math.random() * vForRandom).toInt + StartLocation.bottom // [0,4]

    val pos = new IJ(i, j)

    Map(
      1 → pos,
      2 → mirror(pos)
    )
  }
}

object GameState {

  def getPlayersSlotPositions(startLocationPositions: Map[Int, IJ]) =
    for ((id, pos) ← startLocationPositions;
         slotId ← SlotId.values())
    yield {
      val slotPos = StartLocation.positions(slotId)
      val i = pos.i + slotPos.i
      val j = if (orientations(id) == StartLocationOrientation.TOP) pos.j - slotPos.j else pos.j + slotPos.j
      new IJ(i, j)
    }

  val orientations = Map(
    1 → StartLocationOrientation.TOP,
    2 → StartLocationOrientation.BOTTOM
  )

  def getPlayerBuildings(players: List[Player], startLocationPositions: Map[Int, IJ], buildingIdIterator: BuildingIdIterator, config: GameConfig) =
    for (player ← players;
         (slotId, slot) ← player.startLocation.slots
         if slot.buildingPrototype.isDefined)
    yield {
      val pos = startLocationPositions(player.id.id)
      val slotPos = StartLocation.positions(slotId)

      val i = pos.i + slotPos.i
      val j = if (orientations(player.id.id) == StartLocationOrientation.TOP) pos.j - slotPos.j else pos.j + slotPos.j
      val xy = new IJ(i, j).toXY

      val prototype = slot.buildingPrototype.get
      new Building(
        buildingIdIterator.next,
        prototype,
        x = xy.x,
        y = xy.y,
        population = config.getStartPopulation(prototype),
        owner = Some(player.id),
        strengthened = false,
        strengtheningStartTime = 0,
        lastShootTime = 0)
    }

  def getStartLocations(players: List[Player], positions: Map[Int, IJ], orientations: Map[Int, StartLocationOrientation]) =
    for (player ← players)
    yield {
      val id = player.id.id
      val pos = positions(id)
      StartLocationPosDTO.newBuilder()
        .setPlayerId(player.id.dto)
        .setX(pos.toXY.x.toFloat)
        .setY(pos.toXY.y.toFloat)
        .setOrientation(orientations(id))
        .build()
    }

  def mirrorBuildings(gameArea: GameArea, topRandomBuildings: Iterable[Building], buildingIdIterator: BuildingIdIterator) =
    for (b ← topRandomBuildings)
    yield {
      val pos = gameArea.mirror(b.pos)
      new Building(buildingIdIterator.next, b.prototype, pos.x, pos.y, b.population, b.owner, b.strengthened, b.strengtheningStartTime, b.lastShootTime)
    }

  def init(time: Long, players: List[Player], big: Boolean, config: GameConfig) = {
    assert(players.size == 2)

    val gameArea = new GameArea(big)

    val startLocationPositions = gameArea.randomStartLocationPositions

    val playersSlotPositions = getPlayersSlotPositions(startLocationPositions)

    val buildingIdIterator = new BuildingIdIterator

    val playersBuildings = getPlayerBuildings(players, startLocationPositions, buildingIdIterator, config)

    val topRandomBuildings = MapGenerator.getRandomBuildings(playersSlotPositions, gameArea.h, Math.floor(gameArea.v / 2).toInt, buildingIdIterator, config)

    val bottomRandomBuildings = mirrorBuildings(gameArea, topRandomBuildings, buildingIdIterator)

    val buildings = playersBuildings ++ topRandomBuildings ++ bottomRandomBuildings

    val startLocations = getStartLocations(players, startLocationPositions, orientations)

    val playerStates = for (player ← players) yield player.id → new PlayerState(player.skills.stat, 0)

    new GameState(
      time,
      new Players(players.map(p ⇒ p.id → p).toMap),
      new Buildings(buildings.map(b ⇒ b.id → b).toMap),
      new GameUnits(List.empty),
      new Fireballs(List.empty),
      new Volcanoes(List.empty),
      new Tornadoes(List.empty),
      new Bullets(List.empty),
      new GameItems(players.map(p ⇒ p.id → GameItems.init(p.id, p.items)).toMap),
      new UnitIdIterator,
      startLocations,
      new PlayerStates(playerStates.toMap),
      config
    )
  }
}

class GameState(val time: Long,
                val players: Players,
                val buildings: Buildings,
                val units: GameUnits,
                val fireballs: Fireballs,
                val volcanoes: Volcanoes,
                val tornadoes: Tornadoes,
                val bullets: Bullets,
                val gameItems: GameItems,
                val unitIdIterator: UnitIdIterator,
                val startLocations: Iterable[StartLocationPosDTO],
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

    val createdBullets = createBullets(buildings, units, time, config, playerStates)

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

    val updateBuildingMessages = Buildings.getUpdateMessages(buildings.buildings, newBuildings.buildings)
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

    val newPlayerStates = playerStates.updateChurchesPopulation(buildings.buildings)

    val newGameState = new GameState(
      newTime,
      players,
      newBuildings,
      newUnits,
      newFireballs,
      newVolcanoes,
      newTornadoes,
      newBullets,
      newGameItems,
      unitIdIterator,
      startLocations,
      newPlayerStates,
      config
    )

    val messages = addUnitMessages ++ updateUnitMessages ++ removeUnitMessages ++
      updateBuildingMessages ++
      addFireballMessages ++ addVolcanoMessages ++ addTornadoMessages ++ addBulletsMessages

    val personalMessages = updateItemsStatesMessages

    (newGameState, messages, personalMessages)
  }

  def isPlayerLose(playerId: PlayerId) =
    !buildings.buildings.exists { case (buildingId, building) ⇒ building.owner == Some(playerId)}

  def dtoBuilder(id: PlayerId) =
    GameStateDTO.newBuilder()
      .setSelfId(id.dto)
      .addAllPlayers(players.dto.asJava)
      .addAllStartLocations(startLocations.asJava)
      .addAllBuildings(buildings.dto.asJava)
      .addAllUnits(units.dto(time).asJava)
      .addAllVolcanoes(volcanoes.dto(time).asJava)
      .addAllTornadoes(tornadoes.dto(time).asJava)
      .addAllBullets(bullets.dto(time).asJava)
      .setItemsState(gameItems.dto(id, time, config))
}
