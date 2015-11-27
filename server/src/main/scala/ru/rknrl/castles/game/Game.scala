//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.{Actor, ActorRef, Props}
import protos._
import ru.rknrl.Supervisor._
import ru.rknrl.castles.game.Game.{Join, UpdateGameState}
import ru.rknrl.castles.game.state.{GameState, GameStateDiff}
import ru.rknrl.castles.matchmaking.MatchMaking.{AllPlayersLeaveGame, ConnectToGame, Offline, PlayerLeaveGame}
import ru.rknrl.logging.ActorLog

trait GameMsg

object Game {

  case class Join(accountId: AccountId, client: ActorRef)

  case class UpdateGameState(newTime: Long)

  def props(gameState: GameState,
            isDev: Boolean,
            isTutor: Boolean,
            botFactory: IBotFactory,
            schedulerClass: Class[_],
            matchmaking: ActorRef) =
    Props(classOf[Game], gameState, isDev, isTutor, botFactory, schedulerClass, matchmaking)
}

class Game(var gameState: GameState,
           isDev: Boolean,
           isTutor: Boolean,
           botFactory: IBotFactory,
           schedulerClass: Class[_],
           matchmaking: ActorRef) extends Actor with ActorLog {

  override def supervisorStrategy = StopStrategy

  for ((playerId, player) ← gameState.players if player.isBot) {
    val bot = botFactory.create(player.accountId, isTutor)
    send(bot, ConnectToGame(self))
  }

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

  var moveActions = Map.empty[PlayerId, protos.Move]
  var fireballCasts = Map.empty[PlayerId, PointDTO]
  var strengtheningCasts = Map.empty[PlayerId, BuildingId]
  var volcanoCasts = Map.empty[PlayerId, PointDTO]
  var tornadoCasts = Map.empty[PlayerId, CastTornado]
  var assistanceCasts = Map.empty[PlayerId, BuildingId]

  var gameOvers = Map.empty[PlayerId, GameOver]
  var leaved = Set.empty[PlayerId]

  def sendToPlayers(msg: Any): Unit =
    for ((playerId, client) ← playerIdToClient
         if !(leaved contains playerId))
      client ! msg

  def sendToBots(msg: Any): Unit =
    for ((playerId, client) ← playerIdToClient
         if gameState.players(playerId).isBot
         if !(leaved contains playerId))
      send(client, msg)

  override val logFilter: Any ⇒ Boolean = {
    case _: UpdateGameState ⇒ false
    case _ ⇒ true
  }

  val scheduler = context.actorOf(Props(schedulerClass, self))

  def receive = logged {
    case Join(accountId, client) ⇒
      val playerId = accountIdToPlayerId(accountId)
      playerIdToClient = playerIdToClient + (playerId → client)
      send(client, gameState.dto(playerId, gameOvers.values.toSeq))

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

      sendToPlayers(gameStateUpdate)

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

    case Surrender() ⇒
      if (isDev && senderCanPlay) addLoser(senderPlayerId)

    case LeaveGame() ⇒
      if (clientToPlayerId(sender).isDefined &&
        (gameOvers contains senderPlayerId) &&
        !(leaved contains senderPlayerId)) leave(senderPlayerId)

    case moveDto: protos.Move ⇒
      if (senderCanPlay) moveActions = moveActions + (senderPlayerId → moveDto)

    case CastFireball(pointDto) ⇒
      if (senderCanPlay) fireballCasts = fireballCasts + (senderPlayerId → pointDto)

    case CastStrengthening(buildingId) ⇒
      if (senderCanPlay) strengtheningCasts = strengtheningCasts + (senderPlayerId → buildingId)

    case CastVolcano(pointDto) ⇒
      if (senderCanPlay) volcanoCasts = volcanoCasts + (senderPlayerId → pointDto)

    case castTornadoDto: CastTornado ⇒
      if (senderCanPlay) tornadoCasts = tornadoCasts + (senderPlayerId → castTornadoDto)

    case CastAssistance(buildingId) ⇒
      if (senderCanPlay) assistanceCasts = assistanceCasts + (senderPlayerId → buildingId)

    case statAction: StatAction ⇒
      sendToBots(statAction)
  }

  def addLoser(playerId: PlayerId): Unit = {
    val place = playersIds.size - gameOvers.size
    val reward = placeToReward(place)
    val gameOver = GameOver(playerId = playerId, place = place, reward = reward)
    gameOvers = gameOvers + (playerId → gameOver)

    sendToPlayers(gameOver)

    if (gameOvers.size == playersIds.size - 1) {
      val winnerId = playersIds.find(id ⇒ !gameOvers.contains(id)).get
      addLoser(winnerId)

      context stop scheduler
    }
  }

  def humansLeft =
    for ((playerId, player) ← gameState.players
         if !player.isBot
         if !(leaved contains playerId))
      yield playerId

  def placeToReward(place: Int) = if (place == 1) 2 else 0

  def leave(playerId: PlayerId): Unit = {
    leaved = leaved + playerId

    val gameOver = gameOvers(playerId)
    val player = gameState.players(playerId)

    if (!player.isBot)
      send(matchmaking, PlayerLeaveGame(
        accountId = player.accountId,
        place = gameOver.place,
        reward = gameOver.reward,
        usedItems = gameState.items.states(playerId).usedItems
      ))

    if (humansLeft.size == 0)
      send(matchmaking, AllPlayersLeaveGame(self))
  }
}
