//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.Assertion
import ru.rknrl.castles.account.IJ
import ru.rknrl.castles.game.GameArea.PlayerIdToSlotsPositions
import ru.rknrl.castles.game.state.Assistance.castToUnit
import ru.rknrl.castles.game.state.Building._
import ru.rknrl.castles.game.state.Bullets._
import ru.rknrl.castles.game.state.ChurchesProportion._
import ru.rknrl.castles.game.state.Fireballs._
import ru.rknrl.castles.game.state.GameItems._
import ru.rknrl.castles.game.state.Moving._
import ru.rknrl.castles.game.state.Strengthening._
import ru.rknrl.castles.game.state.Tornadoes._
import ru.rknrl.castles.game.state.Volcanoes._
import ru.rknrl.castles.game.{GameArea, GameConfig, GameMap}
import ru.rknrl.castles.rmi.B2C._
import ru.rknrl.core.points.Point
import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto.ItemType._
import ru.rknrl.dto._
import ru.rknrl.utils.IdIterator

object GameState {

  def getPlayerBuildings(players: List[Player], playersSlotsPositions: PlayerIdToSlotsPositions, buildingIdIterator: BuildingIdIterator, config: GameConfig) =
    for (player ← players;
         (slotId, buildingPrototype) ← player.slots
         if buildingPrototype.isDefined)
      yield {
        val ij = playersSlotsPositions(player.id.id)(slotId)
        val xy = ij.centerXY

        val stat = config.units(buildingPrototype.get) * player.stat

        val prototype = buildingPrototype.get
        new Building(
          id = buildingIdIterator.next,
          buildingPrototype = prototype,
          pos = xy,
          count = config.startCount(prototype),
          owner = Some(player),
          strengthening = None,
          lastShootTime = 0,
          buildingStat = stat)
      }

  def slotsPosDto(players: List[Player], positions: Map[Int, IJ], orientations: Map[Int, SlotsOrientation]) =
    for (player ← players)
      yield {
        val id = player.id.id
        val pos = positions(id)
        SlotsPosDTO(
          player.id,
          pos.centerXY.dto,
          orientations(id)
        )
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

    new GameState(
      time = time,
      width = gameArea.width,
      height = gameArea.height,
      players = players.map(p ⇒ p.id → p).toMap,
      buildings = buildings,
      units = List.empty,
      fireballs = List.empty,
      tornadoes = List.empty,
      volcanoes = List.empty,
      bullets = List.empty,
      items = new GameItems(players.map(p ⇒ p.id → GameItems.init(p.items)).toMap),
      unitIdIterator = new UnitIdIterator,
      slotsPos = slotsPos,
      assistancePositions = gameArea.assistancePositions,
      config = config
    )
  }
}

class BuildingIdIterator extends IdIterator {
  def next = BuildingId(nextInt)
}

class UnitIdIterator extends IdIterator {
  def next = UnitId(nextInt)
}

class GameState(val width: Int,
                val height: Int,
                val slotsPos: Iterable[SlotsPosDTO],
                val time: Long,
                val players: Map[PlayerId, Player],
                val buildings: Seq[Building],
                val units: Seq[GameUnit],
                val fireballs: Iterable[Fireball],
                val volcanoes: Iterable[Volcano],
                val tornadoes: Iterable[Tornado],
                val bullets: Iterable[Bullet],
                val items: GameItems,
                val config: GameConfig,
                val unitIdIterator: UnitIdIterator,
                val assistancePositions: Map[PlayerId, Point]) {

  def update(newTime: Long,
             moveActions: Map[PlayerId, MoveDTO],
             fireballCasts: Map[PlayerId, PointDTO],
             volcanoCasts: Map[PlayerId, PointDTO],
             tornadoCasts: Map[PlayerId, CastTornadoDTO],
             strengtheningCasts: Map[PlayerId, BuildingId],
             assistanceCasts: Map[PlayerId, BuildingId]) = {

    val deltaTime = newTime - time

    val churchesProportion = getChurchesProportion(buildings, players, config)

    val validFireballCasts = items.checkCasts(fireballCasts, FIREBALL, config, newTime)
    val createdFireballs = validFireballCasts.map(castToFireball(_, newTime, churchesProportion, config))

    val validVolcanoCasts = items.checkCasts(volcanoCasts, VOLCANO, config, newTime)
    val createdVolcanoes = validVolcanoCasts.map(castToVolcano(_, newTime, churchesProportion, config))

    val validTornadoCasts = items.checkCasts(tornadoCasts, TORNADO, config, newTime)
    val createdTornadoes = validTornadoCasts.map(castToTornado(_, newTime, churchesProportion, config))

    val validStrengtheningCasts = items.checkCasts(strengtheningCasts, STRENGTHENING, config, newTime)
      .filter { case (playerId, buildingId) ⇒
      val owner = buildings.find(_.id == buildingId).get.owner
      owner.isDefined && owner.get.id == playerId
    }
    val strengthenings = validStrengtheningCasts.map(castToStrengthening(_, newTime, churchesProportion, config))

    val validAssistanceCasts = items.checkCasts(assistanceCasts, ASSISTANCE, config, newTime)
      .map { case (playerId, buildingId) ⇒ players(playerId) → buildingId }
      .filter { case (player, buildingId) ⇒ buildings.find(_.id == buildingId).get.owner == Some(player) }

    val assistanceUnits = validAssistanceCasts.map(castToUnit(_, buildings, config, churchesProportion, unitIdIterator, assistancePositions, newTime))

    val exitUnits = moveActionsToExitUnits(moveActions, buildings, unitIdIterator, newTime)
    val createdUnits = exitUnits ++ assistanceUnits
    val enterUnits = units.filter(_.isFinish(newTime))

    val createdBullets = createBullets(canShoot(buildings, newTime, config), units, newTime, config)

    val finishedFireballs = fireballs.filter(_.isFinish(newTime))
    val damagers = finishedFireballs ++ volcanoes ++ tornadoes

    val finishedBullets = bullets.filter(_.isFinish(newTime))

    val newBuildings = buildings
      .map(_.regenerate(deltaTime, config))
      .map(_.applyExitUnits(exitUnits))
      .map(_.applyEnterUnits(enterUnits, config))
      .map(_.applyStrengthening(strengthenings))
      .map(_.cleanupStrengthening(newTime))
      .map(_.applyDamagers(damagers, newTime))
      .map(_.applyShots(newTime, createdBullets))

    val unitsAfterDamage = (units ++ createdUnits)
      .filterNot(_.isFinish(newTime))
      .map(_.applyDamagers(damagers, newTime))
      .map(_.applyBullets(finishedBullets))

    val newUnits = unitsAfterDamage.filterNot(_.floorCount == 0)

    val newFireballs = (fireballs ++ createdFireballs)
      .filterNot(_.isFinish(newTime))

    val newVolcanoes = (volcanoes ++ createdVolcanoes)
      .filterNot(_.isFinish(newTime))

    val newTornadoes = (tornadoes ++ createdTornadoes)
      .filterNot(_.isFinish(newTime))

    val newBullets = (bullets ++ createdBullets)
      .filterNot(_.isFinish(newTime))

    val newItems = items
      .applyCasts(validFireballCasts, FIREBALL, time)
      .applyCasts(validStrengtheningCasts, STRENGTHENING, time)
      .applyCasts(validTornadoCasts, TORNADO, time)
      .applyCasts(validVolcanoCasts, VOLCANO, time)
      .applyCasts(validAssistanceCasts.map { case (player, cast) ⇒ player.id → cast }, ASSISTANCE, time)

    new GameState(
      width = width,
      height = height,
      slotsPos = slotsPos,
      time = newTime,
      players = players,
      buildings = newBuildings,
      units = newUnits,
      fireballs = newFireballs,
      volcanoes = newVolcanoes,
      tornadoes = newTornadoes,
      bullets = newBullets,
      items = newItems,
      config = config,
      unitIdIterator = unitIdIterator,
      assistancePositions = assistancePositions
    )
  }

  def isPlayerLose(playerId: PlayerId) =
    !buildings.exists(b ⇒ b.owner.isDefined && b.owner.get.id == playerId)

  def playersDto =
    for ((id, player) ← players)
      yield PlayerDTO(id, player.userInfo)

  def dto(id: PlayerId, gameOvers: Seq[GameOverDTO]) =
    GameStateDTO(
      width = width,
      height = height,
      selfId = id,
      slots = slotsPos.toSeq,
      buildings = buildings.map(_.dto),
      units = units.map(_.dto(time)),
      volcanoes = volcanoes.map(_.dto(time)).toSeq,
      tornadoes = tornadoes.map(_.dto(time)).toSeq,
      bullets = bullets.map(_.dto(time)).toSeq,
      itemStates = items.dto(id, time, config),
      players = playersDto.toSeq,
      gameOvers = gameOvers
    )
}
