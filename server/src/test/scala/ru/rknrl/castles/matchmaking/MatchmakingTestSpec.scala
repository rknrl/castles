//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.{ActorRef, Props}
import ru.rknrl.castles.Config
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.dto.AccountType.{FACEBOOK, ODNOKLASSNIKI, VKONTAKTE}
import ru.rknrl.dto.{AccountId, UserInfoDTO}
import ru.rknrl.test.ActorsTest

import scala.concurrent.duration.{FiniteDuration, _}

class MatchmakingTestSpec extends ActorsTest {
  var matchmakingIterator = 0

  val id1 = AccountId(VKONTAKTE, "1")
  val id2 = AccountId(FACEBOOK, "1")
  val id3 = AccountId(ODNOKLASSNIKI, "2")
  val id4 = AccountId(ODNOKLASSNIKI, "4")
  val id5 = AccountId(ODNOKLASSNIKI, "5")

  val info1 = UserInfoDTO(id1)
  val info2 = UserInfoDTO(id2)
  val info3 = UserInfoDTO(id3)
  val info4 = UserInfoDTO(id4)
  val info5 = UserInfoDTO(id5)

  val top5 = new Top(List(
    TopUser(id1, 1512.2, info1),
    TopUser(id2, 1400, info2),
    TopUser(id3, 1399, info3),
    TopUser(id4, 1300, info4),
    TopUser(id5, 1200, info5)
  ))

  def newMatchmaking(gameCreator: GameCreator = gameCreatorMock(),
                     gameFactory: IGameFactory = new GameFactory,
                     interval: FiniteDuration = 30 hours,
                     top: Top = top5,
                     config: Config = configMock(),
                     database: ActorRef,
                     graphite: ActorRef) = {
    matchmakingIterator += 1
    system.actorOf(
      Props(
        classOf[MatchMaking],
        gameCreator,
        gameFactory,
        interval,
        top,
        config,
        database,
        graphite
      ),
      "matchmaking-" + matchmakingIterator
    )
  }

}
