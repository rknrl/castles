package ru.rknrl.castles.game.objects.fireballs

import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.utils.Point
import ru.rknrl.dto.GameDTO.FireballDTO

class Fireball(val playerId: PlayerId,
               val x: Double,
               val y: Double) {
  val pos = new Point(x, y)

  def dto = FireballDTO.newBuilder()
    .setX(x.toFloat)
    .setY(y.toFloat)
    .build()
}
