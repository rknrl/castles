//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.dto.DeviceType

object Matcher {

  case class MatchedGameOrders(playersCount: Int,
                               orders: Seq[GameOrder],
                               isTutor: Boolean)

  def matchOrders(orders: Seq[GameOrder]) = {
    val (smallOrders, bigOrders) = split[GameOrder](orders, _.deviceType == DeviceType.PHONE)

    groupOrders(playersCount = 2, smallOrders) ++
      groupOrders(playersCount = 4, bigOrders)
  }

  def groupOrders(playersCount: Int, orders: Seq[GameOrder]) = {
    val (tutorOrders, notTutorOrders) = split[GameOrder](orders, _.accountState.gamesCount == 0)

    var matched = tutorOrders.map(order ⇒ MatchedGameOrders(playersCount, List(order), isTutor = true))

    var sorted = notTutorOrders.sortBy(_.rating)(Ordering.Double.reverse)

    while (sorted.size > playersCount) {
      matched = matched :+ MatchedGameOrders(playersCount, sorted.take(playersCount), isTutor = false)
      sorted = sorted.drop(playersCount)
    }

    if (sorted.nonEmpty)
      matched = matched :+ MatchedGameOrders(playersCount, sorted, isTutor = false)

    matched
  }

  def split[T](xs: Seq[T], filter: T ⇒ Boolean) =
    (xs.filter(filter), xs.filterNot(filter))
}

