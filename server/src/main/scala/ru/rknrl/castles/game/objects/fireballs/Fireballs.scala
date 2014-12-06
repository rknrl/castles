package ru.rknrl.castles.game.objects.fireballs

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.players.{PlayerStates, PlayerId}
import ru.rknrl.utils.Point
import ru.rknrl.castles.rmi.b2c.AddFireballMsg
import ru.rknrl.dto.GameDTO.PointDTO

object Fireballs {
  def `casts→fireballs`(casts: Map[PlayerId, PointDTO]) =
    for ((playerId, dto) ← casts)
    yield new Fireball(playerId, dto.getX, dto.getY)

  def `fireballs→addMessages`(fireballs: Iterable[Fireball]) =
    fireballs.map(f ⇒ AddFireballMsg(f.dto))

  def inRadius(fireballs: Iterable[Fireball], pos: Point, config: GameConfig, playerStates: PlayerStates) =
    fireballs.filter(f ⇒ f.pos.distance(pos) < config.fireballRadius(playerStates(f.playerId)))
}
