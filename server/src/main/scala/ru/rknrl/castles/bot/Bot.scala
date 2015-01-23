package ru.rknrl.castles.bot

import akka.actor.{Actor, ActorRef}
import ru.rknrl.base.AccountId
import ru.rknrl.base.MatchMaking.ConnectToGame
import ru.rknrl.base.game.Game.Join
import ru.rknrl.castles.game.objects.buildings.{Building, BuildingId}
import ru.rknrl.castles.game.objects.players.PlayerId
import ru.rknrl.castles.game.{GameConfig, GameState}
import ru.rknrl.castles.rmi._
import ru.rknrl.dto.CommonDTO.ItemType
import ru.rknrl.dto.GameDTO.{CellSize, GameStateDTO, MoveDTO}
import ru.rknrl.utils.Point

import scala.collection.JavaConverters._

class Bot(accountId: AccountId, config: GameConfig) extends Actor {
  private val interval = 2000L
  private var lastTime = 0L

  private val cellDiagonal = Math.sqrt(CellSize.SIZE_VALUE * CellSize.SIZE_VALUE)
  private val mapDiagonal = Math.sqrt(15 * 15)

  private var game: Option[ActorRef] = None
  private var gameState: Option[GameState] = None
  private var playerId: Option[PlayerId] = None

  private var toBuildingId: Option[BuildingId] = None
  private var myBuildingsSize = 0

  private var toBuildingSetTime = 0L
  private val timeout = 10000L

  private var lastCastTime = 0L

  case class Weight(id: BuildingId, weight: Double)

  override def receive: Receive = {
    case ConnectToGame(gameRef) ⇒
      game = Some(gameRef)
      gameRef ! Join(accountId, self, self)
      gameRef ! JoinMsg()

    case JoinGameMsg(gameState: GameStateDTO) ⇒
      playerId = Some(new PlayerId(gameState.getSelfId.getId))

    case newGameState: GameState ⇒
      gameState = Some(newGameState)
      val time = newGameState.time

      if (time - lastTime > interval) {
        lastTime = time

        val myBuildings = buildings.filter(my).toList

        val fromBuildings = myBuildings.filter(_.population > 8)

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

          sender ! MoveMsg(
            MoveDTO.newBuilder()
              .addAllFromBuildings(fromBuildings.map(_.id.dto).asJava)
              .setToBuilding(toBuildingId.get.dto)
              .build()
          )

          if (time - lastCastTime > 20000) {
            lastCastTime = time

            val items = newGameState.gameItems.states(playerId.get).items.map { case (_, itemState) ⇒ itemState}

            val availableCast = items.filter(itemState ⇒ itemState.count > 0 &&
              newGameState.gameItems.canCast(playerId.get, itemState.itemType, config, time))

            val rnd = Math.floor(Math.random() * availableCast.size).toInt
            val itemType = availableCast.toList(rnd).itemType

            itemType match {
              case ItemType.FIREBALL ⇒
                sender() ! CastFireballMsg(enemyBuildings.sortBy(_.population)(Ordering.Double.reverse).head.pos.dto)
              case ItemType.TORNADO ⇒
              case ItemType.VOLCANO ⇒
                sender() ! CastVolcanoMsg(enemyBuildings.sortBy(_.population)(Ordering.Double.reverse).head.pos.dto)
              case ItemType.STRENGTHENING ⇒
                sender() ! CastStrengtheningMsg(myBuildings.sortBy(_.population)(Ordering.Double.reverse).head.id.dto)
              case ItemType.ASSISTANCE ⇒
                sender() ! CastAssistanceMsg(myBuildings.sortBy(_.population).head.id.dto)
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
    (distances.sum / myBuildings.size) / (cellDiagonal * mapDiagonal) // from 0 to 1
  }

  def ownerWeight(owner: Option[PlayerId]) = if (owner.isDefined) 0.3 else 0.0

  def populationWeight(population: Double) = population * 3 / config.maxPopulation

  def strengthenedWeight(strengthened: Boolean) = if (strengthened) 0.3 else 0.0
}
