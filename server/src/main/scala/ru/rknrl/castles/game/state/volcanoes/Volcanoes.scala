package ru.rknrl.castles.game.state.volcanoes

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.players.{PlayerId, PlayerStates}
import ru.rknrl.castles.rmi.B2C.AddVolcano
import ru.rknrl.dto.GameDTO.{PointDTO, VolcanoDTO}
import ru.rknrl.utils.{PeriodObjectCollection, Point}

object Volcanoes {
  type Volcanoes = PeriodObjectCollection[VolcanoDTO, Volcano]

  def `casts→volcanoes`(casts: Map[PlayerId, PointDTO], time: Long, config: GameConfig, playerStates: PlayerStates) =
    for ((playerId, dto) ← casts)
    yield new Volcano(playerId, new Point(dto), time, config.volcanoDuration(playerStates(playerId)))

  def `volcanoes→addMessages`(volcanoes: Iterable[Volcano], time: Long) =
    volcanoes.map(v ⇒ AddVolcano(v.dto(time)))

  def inRadius(volcanoes: Iterable[Volcano], pos: Point, config: GameConfig, playerStates: PlayerStates) =
    volcanoes.filter(v ⇒ v.pos.distance(pos) < config.volcanoRadius(playerStates(v.playerId)))
}

