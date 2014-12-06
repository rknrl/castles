package ru.rknrl.castles.game.objects.volcanoes

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.players.{PlayerId, PlayerStates}
import ru.rknrl.utils.{Point, PeriodObjectCollection}
import ru.rknrl.castles.rmi.b2c.AddVolcanoMsg
import ru.rknrl.dto.GameDTO.{PointDTO, VolcanoDTO}

object Volcanoes {
  type Volcanoes = PeriodObjectCollection[VolcanoDTO, Volcano]

  def `casts→volcanoes`(casts: Map[PlayerId, PointDTO], time: Long, config: GameConfig, playerStates: PlayerStates) =
    for ((playerId, dto) ← casts)
    yield new Volcano(playerId, dto.getX, dto.getY, time, config.volcanoDuration(playerStates(playerId)))

  def `volcanoes→addMessages`(volcanoes: Iterable[Volcano], time: Long) =
    volcanoes.map(v ⇒ AddVolcanoMsg(v.dto(time)))

  def inRadius(volcanoes: Iterable[Volcano], pos: Point, config: GameConfig, playerStates: PlayerStates) =
    volcanoes.filter(v ⇒ v.pos.distance(pos) < config.volcanoRadius(playerStates(v.playerId)))
}

