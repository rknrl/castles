//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import akka.actor.{ActorLogging, ActorRef}
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.{AllPlayersLeaveGame, Offline, PlayerLeaveGame}
import ru.rknrl.castles.game.Game.Join
import ru.rknrl.castles.game.state.GameState
import ru.rknrl.castles.game.state.buildings.BuildingId
import ru.rknrl.castles.game.state.players.{Player, PlayerId}
import ru.rknrl.castles.rmi.B2C.GameOver
import ru.rknrl.castles.rmi.C2B._
import ru.rknrl.castles.rmi.{B2C, C2B}
import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto.GameDTO._

import scala.collection.JavaConverters._
import scala.concurrent.duration._

object Game {

  case class Join(accountId: AccountId, client: ActorRef)

}

object PlayerState extends Enumeration {
  type PlayerState = Value

  /**
   * Игрок может играть
   * при переконнекте должен попасть опять в это состояние
   * если игрок сдается или при проигрыше или выигрыше(он остался один в состоянии game) становимся gameOver
   */
  val GAME = Value

  /**
   * Игрок закончил играть, видит GameOverScreen, он больше не может играть, но видит, что происходит в игре
   * при переконнекте должен попасть опять в это состояние
   * по нажатию кнопки Leave переходит в leaved
   */
  val GAME_OVER = Value

  /**
   * Игрок окончательно вышел из игры и не получает никаких сообщений
   * переконнекта нет
   *
   * Игра будет висеть вечно, в ожидании пока все игроки не сделают leave
   */
  val LEAVED = Value
}

/**
 * Online/Offline:
 *
 * Игрок может терять соединение, в этом случае к нам приходит сообщение Offline
 * Когда игрок снова подконнектится он сделает Join
 *
 * Finish game:
 *
 * Когда все игроки перешли в состояние leaved отправляем Matchmaking сообщение GameOver, а он нам в ответ StopGame, удаляем актор игры
 */
class Game(players: Map[PlayerId, Player],
           big: Boolean,
           isTutor: Boolean,
           config: GameConfig,
           matchmaking: ActorRef) extends EscalateStrategyActor with ActorLogging {

  val playersList = for ((id, player) ← players) yield player

  val `accountId→playerId` =
    for ((playerId, player) ← players)
    yield player.accountId → playerId

  val `playerId→accountId` =
    for ((playerId, player) ← players)
    yield playerId → player.accountId

  /** Игроки, с которыми в данный момент установлено соединение
    * Добавление происходит на сообщение JoinMsg
    * Удаление происходит при потере коннекта, на сообщение Offline
    */
  var online = Set[PlayerId]()

  var playerStates =
    for ((playerId, player) ← players)
    yield playerId → PlayerState.GAME

  /** Игроки, которые завершили игру и их места */
  var gameOvers = Map[PlayerId, Int]()

  var `playerId→account` = Map[PlayerId, ActorRef]()

  var `playerId→client` = Map[PlayerId, ActorRef]()

  var `client→playerId` = Map[ActorRef, PlayerId]()

  /** Игроки еще не завершившие игру */
  def playersInGame =
    for ((playerId, playerState) ← playerStates
         if playerState == PlayerState.GAME)
    yield playerId

  def getPlace = playersInGame.size

  /** Все люди вышли из игры */
  def allLeaved =
    playerStates.count { case (playerId, playerState) ⇒ !players(playerId).isBot && playerState != PlayerState.LEAVED} == 0

  def playersDto =
    for ((id, player) ← players)
    yield PlayerDTO.newBuilder()
      .setId(id.dto)
      .setInfo(player.userInfo)
      .build

  def placeToReward(place: Int) =
    if (place == 1) config.winReward else 0

  def getReward(playerId: PlayerId) =
    placeToReward(gameOvers(playerId))

  def getNewLosers =
    for ((playerId, playerState) ← playerStates
         if playerState == PlayerState.GAME
         if gameState.isPlayerLose(playerId))
    yield playerId

  val sendFps = 30
  val sendInterval = 1000 / sendFps

  case object UpdateGameState

  import context.dispatcher

  val scheduler = context.system.scheduler.schedule(0 seconds, sendInterval milliseconds, self, UpdateGameState)

  def canSendMessage(playerId: PlayerId) =
    (online contains playerId) &&
      (playerStates(playerId) != PlayerState.LEAVED) &&
      !players(playerId).isBot

  def sendToPlayers(messages: Iterable[Msg], personalMessages: Iterable[PersonalMessage]): Unit =
    for ((playerId, client) ← `playerId→client`
         if canSendMessage(playerId)) {
      val all = messages ++ personalMessages.filter(_.playerId == playerId).map(_.msg)
      client ! all
    }

  def sendGameStateToBots() = sendToBots(gameState)

  def sendToBots(msg: Any) =
    for ((playerId, ref) ← `playerId→client`
         if players(playerId).isBot)
      ref ! msg

  def senderPlayerId = `client→playerId`(sender)

  def senderCanPlay = playerStates(senderPlayerId) == PlayerState.GAME

  var gameState = GameState.init(System.currentTimeMillis(), playersList.toList, big, isTutor, config)

  var moveActions = Map[PlayerId, MoveDTO]()
  var fireballCasts = Map[PlayerId, PointDTO]()
  var strengtheningCasts = Map[PlayerId, BuildingId]()
  var volcanoCasts = Map[PlayerId, PointDTO]()
  var tornadoCasts = Map[PlayerId, CastTorandoDTO]()
  var assistanceCasts = Map[PlayerId, BuildingId]()

  def clearMaps(): Unit = {
    moveActions = Map.empty
    fireballCasts = Map.empty
    strengtheningCasts = Map.empty
    volcanoCasts = Map.empty
    tornadoCasts = Map.empty
    assistanceCasts = Map.empty
  }

  def updateGameState = {
    val time = System.currentTimeMillis()

    val (newGameState, messages, personalMessages) = gameState.update(
      time,
      moveActions,
      fireballCasts,
      strengtheningCasts,
      volcanoCasts,
      tornadoCasts,
      assistanceCasts
    )

    clearMaps()

    (newGameState, messages, personalMessages)
  }

  override def receive = {
    /** Аккаунт присоединяется к игре и сообщает о рефах куда слать игровые сообщения
      * Добавляем/Обнавляем рефы в мапах
      */
    case Join(accountId, client) ⇒
      log.debug("Join")
      val playerId = `accountId→playerId`(accountId)
      `playerId→account` = `playerId→account` + (playerId → sender)

      `playerId→client` = `playerId→client` + (playerId → client)
      `client→playerId` = `client→playerId` + (client → playerId)

    /** Аккаунт говорит, что потеряли связь с игроком
      * Убираем из его мапы online
      */
    case Offline(accountId) ⇒
      log.debug("Offline")
      val playerId = `accountId→playerId`(accountId)
      online = online - playerId

      // Если это тутор и игрок отвалился, то убиваем игру.
      // При перезаходе игрока будет создана новая игра (Иначе он не поймет, что произошло)
      if(isTutor && !players(playerId).isBot) addLeaved(playerId)

    /** Игрок входит в бой
      * Кладем его в мапу online
      * и отправляем стартовое сообщение JoinGameMsg
      */
    case C2B.JoinGame ⇒
      log.debug("C2B.JoinGame")
      val playerId = `client→playerId`(sender)
      online = online + playerId
      val builder = gameState.dtoBuilder(playerId)
      val dto = builder
        .addAllPlayers(playersDto.asJava)
        .addAllGameOvers(gameOverDto.asJava)
        .build
      sender ! B2C.JoinedGame(dto)

    /** Игрок сдается */
    case Surrender ⇒
      log.debug("Surrender")
      if (playerStates(senderPlayerId) == PlayerState.GAME)
        addLoser(senderPlayerId, getPlace)

    /** Игрок окончательно выходит из боя (нажал leave в GameOverScreen) */
    case C2B.LeaveGame ⇒
      log.debug("C2B.LeaveGame")
      if (playerStates(senderPlayerId) == PlayerState.GAME_OVER)
        addLeaved(senderPlayerId)

    /** Scheduler говорит, что пора обновить game state и отправить игрокам */
    case UpdateGameState ⇒
      val (newGameState, messages, personalMessages) = updateGameState
      gameState = newGameState

      sendToPlayers(messages, personalMessages)
      sendGameStateToBots()

      val newLosers = getNewLosers
      val place = getPlace
      for (playerId ← newLosers) addLoser(playerId, place)


    case Move(dto: MoveDTO) ⇒
      if (senderCanPlay) moveActions = moveActions + (senderPlayerId → dto)

    case CastFireball(point: PointDTO) ⇒
      if (senderCanPlay) fireballCasts = fireballCasts + (senderPlayerId → point)

    case CastStrengthening(buildingId: BuildingIdDTO) ⇒
      if (senderCanPlay) strengtheningCasts = strengtheningCasts + (senderPlayerId → new BuildingId(buildingId.getId))

    case CastVolcano(point: PointDTO) ⇒
      if (senderCanPlay) volcanoCasts = volcanoCasts + (senderPlayerId → point)

    case CastTornado(dto: CastTorandoDTO) ⇒
      if (senderCanPlay) tornadoCasts = tornadoCasts + (senderPlayerId → dto)

    case CastAssistance(buildingId: BuildingIdDTO) ⇒
      if (senderCanPlay) assistanceCasts = assistanceCasts + (senderPlayerId → new BuildingId(buildingId.getId))

    case StartTutorGame ⇒
      sendToBots(StartTutorGame)
  }

  def addLoser(playerId: PlayerId, place: Int) {
    playerStates = playerStates.updated(playerId, PlayerState.GAME_OVER)

    val dto = getLoseDto(playerId, place)
    gameOvers = gameOvers + (playerId → place)
    sendToPlayers(List(GameOver(dto)), List.empty)

    // Если в бою остался один игрок - объявляем его победителем

    val winnerId = if (playersInGame.size == 1) Some(playersInGame.head) else None

    if (winnerId.isDefined) {
      playerStates = playerStates.updated(winnerId.get, PlayerState.GAME_OVER)
      val winDto = getWinDto(winnerId.get)
      gameOvers = gameOvers + (winnerId.get → 1)
      sendToPlayers(List(GameOver(winDto)), List.empty)
      scheduler.cancel()
    }
  }

  def getWinDto(playerId: PlayerId) =
    GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(1)
      .setReward(config.winReward)
      .build()

  def getLoseDto(playerId: PlayerId, place: Int) =
    GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(place)
      .setReward(0)
      .build()

  def gameOverDto =
    for ((playerId, place) ← gameOvers)
    yield GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(place)
      .setReward(placeToReward(place))
      .build()

  def addLeaved(playerId: PlayerId) {
    playerStates = playerStates.updated(playerId, PlayerState.LEAVED)

    // Говорим матчмейкингу, что игрок вышел

    val accountId = `playerId→accountId`(playerId)
    val place = gameOvers(playerId)
    matchmaking ! PlayerLeaveGame(accountId, place = place, reward = placeToReward(place), usedItems = gameState.gameItems.states(playerId).usedItems, players(playerId).userInfo)

    // Если вышли все - завершаем игру

    if (allLeaved) matchmaking ! AllPlayersLeaveGame
  }

  override def postStop() = log.debug("game stop")
}
