//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.bot

import akka.actor.{Actor, ActorRef}
import protos._
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.matchmaking.MatchMaking.ConnectToGame
import ru.rknrl.core.points.Point
import ru.rknrl.logging.ShortActorLogging

class GameBot(accountId: AccountId) extends Actor with ShortActorLogging {
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

  override val logFilter: Any ⇒ Boolean = {
    case _: GameStateUpdate ⇒ false
    case _ ⇒ true
  }

  override def receive: Receive = baseReceive

  val baseReceive: Receive = {
    case ConnectToGame(gameRef) ⇒
      game = Some(gameRef)
      send(gameRef, Join(accountId, self))

    case newGameState: GameState ⇒
      gameState = Some(newGameState)
      playerId = Some(newGameState.selfId)
      mapDiagonal = Math.sqrt(newGameState.width * newGameState.height)

    case gameStateUpdate: GameStateUpdate ⇒
      update(GameStateMerge.merge(gameState.get, gameStateUpdate))

    case gameOver: GameOver ⇒
      if (gameOver.playerId == playerId.get)
        send(game.get, LeaveGame())
  }

  def update(newGameState: GameState) = {
    gameState = Some(newGameState)
    val time = System.currentTimeMillis()

    if (time - lastTime > moveInterval) {
      lastTime = time

      val myBuildings = getMyBuildings

      val fromBuildings = myBuildings.filter(_.population > 5)

      val enemyBuildings = getEnemyBuildings

      if (fromBuildings.nonEmpty && enemyBuildings.nonEmpty) {

        if (toBuildingId.isEmpty ||
          my(gameState.get.buildings.find(_.id == toBuildingId.get).get) ||
          myBuildings.size < myBuildingsSize ||
          time - toBuildingSetTime > timeout) {

          val weights = buildingsToWeights(enemyBuildings, myBuildings)
          val toBuildings = weights.toList.sortBy(_.weight).take(3)

          val friendly = Math.random() < 0.33
          if (friendly) {
            toBuildingId = Some(myBuildings.minBy(_.population).id)
          } else {
            val rnd = Math.floor(Math.random() * toBuildings.size).toInt
            toBuildingId = Some(toBuildings(rnd).id)
            toBuildingSetTime = time
          }
        }

        myBuildingsSize = myBuildings.size

        send(sender,
          Move(
            fromBuildings.map(_.id),
            toBuildingId.get
          )
        )

        if (time - lastCastTime > castInterval) {
          lastCastTime = time

          val items = newGameState.itemStates.items

          val availableCast = items.filter(itemState ⇒
            itemState.count > 0 &&
              itemState.itemType != ItemType.TORNADO)

          if (availableCast.nonEmpty) {
            val rnd = Math.floor(Math.random() * availableCast.size).toInt
            val itemType = availableCast.toList(rnd).itemType

            val ownedEnemyBuildings = enemyBuildings.filter(_.owner.isDefined)

            itemType match {
              case ItemType.FIREBALL ⇒
                send(sender, CastFireball(ownedEnemyBuildings.sortBy(_.population)(Ordering.Int.reverse).head.pos))
              case ItemType.VOLCANO ⇒
                send(sender, CastVolcano(ownedEnemyBuildings.sortBy(_.population)(Ordering.Int.reverse).head.pos))
              case ItemType.STRENGTHENING ⇒
                send(sender, CastStrengthening(myBuildings.sortBy(_.population)(Ordering.Int.reverse).head.id))
              case ItemType.ASSISTANCE ⇒
                send(sender, CastAssistance(myBuildings.sortBy(_.population).head.id))
            }
          }
        }
      }
    }
  }

  def buildings = gameState.get.buildings

  def getMyBuildings = buildings.filter(my).toList

  def getEnemyBuildings = buildings.filterNot(my).toList

  def my(b: BuildingDTO) = b.owner.isDefined && b.owner.get == playerId.get

  def buildingsToWeights(enemyBuildings: Seq[BuildingDTO], myBuildings: Seq[BuildingDTO]) =
    for (b ← enemyBuildings)
      yield Weight(
        b.id,
        distanceWeight(b.pos, myBuildings) +
          ownerWeight(b.owner) +
          populationWeight(b.population) +
          strengthenedWeight(b.strengthened)
      )

  def distanceWeight(pos: PointDTO, myBuildings: Seq[BuildingDTO]) = {
    val myPos = Point(pos)
    val distances = myBuildings.map(b ⇒ Point(b.pos).distance(myPos))
    (distances.sum / myBuildings.size) / mapDiagonal // from 0 to 1
  }

  def ownerWeight(owner: Option[PlayerId]) = if (owner.isDefined) 0.3 else 0.0

  def populationWeight(population: Double) = population * 3 / 99

  def strengthenedWeight(strengthened: Boolean) = if (strengthened) 0.3 else 0.0
}
