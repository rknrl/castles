package ru.rknrl.castles.game.objects.units

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.bullets.Bullet
import ru.rknrl.castles.game.objects.fireballs.{Fireball, Fireballs}
import ru.rknrl.castles.game.objects.players.PlayerStates
import ru.rknrl.castles.game.objects.tornadoes.{Tornado, Tornadoes}
import ru.rknrl.castles.game.objects.volcanoes.{Volcano, Volcanoes}
import ru.rknrl.castles.rmi.{AddUnitMsg, RemoveUnitMsg, UpdateUnitMsg}

class GameUnits(val units: Iterable[GameUnit]) {
  def add(newUnits: Iterable[GameUnit]) =
    new GameUnits(units ++ newUnits)

  def applyRemoveMessages(messages: Iterable[RemoveUnitMsg]) =
    new GameUnits(units.filter(u ⇒ !messages.exists(_.unitIdDTO.getId == u.id.id)))

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
          val bulletPlayer = if (building.owner.isDefined) Some(playerStates(building.owner.get)) else None
          newUnit = newUnit.setCount(config.unitCountAfterBulletHit(newUnit.count, building, playerStates(unit.owner), bulletPlayer))
        }
        newUnit
      }
    )

  def applyFireballs(fireballs: Iterable[Fireball], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new GameUnits(
      for (unit ← units)
      yield {
        val nearFireballs = Fireballs.inRadius(fireballs, unit.getPos(time), config, playerStates)
        var newUnit = unit
        for (fireball ← nearFireballs)
          newUnit = newUnit.setCount(config.unitCountAfterFireballHit(newUnit.count, playerStates(unit.owner), playerStates(fireball.playerId)))
        newUnit
      }
    )

  def applyTornadoes(tornadoes: Iterable[Tornado], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new GameUnits(
      for (unit ← units)
      yield {
        val nearTornadoes = Tornadoes.inRadius(tornadoes, unit.getPos(time), config, playerStates, time)
        var newUnit = unit
        for (tornado ← nearTornadoes)
          newUnit = newUnit.setCount(config.unitCountAfterTornadoHit(newUnit.count, playerStates(unit.owner), playerStates(tornado.playerId)))
        newUnit
      }
    )

  def applyVolcanoes(volcanoes: Iterable[Volcano], playerStates: PlayerStates, config: GameConfig, time: Long) =
    new GameUnits(
      for (unit ← units)
      yield {
        val nearVolcanoes = Volcanoes.inRadius(volcanoes, unit.getPos(time), config, playerStates)
        var newUnit = unit
        for (volcano ← nearVolcanoes)
          newUnit = newUnit.setCount(config.unitCountAfterVolcanoHit(newUnit.count, playerStates(unit.owner), playerStates(volcano.playerId)))
        newUnit
      }
    )

  def `killed→removeMessages` =
    for (unit ← units if unit.floorCount <= 0) yield new RemoveUnitMsg(unit.id.dto)
}

object GameUnits {
  def `units→addMessages`(units: Iterable[GameUnit], time: Long) =
    for (unit ← units) yield AddUnitMsg(unit.dto(time))

  def getUpdateMessages(oldUnits: Iterable[GameUnit], newUnits: Iterable[GameUnit], time: Long) =
    for (newUnit ← newUnits;
         oldUnit = oldUnits.find(_.id == newUnit.id)
         if oldUnit.isDefined
         if oldUnit.get differentWith newUnit
    ) yield new UpdateUnitMsg(newUnit.updateDto(time))
}