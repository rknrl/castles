package ru.rknrl.castles.game

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.{GameOver, Leaved}
import ru.rknrl.castles.account.LeaveGame
import ru.rknrl.castles.game.Game.{Join, Offline, StopGame}
import ru.rknrl.castles.game.objects.buildings.BuildingId
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
 *
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
class Game(players: Map[PlayerId, Player],
           big: Boolean,
           config: GameConfig,
           matchmaking: ActorRef) extends Actor {

  private val playersList = for ((id, player) ← players) yield player

  private def playerInfosMock =
    for ((id, player) ← players)
    yield PlayerInfoDTO.newBuilder()
      .setId(id.dto)
      .setInfo(player.userInfo)
      .build

  private val `accountId→playerId` =
    for ((playerId, player) ← players)
    yield player.accountId → playerId

  private val `playerId→accountId` =
    for ((playerId, player) ← players)
    yield playerId → player.accountId

  /**
   * Игроки, с которыми в данный момент установлено соединение
   * Добавление происходит на сообщение JoinMsg
   * Удаление происходит при потере коннекта, на сообщение Offline
   */
  private var online = Set[PlayerId]()

  private var playerStates =
    for ((playerId, player) ← players)
    yield playerId → PlayerState.GAME

  /**
   * Список игроков, которые завершили игру, их места и награды
   */
  private var gameOvers = List[GameOverDTO]()

  private def getReward(playerId: PlayerId) =
    gameOvers.find(_.getPlayerId.getId == playerId.id).get.getReward

  private var `playerId→account` = Map[PlayerId, ActorRef]()

  private var `playerId→enterGameRmi` = Map[PlayerId, ActorRef]()

  private var `enterGameRmi→playerId` = Map[ActorRef, PlayerId]()

  private var `playerId→gameRmi` = Map[PlayerId, ActorRef]()

  private var `gameRmi→playerId` = Map[ActorRef, PlayerId]()

  /**
   * Игроки еще не завершившие игру
   */
  private def playersInGame =
    for ((playerId, playerState) ← playerStates
         if playerState == PlayerState.GAME)
    yield playerId

  private def getPlace =
    players.size - playersInGame.size

  /**
   * Все вышли из игры
   */
  private def allLeaved =
    playerStates.count { case (playerId, playerState) ⇒ playerState != PlayerState.LEAVED} == 0

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

  private var gameState = GameState.init(System.currentTimeMillis(), playersList.toList, big, config)

  private var moveActions = Map[PlayerId, MoveDTO]()
  private var fireballCasts = Map[PlayerId, PointDTO]()
  private var strengtheningCasts = Map[PlayerId, BuildingId]()
  private var volcanoCasts = Map[PlayerId, PointDTO]()
  private var tornadoCasts = Map[PlayerId, CastTorandoDTO]()
  private var assistanceCasts = Map[PlayerId, BuildingId]()

  private def clearMaps(): Unit = {
    moveActions = Map.empty
    fireballCasts = Map.empty
    strengtheningCasts = Map.empty
    volcanoCasts = Map.empty
    tornadoCasts = Map.empty
    assistanceCasts = Map.empty
  }

  private def updateGameState = {
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

  private def canSendMessage(playerId: PlayerId) =
    (online contains playerId) &&
      (playerStates(playerId) != PlayerState.LEAVED) &&
      !players(playerId).isBot

  private def sendToPlayers(messages: Iterable[Msg], personalMessages: Iterable[PersonalMessage]): Unit =
      for((playerId, gameRmi) ← `playerId→gameRmi`
          if canSendMessage(playerId)) {
        val all = messages ++ personalMessages.filter(_.playerId == playerId).map(_.msg)
        gameRmi ! all
      }

  /**
   * Отправить игрокам-ботам геймстейт
   */
  private def sendGameStateToBots() =
    for ((playerId, ref) ← `playerId→gameRmi`
         if players(playerId).isBot)
      ref ! gameState

  private def getPlayerId = `gameRmi→playerId`(sender)

  private def assertCanPlay() =
    assert(playerStates(getPlayerId) == PlayerState.GAME)

  override def receive = {
    /**
     * Аккаунт присоединяется к игре и сообщает о рефах куда слать игровые сообщения
     * Добавляем/Обнавляем рефы в мапах
     */
    case Join(accountId, enterGameRmiRef, gameRmiRef) ⇒
      val playerId = `accountId→playerId`(accountId)
      `playerId→account` = `playerId→account` + (playerId → sender)

      `playerId→enterGameRmi` = `playerId→enterGameRmi` + (playerId → enterGameRmiRef)
      `enterGameRmi→playerId` = `enterGameRmi→playerId` + (enterGameRmiRef → playerId)

      `playerId→gameRmi` = `playerId→gameRmi` + (playerId → gameRmiRef)
      `gameRmi→playerId` = `gameRmi→playerId` + (gameRmiRef → playerId)

    /**
     * Аккаунт говорит, что потеряли связь с игроком
     * Убираем из его мапы online
     */
    case Offline(accountId) ⇒
      val playerId = `accountId→playerId`(accountId)
      online = online - playerId

    /**
     * Игрок входит в бой
     * Кладем его в мапу online
     * и отправляем стартовое сообщение JoinGameMsg
     */
    case JoinMsg() ⇒
      val playerId = `enterGameRmi→playerId`(sender)
      online = online + playerId
      val builder = gameState.dtoBuilder(playerId)
      val dto = builder
        .addAllPlayerInfos(playerInfosMock.asJava)
        .addAllGameOvers(gameOvers.asJava)
        .build
      sender ! JoinGameMsg(dto)

    /**
     * Игрок сдается
     */
    case SurrenderMsg() ⇒
      assert(playerStates(getPlayerId) == PlayerState.GAME)
      addLoser(getPlayerId, getPlace)

    /**
     * Игрок окончательно выходит из боя (нажал leave в GameOverScreen)
     */
    case LeaveMsg() ⇒
      assert(playerStates(getPlayerId) == PlayerState.GAME_OVER)
      addLeaved(getPlayerId)

    /**
     * Matchmaking завершает игру (Ответ на GameOver)
     */
    case StopGame ⇒
      context stop self

    /**
     * Scheduler говорит, что пора обновить game state и отправить игрокам
     */
    case UpdateGameState ⇒
      val (newGameState, messages, personalMessages) = updateGameState
      gameState = newGameState

      sendToPlayers(messages, personalMessages)
      sendGameStateToBots()

      val newLosers = getNewLosers
      val place = getPlace
      for (playerId ← newLosers) addLoser(playerId, place)

    /**
     * Игрок отправляет отряд из домика в домик - кладем в мапу
     */
    case MoveMsg(dto: MoveDTO) ⇒
      assertCanPlay()
      moveActions = moveActions + (getPlayerId → dto)

    /**
     * Игрок кастует "Фаерболл" - кладем в мапу
     */
    case CastFireballMsg(point: PointDTO) ⇒
      assertCanPlay()
      fireballCasts = fireballCasts + (getPlayerId → point)

    /**
     * Игрок кастует "Усиление" - кладем в мапу
     */
    case CastStrengtheningMsg(buildingId: BuildingIdDTO) ⇒
      assertCanPlay()
      strengtheningCasts = strengtheningCasts + (getPlayerId → new BuildingId(buildingId.getId))

    /**
     * Игрок кастует "Вулкан" - кладем в мапу
     */
    case CastVolcanoMsg(point: PointDTO) ⇒
      assertCanPlay()
      volcanoCasts = volcanoCasts + (getPlayerId → point)

    /**
     * Игрок кастует "Торнадо" - кладем в мапу
     */
    case CastTornadoMsg(dto: CastTorandoDTO) ⇒
      assertCanPlay()
      tornadoCasts = tornadoCasts + (getPlayerId → dto)

    /**
     * Игрок кастует "Призыв" - кладем в мапу
     */
    case CastAssistanceMsg(buildingId: BuildingIdDTO) ⇒
      assertCanPlay()
      assistanceCasts = assistanceCasts + (getPlayerId → new BuildingId(buildingId.getId))
  }

  private def addLoser(playerId: PlayerId, place: Int) {
    playerStates = playerStates.updated(playerId, PlayerState.GAME_OVER)

    val dto = getLoseDto(playerId, place)
    gameOvers = gameOvers :+ dto
    sendToPlayers(List(GameOverMsg(dto)), List.empty)

    // Если в бою остался один игрок - объявляем его победителем

    val winnerId = if (playersInGame.size == 1) Some(playersInGame.head) else None

    if (winnerId.isDefined) {
      playerStates = playerStates.updated(winnerId.get, PlayerState.GAME_OVER)
      val winDto = getWinDto(winnerId.get)
      gameOvers = gameOvers :+ winDto
      sendToPlayers(List(GameOverMsg(winDto)), List.empty)
      scheduler.cancel()
    }
  }

  private def getWinDto(playerId: PlayerId) =
    GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(1)
      .setReward(config.winReward)
      .build()

  private def getLoseDto(playerId: PlayerId, place: Int) =
    GameOverDTO.newBuilder()
      .setPlayerId(playerId.dto)
      .setPlace(place)
      .setReward(0)
      .build()

  private def addLeaved(playerId: PlayerId) {
    playerStates = playerStates.updated(playerId, PlayerState.LEAVED)

    // Говорим матчмейкингу, что игрок вышел

    val accountId = `playerId→accountId`(playerId)
    matchmaking ! Leaved(accountId)

    // Ответ самому игроку

    `playerId→enterGameRmi`(playerId) ! LeaveGameMsg()

    // Говорим аккаунту

    `playerId→account`(playerId) ! LeaveGame(gameState.gameItems.states(playerId).usedItems, getReward(playerId))

    // Если вышли все - завершаем игру

    if (allLeaved) matchmaking ! GameOver
  }

  override def preStart(): Unit = println("Game start")

  override def postStop(): Unit = println("Game stop")
}