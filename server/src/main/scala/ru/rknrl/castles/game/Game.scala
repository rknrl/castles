//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.game.Game.{Join, UpdateGameState}
import ru.rknrl.castles.game.state.{GameState, GameStateDiff}
import ru.rknrl.castles.matchmaking.NewMatchmaking.{PlayerLeaveGame, AllPlayersLeaveGame, Offline}
import ru.rknrl.castles.rmi.B2C.{GameOver, GameStateUpdated, JoinedGame}
import ru.rknrl.castles.rmi.C2B
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.dto._
import ru.rknrl.{BugType, Logged, SilentLog}

object Game {

  case class Join(accountId: AccountId, client: ActorRef)

  case class UpdateGameState(newTime: Long)

}

class Game(var gameState: GameState,
           isDev: Boolean,
           schedulerClass: Class[_],
           matchmaking: ActorRef,
           bugs: ActorRef) extends Actor {

  val playersIds = gameState.players.keys

  def accountIdToPlayerId(accountId: AccountId) =
    gameState.players.values.find(p ⇒ p.accountId == accountId).get.id

  var playerIdToClient = Map.empty[PlayerId, ActorRef]

  def clientToPlayerId(ref: ActorRef) =
    playerIdToClient.find { case (playerId, client) ⇒ client == ref }

  def senderPlayerId = clientToPlayerId(sender).get._1

  def senderCanPlay =
    clientToPlayerId(sender).isDefined &&
      !(gameOvers contains senderPlayerId)

  var moveActions = Map.empty[PlayerId, MoveDTO]
  var fireballCasts = Map.empty[PlayerId, PointDTO]
  var strengtheningCasts = Map.empty[PlayerId, BuildingId]
  var volcanoCasts = Map.empty[PlayerId, PointDTO]
  var tornadoCasts = Map.empty[PlayerId, CastTornadoDTO]
  var assistanceCasts = Map.empty[PlayerId, BuildingId]

  var gameOvers = Map.empty[PlayerId, GameOverDTO]
  var leaved = Set.empty[PlayerId]

  def sendToPlayers(msg: Any): Unit =
    for ((playerId, client) ← playerIdToClient
         if !gameState.players(playerId).isBot
         if !(leaved contains playerId))
      client ! msg

  def sendToBots(msg: Any): Unit =
    for ((playerId, client) ← playerIdToClient
         if gameState.players(playerId).isBot
         if !(leaved contains playerId))
      client ! msg

  val log = new SilentLog

  def logged(r: Receive) = new Logged(r, log, Some(bugs), Some(BugType.GAME), {
    case UpdateGameState ⇒ false
    case _ ⇒ true
  })

  val scheduler = context.actorOf(Props(schedulerClass, self))

  def receive = logged({
    case Join(accountId, client) ⇒
      val playerId = accountIdToPlayerId(accountId)
      playerIdToClient = playerIdToClient + (playerId → client)
      client ! JoinedGame(gameState.dto(playerId, gameOvers.values.toSeq))

    case Offline(accountId, client) ⇒
      val playerId = accountIdToPlayerId(accountId)
      if ((playerIdToClient contains playerId) &&
        playerIdToClient(playerId) == client)
        playerIdToClient = playerIdToClient - playerId

    case UpdateGameState(newTime) ⇒
      val newGameState = gameState.update(
        newTime = newTime,
        moveActions = moveActions,
        fireballCasts = fireballCasts,
        volcanoCasts = volcanoCasts,
        tornadoCasts = tornadoCasts,
        strengtheningCasts = strengtheningCasts,
        assistanceCasts = assistanceCasts
      )

      val gameStateUpdate = GameStateDiff.diff(gameState, newGameState)

      sendToPlayers(GameStateUpdated(gameStateUpdate))
      sendToBots(newGameState)

      gameState = newGameState

      moveActions = Map.empty
      fireballCasts = Map.empty
      strengtheningCasts = Map.empty
      volcanoCasts = Map.empty
      tornadoCasts = Map.empty
      assistanceCasts = Map.empty

      val newLosers = playersIds
        .filter(gameState.isPlayerLose)
        .filterNot(gameOvers.contains)

      newLosers.foreach(addLoser)

    case Surrender ⇒
      if (isDev && senderCanPlay) addLoser(senderPlayerId)

    case C2B.LeaveGame ⇒
      if (clientToPlayerId(sender).isDefined &&
        (gameOvers contains senderPlayerId) &&
        !(leaved contains senderPlayerId)) leave(senderPlayerId)

    case Move(moveDto) ⇒
      if (senderCanPlay) moveActions = moveActions + (senderPlayerId → moveDto)

    case CastFireball(pointDto) ⇒
      if (senderCanPlay) fireballCasts = fireballCasts + (senderPlayerId → pointDto)

    case CastStrengthening(buildingId) ⇒
      if (senderCanPlay) strengtheningCasts = strengtheningCasts + (senderPlayerId → buildingId)

    case CastVolcano(pointDto) ⇒
      if (senderCanPlay) volcanoCasts = volcanoCasts + (senderPlayerId → pointDto)

    case CastTornado(castTornadoDto) ⇒
      if (senderCanPlay) tornadoCasts = tornadoCasts + (senderPlayerId → castTornadoDto)

    case CastAssistance(buildingId) ⇒
      if (senderCanPlay) assistanceCasts = assistanceCasts + (senderPlayerId → buildingId)

    case statAction: StatAction ⇒
      sendToBots(statAction)
  })

  def addLoser(playerId: PlayerId): Unit = {
    val place = playersIds.size - gameOvers.size
    val reward = placeToReward(place)
    val gameOver = GameOverDTO(playerId = playerId, place = place, reward = reward)
    gameOvers = gameOvers + (playerId → gameOver)

    sendToPlayers(GameOver(gameOver))

    if (gameOvers.size == playersIds.size - 1) {
      val winnerId = playersIds.find(id ⇒ !gameOvers.contains(id)).get
      addLoser(winnerId)

      context stop scheduler
    }
  }

  def placeToReward(place: Int) = if (place == 1) 2 else 0

  def leave(playerId: PlayerId): Unit = {
    leaved = leaved + playerId

    val gameOver = gameOvers(playerId)
    val player = gameState.players(playerId)

    matchmaking ! PlayerLeaveGame(
      accountId = player.accountId,
      place = gameOver.place,
      reward = gameOver.reward,
      usedItems = gameState.items.states(playerId).usedItems
    )

    if (leaved.size == playersIds.size)
      matchmaking ! AllPlayersLeaveGame(self)
  }
}
