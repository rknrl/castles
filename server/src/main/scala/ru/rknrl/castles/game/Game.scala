package ru.rknrl.castles.game

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.{GameOver, Leaved}
import ru.rknrl.castles.account.LeaveGame
import ru.rknrl.castles.game.Game.{Join, Offline, StopGame}
import ru.rknrl.castles.game.objects.buildings.BuildingId
import ru.rknrl.castles.game.objects.players.{Player, PlayerId}
import ru.rknrl.castles.rmi._
import ru.rknrl.dto.GameDTO._

import scala.concurrent.duration._

object Game {

  case class Join(accountId: AccountId, enterGameRef: ActorRef, gameRef: ActorRef)

  case class Offline(accountId: AccountId)

  case object StopGame

}

class Game(players: Map[PlayerId, Player],
           big: Boolean,
           config: GameConfig,
           matchmaking: ActorRef) extends Actor {

  private val playersList = for ((id, player) ← players) yield player

  private def noBotCount = {
    var i = 0
    for ((id, player) ← players if !player.isBot) i += 1
    i
  }

  /**
   * Игроки, с которыми в данный момент установлено соединение
   * Удаление из сэта происходит при потере коннекта
   */
  private var online = Set[PlayerId]()

  /**
   * Игроки которые закончили игру
   * Видят GameOverScreen у себя на клиенте
   */
  private var losers = Set[PlayerId]()

  /**
   * Игроки которые вышли из игры окончательно
   */
  private var leaved = Set[PlayerId]()

  private val `accountId→playerId` =
    for ((playerId, player) ← players)
    yield player.accountId → playerId

  private val `playerId→accountId` =
    for ((playerId, player) ← players)
    yield playerId → player.accountId

  private var `playerId→account` = Map[PlayerId, ActorRef]()

  private var `playerId→enterGameRmi` = Map[PlayerId, ActorRef]()

  private var `enterGameRmi→playerId` = Map[ActorRef, PlayerId]()

  private var `playerId→gameRmi` = Map[PlayerId, ActorRef]()

  private var `gameRmi→playerId` = Map[ActorRef, PlayerId]()

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

  /**
   * Отправляем online игрокам сообщения об изменении gameState
   * Ботам отправляем просто новый gameState
   */
  private def sendMessagesToPlayers(messages: Iterable[Any]) =
    for ((playerId, ref) ← `playerId→gameRmi`
         if online contains playerId
         if !(leaved contains playerId))
      if (players(playerId).isBot)
        ref ! gameState
      else
        for (message ← messages) ref ! message

  private def sendPersonalMessagesToPlayers(personalMessages: Iterable[PersonalMessage]) =
    for (personalMessage ← personalMessages;
         playerId = personalMessage.playerId
         if online contains playerId
         if !(leaved contains playerId))
      `playerId→gameRmi`(playerId) ! personalMessage.msg

  private def getPlayerId = `gameRmi→playerId`(sender)

  private def assertCanPlay() = {
    val playerId = getPlayerId
    assert(!leaved.contains(playerId))
    assert(!losers.contains(playerId))
  }

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
      sender ! JoinGameMsg(gameState.dto(playerId))

    /**
     * Игрок сдается
     */
    case SurrenderMsg() ⇒ // todo
      sender ! GameOverMsg(GameOverDTO.newBuilder()
        .setPlayerId(getPlayerId.dto)
        .setWin(false)
        .setPlace(2)
        .setReward(0)
        .build())

    /**
     * Игрок окончательно выходит из боя (нажал leave в GameOverScreen)
     */
    case LeaveMsg() ⇒
      // Кладем игрока в мапу выбывших

      val playerId = getPlayerId
      leaved += playerId

      // Если в бою остался один игрок - объявляем его победителем

      if (onePlayerLeft) {
        sendMessagesToPlayers(List(getWinMessage))
        scheduler.cancel()
      }

      // Говорим матчмейкингу, что игрок вышел

      val accountId = `playerId→accountId`(playerId)
      matchmaking ! Leaved(accountId)

      // Ответ самому игроку

      `playerId→enterGameRmi`(playerId) ! LeaveGameMsg()

      // Говорим аккаунту

      `playerId→account`(playerId) ! LeaveGame(gameState.gameItems.states(playerId).usedItems)

      // Если вышли все - завершаем игру

      if (leaved.size == noBotCount) matchmaking ! GameOver

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

      val newLosers = getNewLosers

      val loseMessages = `losers→gameOverMessages`(newLosers, place = losers.size)

      losers = losers ++ newLosers

      val winMessages = if (onePlayerLeft) List(getWinMessage) else List.empty

      if (winMessages.nonEmpty) scheduler.cancel()

      sendMessagesToPlayers(messages ++ loseMessages ++ winMessages)
      sendPersonalMessagesToPlayers(personalMessages)

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

  private def onePlayerLeft = losers.size + leaved.size == players.size - 1

  private def getWinner = playersList.find(p ⇒ !losers.contains(p.id) && !leaved.contains(p.id)).get

  private def getWinMessage =
    GameOverMsg(GameOverDTO.newBuilder()
      .setPlayerId(getWinner.id.dto)
      .setWin(true)
      .setPlace(1)
      .setReward(config.winReward)
      .build())

  private def getNewLosers =
    for (player ← playersList
         if !losers.contains(player.id)
         if gameState.isPlayerLose(player.id))
    yield player.id

  private def `losers→gameOverMessages`(losers: Iterable[PlayerId], place: Int) =
    for (playerId ← losers)
    yield GameOverMsg(
      GameOverDTO.newBuilder()
        .setPlayerId(playerId.dto)
        .setWin(false)
        .setPlace(place)
        .setReward(0)
        .build()
    )

  override def preStart(): Unit = println("Game start")

  override def postStop(): Unit = println("Game stop")
}