//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import protos._
import ru.rknrl.castles.game.{DamagerConfig, GameConfig}
import ru.rknrl.core.points.{Point, Points}
import ru.rknrl.core.{Damager, Periodic, Stat}

case class Fireball(playerId: PlayerId,
                    pos: Point,
                    startTime: Long,
                    duration: Long,
                    damagerConfig: DamagerConfig) extends Periodic with Damager {

  def pos(time: Long) = pos

  def dto(time: Long) = protos.Fireball(pos.dto, millisTillEnd(time))
}

object Fireballs {
  def castToFireball(cast: (PlayerId, PointDTO), time: Long, churchesProportion: ChurchesProportion, config: GameConfig) =
    new Fireball(
      playerId = cast._1,
      pos = Point(cast._2),
      startTime = time,
      duration = config.fireball.flyDuration,
      damagerConfig = config.fireball.damage.bonused(churchesProportion(cast._1))
    )
}

case class Volcano(playerId: PlayerId,
                   pos: Point,
                   startTime: Long,
                   duration: Long,
                   damagerConfig: DamagerConfig) extends Periodic with Damager {

  def pos(time: Long) = pos

  def dto(time: Long) = protos.Volcano(pos.dto, millisTillEnd(time))
}

object Volcanoes {
  def castToVolcano(cast: (PlayerId, PointDTO), time: Long, churchesProportion: ChurchesProportion, config: GameConfig) =
    new Volcano(
      playerId = cast._1,
      pos = Point(cast._2),
      startTime = time,
      duration = config.volcano.duration,
      damagerConfig = config.volcano.damage.bonused(churchesProportion(cast._1))
    )
}

case class Tornado(playerId: PlayerId,
                   points: Points,
                   startTime: Long,
                   duration: Long,
                   damagerConfig: DamagerConfig) extends Periodic with Damager {

  def pos(time: Long) = points.pos(progress(time))

  def dto(time: Long) =
    protos.Tornado(
      points = points.dto,
      millisFromStart = millisFromStart(time),
      millisTillEnd = millisTillEnd(time)
    )
}

object Tornadoes {
  def castToTornado(cast: (PlayerId, CastTornado), time: Long, churchesProportion: ChurchesProportion, config: GameConfig) =
    new Tornado(
      playerId = cast._1,
      points = Points.fromDto(cast._2.points).toDistance(config.tornado.duration * config.tornado.speed),
      startTime = time,
      duration = config.tornado.duration,
      damagerConfig = config.tornado.damage.bonused(churchesProportion(cast._1))
    )
}

case class Strengthening(buildingId: BuildingId,
                         startTime: Long,
                         duration: Long,
                         stat: Stat) extends Periodic

object Strengthening {
  def castToStrengthening(cast: (PlayerId, BuildingId), time: Long, churchesProportion: ChurchesProportion, config: GameConfig) =
    Strengthening(
      buildingId = cast._2,
      startTime = time,
      duration = config.strengtheningDuration(churchesProportion(cast._1)),
      stat = config.strengtheningToStat(churchesProportion(cast._1))
    )
}
