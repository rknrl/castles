//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.fireballs

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.game.points.Point
import ru.rknrl.castles.rmi.B2C.AddFireball
import ru.rknrl.dto.GameDTO.{PlayerIdDTO, FireballDTO, PointDTO}
import ru.rknrl.utils.PeriodObjectCollection

object Fireballs {
  type Fireballs = PeriodObjectCollection[FireballDTO, Fireball]

  def `casts→fireballs`(casts: Map[PlayerIdDTO, PointDTO], config: GameConfig, time: Long) =
    for ((playerId, dto) ← casts)
    yield new Fireball(playerId, Point(dto), config.fireballFlyDuration, time)

  def `fireballs→addMessages`(fireballs: Iterable[Fireball], time: Long) =
    fireballs.map(f ⇒ AddFireball(f.dto(time)))

  def inRadius(fireballs: Iterable[Fireball], pos: Point, config: GameConfig, playerStates: PlayerStates) =
    fireballs.filter(f ⇒ f.pos.distance(pos) < config.fireballSplashRadius(playerStates(f.playerId)))
}
