//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder

object ELO {
  def getSA(playersCount: Int, place: Int) =
    if (playersCount == 4)
      place match {
        case 1 ⇒ 1.0
        case 2 ⇒ 0.5
        case 3 ⇒ 0.25
        case 4 ⇒ 0.0
      }
    else
    if (place == 1) 1.0 else 0.0

  /** http://en.wikipedia.org/wiki/Elo_rating_system */
  def getNewRating(ratingA: Double, ratingB: Double, gamesCountA: Int, sA: Double) = {
    val eA: Double = 1 / (1 + Math.pow(10, (ratingB - ratingA) / 400))

    val k: Double = if (ratingA > 2400) 10 else if (gamesCountA <= 30) 30 else 15

    ratingA + k * (sA - eA)
  }

  def newRating(orders: Iterable[GameOrder], order: GameOrder, place: Int) = {
    val averageEnemyRating = orders.filter(_ != order).map(_.rating).sum / (orders.size - 1)
    val sA = ELO.getSA(orders.size, place)
    getNewRating(order.rating, averageEnemyRating, order.accountState.gamesCount, sA)
  }
}
