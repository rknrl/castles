//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.bot

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.ConnectToGame
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.points.Point
import ru.rknrl.castles.game.state.GameState
import ru.rknrl.castles.game.state.buildings.{Building, BuildingId}
import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.castles.rmi.B2C.GameOver
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi._
import ru.rknrl.dto.CommonDTO.ItemType
import ru.rknrl.dto.GameDTO.MoveDTO

import scala.collection.JavaConverters._

class Bot(accountId: AccountId, config: GameConfig) extends Actor {
  val interval = 2000L
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

  override def receive: Receive = {
    case ConnectToGame(gameRef) ⇒
      game = Some(gameRef)
      gameRef ! Join(accountId, self)
      gameRef ! C2B.JoinGame

    case B2C.JoinedGame(gameState) ⇒
      playerId = Some(new PlayerId(gameState.getSelfId.getId))
      mapDiagonal = Math.sqrt(gameState.getWidth * gameState.getHeight)

    case GameOver(dto) ⇒
      if (dto.getPlayerId.getId == playerId.get.id) sender ! C2B.LeaveGame

    case newGameState: GameState ⇒
      gameState = Some(newGameState)
      val time = newGameState.time

      if (time - lastTime > interval) {
        lastTime = time

        val myBuildings = buildings.filter(my).toList

        val fromBuildings = myBuildings.filter(_.population > 5)

        if (fromBuildings.size > 0) {
          val enemyBuildings = buildings.filterNot(my).toList

          if (toBuildingId.isEmpty ||
            my(gameState.get.buildings.map(toBuildingId.get)) ||
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

          sender ! Move(
            MoveDTO.newBuilder()
              .addAllFromBuildings(fromBuildings.map(_.id.dto).asJava)
              .setToBuilding(toBuildingId.get.dto)
              .build()
          )

          if (time - lastCastTime > 10000) {
            lastCastTime = time

            val items = newGameState.gameItems.states(playerId.get).items.map { case (_, itemState) ⇒ itemState}

            val availableCast = items.filter(itemState ⇒
              itemState.count > 0 &&
                itemState.itemType != ItemType.TORNADO &&
                newGameState.gameItems.canCast(playerId.get, itemState.itemType, config, time))

            val rnd = Math.floor(Math.random() * availableCast.size).toInt
            val itemType = availableCast.toList(rnd).itemType

            val ownedEnemyBuildings = enemyBuildings.filter(_.owner.isDefined)

            itemType match {
              case ItemType.FIREBALL ⇒
                sender() ! CastFireball(ownedEnemyBuildings.sortBy(_.population)(Ordering.Double.reverse).head.pos.dto)
              case ItemType.VOLCANO ⇒
                sender() ! CastVolcano(ownedEnemyBuildings.sortBy(_.population)(Ordering.Double.reverse).head.pos.dto)
              case ItemType.STRENGTHENING ⇒
                sender() ! CastStrengthening(myBuildings.sortBy(_.population)(Ordering.Double.reverse).head.id.dto)
              case ItemType.ASSISTANCE ⇒
                sender() ! CastAssistance(myBuildings.sortBy(_.population).head.id.dto)
            }
          }
        }
      }
  }

  def buildings = gameState.get.buildings.map.map { case (id, b) ⇒ b}

  def my(b: Building) = b.owner.isDefined && b.owner.get == playerId.get

  def buildingsToWeights(enemyBuildings: Iterable[Building], myBuildings: Iterable[Building]) =
    for (b ← enemyBuildings)
    yield Weight(
      b.id,
      distanceWeight(b.pos, myBuildings) +
        ownerWeight(b.owner) +
        populationWeight(b.population) +
        strengthenedWeight(b.strengthened)
    )

  def distanceWeight(pos: Point, myBuildings: Iterable[Building]) = {
    val distances = myBuildings.map(_.pos.distance(pos))
    (distances.sum / myBuildings.size) / mapDiagonal // from 0 to 1
  }

  def ownerWeight(owner: Option[PlayerId]) = if (owner.isDefined) 0.3 else 0.0

  def populationWeight(population: Double) = population * 3 / config.maxPopulation

  def strengthenedWeight(strengthened: Boolean) = if (strengthened) 0.3 else 0.0
}
