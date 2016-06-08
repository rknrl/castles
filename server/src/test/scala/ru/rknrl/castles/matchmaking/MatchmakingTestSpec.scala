//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import akka.actor.ActorRef
import ru.rknrl.castles.Config
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.test.ActorsTest

import scala.concurrent.duration.{FiniteDuration, _}

class MatchmakingTestSpec extends ActorsTest {
  var matchmakingIterator = 0

  def newMatchmaking(gameCreator: GameCreator = gameCreatorMock(),
                     gameFactory: IGameFactory = new GameFactory,
                     interval: FiniteDuration = 30 hours,
                     config: Config = configMock(),
                     database: ActorRef,
                     graphite: ActorRef) = {
    matchmakingIterator += 1
    system.actorOf(
      MatchMaking.props(
        gameCreator = gameCreator,
        gameFactory = gameFactory,
        interval = interval,
        config = config,
        storage = database,
        graphite = graphite
      ),
      "matchmaking-" + matchmakingIterator
    )
  }

}
