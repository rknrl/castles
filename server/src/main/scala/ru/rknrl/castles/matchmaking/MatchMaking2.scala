//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor._
import ru.rknrl.castles.Config
import ru.rknrl.castles.game.init.GameMaps

import scala.concurrent.duration._


class MatchMaking2(interval: FiniteDuration,
                   database: ActorRef,
                   bugs: ActorRef,
                   var top: Top,
                   config: Config,
                   gameMaps: GameMaps) extends Actor {

  /** Если у бота случается ошибка - стопаем его
    * Если в игре случается ошибка, посылаем всем не вышедшим игрокам LeaveGame и стопаем актор игры
    */
  /*
    override def supervisorStrategy = OneForOneStrategy() {
      case e: Exception ⇒
        if (gameRefToGameInfo.contains(sender)) {
          val gameInfo = gameRefToGameInfo(sender)
          for (order ← gameInfo.orders;
               accountId = order.accountId
               if accountIdToGameInfo.contains(accountId) && accountIdToGameInfo(accountId) == gameInfo) {
            onAccountLeaveGame(accountId, place = gameInfo.orders.size, reward = 0, usedItems = Map.empty)
          }
          onGameOver(sender)
        }

        if (config.isDev) throw new Error(e)

        Stop
    }

    def onAccountLeaveGame(accountId: AccountId, place: Int, reward: Int, usedItems: Map[ItemType, Int]) = {
      val gameInfo = gameRefToGameInfo(sender)
      val orders = gameInfo.orders
      val order = gameInfo.order(accountId)
      val newRating = ELO.newRating(gameInfo.orders, order, place)
      top = top.insert(TopUser(accountId, newRating, order.userInfo))

      context.actorOf(Props(classOf[Patcher], accountId, reward, usedItems, newRating, self, database))

      accountIdToAccountRef(accountId) ! AccountLeaveGame(top.dto)

      Statistics.sendLeaveGameStatistics(place, gameInfo, orders, order, database)
    }
  */

  def receive = {
    case _ ⇒
  }
}
