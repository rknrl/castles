//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.{DamagerConfig, GameConfig}
import ru.rknrl.dto._

case class Fireball(playerId: PlayerId,
                    pos: Point,
                    startTime: Long,
                    duration: Long,
                    damagerConfig: DamagerConfig) extends Periodic with Damager {

  def pos(time: Long) = pos

  def dto(time: Long) = FireballDTO(pos.dto, millisTillEnd(time))
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

  def dto(time: Long) = VolcanoDTO(pos.dto, millisTillEnd(time))
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

  def pos(time: Long) = points.pos(millisFromsStart(time))

  def dto(time: Long) =
    TornadoDTO(
      points = points.dto,
      millisFromStart = millisFromsStart(time),
      millisTillEnd = millisTillEnd(time)
    )
}

object Tornadoes {
  def castToTornado(cast: (PlayerId, CastTornadoDTO), time: Long, churchesProportion: ChurchesProportion, config: GameConfig) =
    new Tornado(
      playerId = cast._1,
      points = Points.fromDto(cast._2.points),
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
