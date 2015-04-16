//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.bot

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.MatchMaking.ConnectToGame
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.{Building, GameState, Player}
import ru.rknrl.castles.rmi.B2C.JoinedGame
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi._
import ru.rknrl.core.points.Point
import ru.rknrl.dto._
import ru.rknrl.{BugType, Logged, SilentLog}

class Bot(accountId: AccountId, config: GameConfig, bugs: ActorRef) extends Actor {
  val moveInterval = 5000
  val castInterval = 10000
  var lastTime = 0L

  var mapDiagonal = Double.NaN

  var game: Option[ActorRef] = None
  var gameState: Option[GameState] = None
  var playerId: Option[PlayerId] = None

  var toBuildingId: Option[BuildingId] = None
  var myBuildingsSize = 0

  var toBuildingSetTime = 0L
  val timeout = 10000L

  var lastCastTime = 0L

  case class Weight(id: BuildingId, weight: Double)

  val log = new SilentLog

  def logged(r: Receive) = new Logged(r, log, Some(bugs), Some(BugType.BOT), {
    case state: GameState ⇒ false
    case _ ⇒ true
  })

  override def receive: Receive = logged({
    case ConnectToGame(gameRef) ⇒
      game = Some(gameRef)
      gameRef ! Join(accountId, self)
      gameRef ! C2B.JoinGame

    case JoinedGame(gameState) ⇒
      playerId = Some(gameState.selfId)
      mapDiagonal = Math.sqrt(gameState.width * gameState.height)

    case newGameState: GameState ⇒ update(newGameState)
  })

  def update(newGameState: GameState) = {
    gameState = Some(newGameState)
    val time = newGameState.time

    if (time - lastTime > moveInterval) {
      lastTime = time

      val myBuildings = getMyBuildings

      val fromBuildings = myBuildings.filter(_.count > 5)

      val enemyBuildings = getEnemyBuildings

      if (fromBuildings.size > 0 && enemyBuildings.size > 0) {

        if (toBuildingId.isEmpty ||
          my(gameState.get.buildings.find(_.id == toBuildingId.get).get) ||
          myBuildings.size < myBuildingsSize ||
          time - toBuildingSetTime > timeout) {

          val weights = buildingsToWeights(enemyBuildings, myBuildings)
          val toBuildings = weights.toList.sortBy(_.weight).take(3)

          val friendly = Math.random() < 0.33
          if (friendly) {
            toBuildingId = Some(myBuildings.minBy(_.count).id)
          } else {
            val rnd = Math.floor(Math.random() * toBuildings.size).toInt
            toBuildingId = Some(toBuildings(rnd).id)
            toBuildingSetTime = time
          }
        }

        myBuildingsSize = myBuildings.size

        sender ! Move(
          MoveDTO(
            fromBuildings.map(_.id),
            toBuildingId.get
          )
        )

        if (time - lastCastTime > castInterval) {
          lastCastTime = time

          val items = newGameState.items.states(playerId.get).items.map { case (_, itemState) ⇒ itemState }

          val availableCast = items.filter(itemState ⇒
            itemState.count > 0 &&
              itemState.itemType != ItemType.TORNADO &&
              newGameState.items.canCast(playerId.get, itemState.itemType, config, time))

          if (availableCast.size > 0) {
            val rnd = Math.floor(Math.random() * availableCast.size).toInt
            val itemType = availableCast.toList(rnd).itemType

            val ownedEnemyBuildings = enemyBuildings.filter(_.owner.isDefined)

            itemType match {
              case ItemType.FIREBALL ⇒
                sender ! CastFireball(ownedEnemyBuildings.sortBy(_.count)(Ordering.Double.reverse).head.pos.dto)
              case ItemType.VOLCANO ⇒
                sender ! CastVolcano(ownedEnemyBuildings.sortBy(_.count)(Ordering.Double.reverse).head.pos.dto)
              case ItemType.STRENGTHENING ⇒
                sender ! CastStrengthening(myBuildings.sortBy(_.count)(Ordering.Double.reverse).head.id)
              case ItemType.ASSISTANCE ⇒
                sender ! CastAssistance(myBuildings.sortBy(_.count).head.id)
            }
          }
        }
      }
    }
  }

  def buildings = gameState.get.buildings

  def getMyBuildings = buildings.filter(my).toList

  def getEnemyBuildings = buildings.filterNot(my).toList

  def my(b: Building) = b.owner.isDefined && b.owner.get.id == playerId.get

  def buildingsToWeights(enemyBuildings: Iterable[Building], myBuildings: Iterable[Building]) =
    for (b ← enemyBuildings)
      yield Weight(
        b.id,
        distanceWeight(b.pos, myBuildings) +
          ownerWeight(b.owner) +
          populationWeight(b.count) +
          strengthenedWeight(b.strengthening.isDefined)
      )

  def distanceWeight(pos: Point, myBuildings: Iterable[Building]) = {
    val distances = myBuildings.map(_.pos.distance(pos))
    (distances.sum / myBuildings.size) / mapDiagonal // from 0 to 1
  }

  def ownerWeight(owner: Option[Player]) = if (owner.isDefined) 0.3 else 0.0

  def populationWeight(population: Double) = population * 3 / 99

  def strengthenedWeight(strengthened: Boolean) = if (strengthened) 0.3 else 0.0
}
