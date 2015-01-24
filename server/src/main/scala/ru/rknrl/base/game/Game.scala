package ru.rknrl.base.game

import akka.actor.{ActorLogging, ActorRef}
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.base.MatchMaking.{AllPlayersLeaveGame, PlayerLeaveGame}
import ru.rknrl.base.game.Game.{Join, Offline, StopGame}
import ru.rknrl.castles.game.GameState
import ru.rknrl.castles.game.objects.players.{Player, PlayerId}
import ru.rknrl.castles.rmi._
import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto.GameDTO._

import scala.collection.JavaConverters._
import scala.concurrent.duration._

object Game {

  case class Join(accountId: AccountId, enterGameRef: ActorRef, gameRef: ActorRef)

  case class Offline(accountId: AccountId)

  case object StopGame

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
 *
 */
abstract class Game(players: Map[PlayerId, Player],
                    matchmaking: ActorRef,
                    winReward: Int) extends EscalateStrategyActor with ActorLogging {

  protected val playersList = for ((id, player) ← players) yield player

  private val `accountId→playerId` =
    for ((playerId, player) ← players)
    yield player.accountId → playerId

  private val `playerId→accountId` =
    for ((playerId, player) ← players)
    yield playerId → player.accountId

  /** Игроки, с которыми в данный момент установлено соединение
    * Добавление происходит на сообщение JoinMsg
    * Удаление происходит при потере коннекта, на сообщение Offline
    */
  private var online = Set[PlayerId]()

  private var playerStates =
    for ((playerId, player) ← players)
    yield playerId → PlayerState.GAME

  /** Игроки, которые завершили игру и их места */
  private var gameOvers = Map[PlayerId, Int]()

  private var `playerId→account` = Map[PlayerId, ActorRef]()

  private var `playerId→enterGameRmi` = Map[PlayerId, ActorRef]()

  private var `enterGameRmi→playerId` = Map[ActorRef, PlayerId]()

  private var `playerId→gameRmi` = Map[PlayerId, ActorRef]()

  private var `gameRmi→playerId` = Map[ActorRef, PlayerId]()

  /** Игроки еще не завершившие игру */
  private def playersInGame =
    for ((playerId, playerState) ← playerStates
         if playerState == PlayerState.GAME)
    yield playerId

  private def getPlace = playersInGame.size

  /** Все вышли из игры */
  private def allLeaved =
    playerStates.count { case (playerId, playerState) ⇒ playerState != PlayerState.LEAVED} == 0

  private def playerInfos =
    for ((id, player) ← players)
    yield PlayerInfoDTO.newBuilder()
      .setId(id.dto)
      .setInfo(player.userInfo)
      .build

  private def placeToReward(place: Int) =
    if (place == 1) winReward else 0

  private def getReward(playerId: PlayerId) =
    placeToReward(gameOvers(playerId))

  private def getNewLosers =
    for ((playerId, playerState) ← playerStates
         if playerState == PlayerState.GAME
         if gameState.isPlayerLose(playerId))
    yield playerId

  private val sendFps = 30
  private val sendInterval = 1000 / sendFps

  case object UpdateGameState

  import context.dispatcher

  private val scheduler = context.system.scheduler.schedule(0 seconds, sendInterval milliseconds, self, UpdateGameState)

  private def canSendMessage(playerId: PlayerId) =
    (online contains playerId) &&
      (playerStates(playerId) != PlayerState.LEAVED) &&
      !players(playerId).isBot

  private def sendToPlayers(messages: Iterable[Msg], personalMessages: Iterable[PersonalMessage]): Unit =
    for ((playerId, gameRmi) ← `playerId→gameRmi`
         if canSendMessage(playerId)) {
      val all = messages ++ personalMessages.filter(_.playerId == playerId).map(_.msg)
      gameRmi ! all
    }

  /** Отправить игрокам-ботам геймстейт */
  private def sendGameStateToBots() =
    for ((playerId, ref) ← `playerId→gameRmi`
         if players(playerId).isBot)
      ref ! gameState

  protected def senderPlayerId = `gameRmi→playerId`(sender)

  protected def senderCanPlay = playerStates(senderPlayerId) == PlayerState.GAME

  protected var gameState: GameState

  protected def updateGameState: (GameState, Iterable[Msg], Iterable[PersonalMessage])

  override def receive = {
    /** Аккаунт присоединяется к игре и сообщает о рефах куда слать игровые сообщения
      * Добавляем/Обнавляем рефы в мапах
      */
    case Join(accountId, enterGameRmiRef, gameRmiRef) ⇒
      val playerId = `accountId→playerId`(accountId)
      `playerId→account` = `playerId→account` + (playerId → sender)

      `playerId→enterGameRmi` = `playerId→enterGameRmi` + (playerId → enterGameRmiRef)
      `enterGameRmi→playerId` = `enterGameRmi→playerId` + (enterGameRmiRef → playerId)

      `playerId→gameRmi` = `playerId→gameRmi` + (playerId → gameRmiRef)
      `gameRmi→playerId` = `gameRmi→playerId` + (gameRmiRef → playerId)

    /** Аккаунт говорит, что потеряли связь с игроком
      * Убираем из его мапы online
      */
    case Offline(accountId) ⇒
      val playerId = `accountId→playerId`(accountId)
      online = online - playerId

    /** Игрок входит в бой
      * Кладем его в мапу online
      * и отправляем стартовое сообщение JoinGameMsg
      */
    case JoinMsg() ⇒
      val playerId = `enterGameRmi→playerId`(sender)
      online = online + playerId
      val builder = gameState.dtoBuilder(playerId)
      val dto = builder
        .addAllPlayerInfos(playerInfos.asJava)
        .addAllGameOvers(gameOverDto.asJava)
        .build
      sender ! JoinGameMsg(dto)

    /** Игрок сдается */
    case SurrenderMsg() ⇒
      assert(playerStates(senderPlayerId) == PlayerState.GAME)
      addLoser(senderPlayerId, getPlace)

    /** Игрок окончательно выходит из боя (нажал leave в GameOverScreen) */
    case LeaveMsg() ⇒
      assert(playerStates(senderPlayerId) == PlayerState.GAME_OVER)
      addLeaved(senderPlayerId)

    /** Matchmaking завершает игру (Ответ на AllPlayersLeaveGame) */
    case StopGame ⇒
      context stop self

    /** Scheduler говорит, что пора обновить game state и отправить игрокам */
    case UpdateGameState ⇒
      val (newGameState, messages, personalMessages) = updateGameState
      gameState = newGameState

      sendToPlayers(messages, personalMessages)
      sendGameStateToBots()

      val newLosers = getNewLosers
      val place = getPlace
      for (playerId ← newLosers) addLoser(playerId, place)
  }

  private def addLoser(playerId: PlayerId, place: Int) {
    playerStates = playerStates.updated(playerId, PlayerState.GAME_OVER)

    val dto = getLoseDto(playerId, place)
    gameOvers = gameOvers + (playerId → place)
    sendToPlayers(List(GameOverMsg(dto)), List.empty)

    // Если в бою остался один игрок - объявляем его победителем

    val winnerId = if (playersInGame.size == 1) Some(playersInGame.head) else None

    if (winnerId.isDefined) {
      playerStates = playerStates.updated(winnerId.get, PlayerState.GAME_OVER)
      val winDto = getWinDto(winnerId.get)
      gameOvers = gameOvers + (winnerId.get → 1)
      sendToPlayers(List(GameOverMsg(winDto)), List.empty)
      scheduler.cancel()
    }
  }

  private def getWinDto(playerId: PlayerId) =
    GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(1)
      .setReward(winReward)
      .build()

  private def getLoseDto(playerId: PlayerId, place: Int) =
    GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(place)
      .setReward(0)
      .build()

  private def gameOverDto =
    for ((playerId, place) ← gameOvers)
    yield GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(place)
      .setReward(placeToReward(place))
      .build()

  private def addLeaved(playerId: PlayerId) {
    playerStates = playerStates.updated(playerId, PlayerState.LEAVED)

    // Говорим матчмейкингу, что игрок вышел

    val accountId = `playerId→accountId`(playerId)
    val place = gameOvers(playerId)
    matchmaking ! PlayerLeaveGame(accountId, place = place, reward = placeToReward(place), usedItems = gameState.gameItems.states(playerId).usedItems)

    // Если вышли все - завершаем игру

    if (allLeaved) matchmaking ! AllPlayersLeaveGame
  }

  override def preStart(): Unit = log.info("Game start")

  override def postStop(): Unit = log.info("Game stop")
}
