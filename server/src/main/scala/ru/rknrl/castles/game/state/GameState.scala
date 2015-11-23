//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import protos.ItemType._
import protos._
import ru.rknrl.IdIterator
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.Assistance.castToUnit
import ru.rknrl.castles.game.state.Building._
import ru.rknrl.castles.game.state.Bullets._
import ru.rknrl.castles.game.state.ChurchesProportion._
import ru.rknrl.castles.game.state.Fireballs._
import ru.rknrl.castles.game.state.Moving._
import ru.rknrl.castles.game.state.Strengthening._
import ru.rknrl.castles.game.state.Tornadoes._
import ru.rknrl.castles.game.state.Volcanoes._
import ru.rknrl.core.points.Point

class BuildingIdIterator extends IdIterator {
  def next = BuildingId(nextInt)
}

class UnitIdIterator extends IdIterator {
  def next = UnitId(nextInt)
}

case class GameState(width: Int,
                     height: Int,
                     slotsPos: Iterable[SlotsPos],
                     time: Long,
                     players: Map[PlayerId, Player],
                     buildings: Seq[Building],
                     units: Seq[GameUnit],
                     fireballs: Iterable[Fireball],
                     volcanoes: Iterable[Volcano],
                     tornadoes: Iterable[Tornado],
                     bullets: Iterable[Bullet],
                     items: GameItems,
                     config: GameConfig,
                     unitIdIterator: UnitIdIterator,
                     assistancePositions: Map[PlayerId, Point]) {

  def update(newTime: Long,
             moveActions: Map[PlayerId, protos.Move],
             fireballCasts: Map[PlayerId, PointDTO],
             volcanoCasts: Map[PlayerId, PointDTO],
             tornadoCasts: Map[PlayerId, CastTornado],
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
      yield protos.Player(id, player.userInfo)

  def dto(id: PlayerId, gameOvers: Seq[GameOver]) =
    protos.GameState(
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
