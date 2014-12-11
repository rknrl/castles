package ru.rknrl.castles.game.objects.fireballs

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.rmi.AddFireballMsg
import ru.rknrl.dto.GameDTO.{FireballDTO, PointDTO}
import ru.rknrl.utils.{PeriodObjectCollection, Point}

object Fireballs {
  type Fireballs = PeriodObjectCollection[FireballDTO, Fireball]

  def `casts→fireballs`(casts: Map[PlayerId, PointDTO], config: GameConfig, time: Long) =
    for ((playerId, dto) ← casts)
    yield new Fireball(playerId, new Point(dto.getX, dto.getY), config.fireballFlyDuration, time)

  def `fireballs→addMessages`(fireballs: Iterable[Fireball], time: Long) =
    fireballs.map(f ⇒ AddFireballMsg(f.dto(time)))

  def inRadius(fireballs: Iterable[Fireball], pos: Point, config: GameConfig, playerStates: PlayerStates) =
    fireballs.filter(f ⇒ f.pos.distance(pos) < config.fireballSplashRadius(playerStates(f.playerId)))
}
