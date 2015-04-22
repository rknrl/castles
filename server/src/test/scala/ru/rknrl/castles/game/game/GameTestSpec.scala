//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.game

import akka.actor.{Actor, ActorRef, Props}
import org.scalatest.{Matchers, WordSpecLike}
import ru.rknrl.castles.account.AccountState._
import ru.rknrl.castles.game.{BotFactory, IBotFactory, Game}
import ru.rknrl.castles.game.state.{GameItems, GameState}
import ru.rknrl.castles.kit.ActorsTest
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.points.Point
import ru.rknrl.dto.AccountType.{FACEBOOK, VKONTAKTE}
import ru.rknrl.dto.ItemType._
import ru.rknrl.dto._

import scala.concurrent.duration._

class FakeScheduler(game: ActorRef) extends Actor {
  def receive = {
    case _ ⇒
  }
}

class GameTestSpec extends ActorsTest with WordSpecLike with Matchers {
  val TIMEOUT = 100 millis

  var gameIterator = 0

  def newGame(gameState: GameState = gameStateMock(),
              botFactory: IBotFactory = new BotFactory(),
              isTutor: Boolean = false,
              isDev: Boolean = true,
              schedulerClass: Class[_] = classOf[FakeScheduler],
              matchmaking: ActorRef = self,
              bugs: ActorRef = self) = {
    gameIterator += 1
    system.actorOf(Props(classOf[Game], gameState, isDev, isTutor, botFactory, schedulerClass, matchmaking, bugs), "game" + gameIterator)
  }

  def updateGameState(gameState: GameState,
                      newTime: Long = 1,
                      moveActions: Map[PlayerId, MoveDTO] = Map.empty,
                      fireballCasts: Map[PlayerId, PointDTO] = Map.empty,
                      volcanoCasts: Map[PlayerId, PointDTO] = Map.empty,
                      tornadoCasts: Map[PlayerId, CastTornadoDTO] = Map.empty,
                      strengtheningCasts: Map[PlayerId, BuildingId] = Map.empty,
                      assistanceCasts: Map[PlayerId, BuildingId] = Map.empty) =
    gameState.update(
      newTime = newTime,
      moveActions = moveActions,
      fireballCasts = fireballCasts,
      volcanoCasts = volcanoCasts,
      tornadoCasts = tornadoCasts,
      strengtheningCasts = strengtheningCasts,
      assistanceCasts = assistanceCasts
    )

  def initItems: Items = Map(
    FIREBALL → 4,
    TORNADO → 4,
    VOLCANO → 4,
    STRENGTHENING → 4,
    ASSISTANCE → 4
  )

  val player0 = playerMock(
    id = PlayerId(0),
    accountId = AccountId(VKONTAKTE, "1"),
    items = initItems
  )

  val player1 = playerMock(
    id = PlayerId(1),
    accountId = AccountId(FACEBOOK, "1"),
    items = initItems
  )

  val players = Map(
    PlayerId(0) → player0,
    PlayerId(1) → player1
  )

  def initGameState = gameStateMock(
    players = players,
    buildings = List(
      buildingMock(id = BuildingId(0), pos = Point(0, 0), owner = Some(player0), count = 99),
      buildingMock(id = BuildingId(1), pos = Point(600, 600), owner = Some(player1), count = 99)
    ),
    items = new GameItems(players.map { case (id, player) ⇒ id → GameItems.init(player.items) }),
    assistancePositions = Map(
      PlayerId(0) → Point(20, 30),
      PlayerId(1) → Point(80, 90)
    )
  )

}
