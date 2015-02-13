package ru.rknrl.castles.game.state.tornadoes

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.rmi.B2C.AddTornado
import ru.rknrl.dto.GameDTO.{CastTorandoDTO, TornadoDTO}
import ru.rknrl.utils.{PeriodObjectCollection, Point, Points}

import scala.collection.JavaConverters._

object Tornadoes {
  type Tornadoes = PeriodObjectCollection[TornadoDTO, Tornado]

  def `casts→tornadoes`(casts: Map[PlayerId, CastTorandoDTO], time: Long, config: GameConfig, playerStates: PlayerStates) =
    for ((playerId, dto) ← casts)
    yield new Tornado(playerId, new Points(dto.getPointsList.asScala), time, config.tornadoDuration(playerStates(playerId)), config.tornadoSpeed)

  def `tornadoes→addMessages`(tornadoes: Iterable[Tornado], time: Long) =
    tornadoes.map(v ⇒ AddTornado(v.dto(time)))

  def inRadius(tornadoes: Iterable[Tornado], pos: Point, config: GameConfig, playerStates: PlayerStates, time: Long) =
    tornadoes.filter(t ⇒ t.getPos(time).distance(pos) < config.tornadoRadius(playerStates(t.playerId)))
}