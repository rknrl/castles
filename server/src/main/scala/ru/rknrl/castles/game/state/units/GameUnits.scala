//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.units

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.Moving.EnterUnit
import ru.rknrl.castles.game.state.bullets.Bullet
import ru.rknrl.castles.game.state.fireballs.{Fireball, Fireballs}
import ru.rknrl.castles.game.state.players.PlayerStates
import ru.rknrl.castles.game.state.tornadoes.{Tornado, Tornadoes}
import ru.rknrl.castles.game.state.volcanoes.{Volcano, Volcanoes}
import ru.rknrl.castles.rmi.B2C._

class GameUnits(val units: Iterable[GameUnit]) {
  def add(newUnits: Iterable[GameUnit]) =
    new GameUnits(units ++ newUnits)

  def applyEnterUnits(enters: Iterable[EnterUnit]) =
    new GameUnits(units.filter(u ⇒ !enters.exists(_.unit.id == u.id)))

  def applyKillMessages(messages: Iterable[KillUnit]) =
    new GameUnits(units.filter(u ⇒ !messages.exists(_.killedId.getId == u.id)))

  def dto(time: Long) =
    for (unit ← units) yield unit.dto(time)

  def updateDto(time: Long) =
    for (unit ← units) yield unit.updateDto(time)

  def applyBullets(bullets: Iterable[Bullet], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new GameUnits(
      for (unit ← units)
      yield {
        val myBullets = bullets.filter(b ⇒ b.unit.id == unit.id)
        var newUnit = unit
        for (bullet ← myBullets) {
          val building = bullet.building
          val bulletPlayer = playerStates(building.owner)
          newUnit = newUnit.setCount(config.unitCountAfterBulletHit(newUnit, building, playerStates(unit.owner), bulletPlayer))
        }
        newUnit
      }
    )

  def applyFireballs(fireballs: Iterable[Fireball], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new GameUnits(
      for (unit ← units)
      yield {
        val nearFireballs = Fireballs.inRadius(fireballs, unit.pos(time), config, playerStates)
        var newUnit = unit
        for (fireball ← nearFireballs)
          newUnit = newUnit.setCount(config.unitCountAfterFireballHit(newUnit, playerStates(unit.owner), playerStates(fireball.playerId)))
        newUnit
      }
    )

  def applyTornadoes(tornadoes: Iterable[Tornado], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new GameUnits(
      for (unit ← units)
      yield {
        val nearTornadoes = Tornadoes.inRadius(tornadoes, unit.pos(time), config, playerStates, time)
        var newUnit = unit
        for (tornado ← nearTornadoes)
          newUnit = newUnit.setCount(config.unitCountAfterTornadoHit(newUnit, playerStates(unit.owner), playerStates(tornado.playerId)))
        newUnit
      }
    )

  def applyVolcanoes(volcanoes: Iterable[Volcano], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new GameUnits(
      for (unit ← units)
      yield {
        val nearVolcanoes = Volcanoes.inRadius(volcanoes, unit.pos(time), config, playerStates)
        var newUnit = unit
        for (volcano ← nearVolcanoes)
          newUnit = newUnit.setCount(config.unitCountAfterVolcanoHit(newUnit, playerStates(unit.owner), playerStates(volcano.playerId)))
        newUnit
      }
    )

  def `killed→killMessages` =
    for (unit ← units if unit.floorCount <= 0) yield KillUnit(unit.id)
}

object GameUnits {
  def `units→addMessages`(units: Iterable[GameUnit], time: Long) =
    for (unit ← units) yield AddUnit(unit.dto(time))

  def getUpdateMessages(oldUnits: Iterable[GameUnit], newUnits: Iterable[GameUnit], time: Long) =
    for (newUnit ← newUnits;
         oldUnit = oldUnits.find(_.id == newUnit.id)
         if oldUnit.isDefined
         if oldUnit.get differentWith newUnit
    ) yield new UpdateUnit(newUnit.updateDto(time))
}