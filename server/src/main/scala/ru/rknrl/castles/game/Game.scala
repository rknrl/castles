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

  def noBotCount = {
    var i = 0
    for ((id, player) ← players if !player.isBot) i += 1
    i
  }

  private var losers = Set[PlayerId]()
  private var leaved = Set[PlayerId]()
  private var online = Set[PlayerId]()

  private val `accountId→playerId` =
    for ((playerId, player) ← players)
    yield player.accountId → playerId

  private def `playerId→accountId`(id: PlayerId) =
    `accountId→playerId`.find { case (accountId, playerId) ⇒ playerId == id}.get._1

  private var `playerId→accountRef` = Map[PlayerId, ActorRef]()

  private var `playerId→enterGameRef` = Map[PlayerId, ActorRef]()

  private def `enterGameRef→playerId`(ref: ActorRef) =
    `ref→playerId`(ref, `playerId→enterGameRef`)

  private var `playerId→gameRef` = Map[PlayerId, ActorRef]()

  private def `gameRef→playerId`(ref: ActorRef) =
    `ref→playerId`(ref, `playerId→gameRef`)

  def `ref→playerId`(actorRef: ActorRef, map: Map[PlayerId, ActorRef]) =
    map.find { case (id: PlayerId, ref: ActorRef) ⇒ ref == actorRef}.get._1

  private val sendFps = 30
  private val sendInterval = 1000 / sendFps

  case object UpdateGameState

  import context.dispatcher

  val scheduler = context.system.scheduler.schedule(0 seconds, sendInterval milliseconds, self, UpdateGameState)

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

    val (newGameState, messages, cooldowns) = gameState.update(
      time,
      moveActions.toMap,
      fireballCasts.toMap,
      strengtheningCasts.toMap,
      volcanoCasts.toMap,
      tornadoCasts.toMap,
      assistanceCasts.toMap
    )

    clearMaps()

    (newGameState, messages, cooldowns)
  }

  /**
   * Отправляем online игрокам сообщения об изменении gameState
   * Ботам отправляем просто новый gameState
   */
  private def sendMessagesToPlayers(messages: Iterable[Any]) =
    for ((playerId, ref) ← `playerId→gameRef`
         if online contains playerId
         if !(leaved contains playerId))
      if (players(playerId).isBot)
        ref ! gameState
      else
        for (message ← messages) ref ! message

  private def sendCooldownsToPlayers(cooldowns: Iterable[Cooldown]) =
    for (cooldown ← cooldowns
         if online contains cooldown.playerId
         if !(leaved contains cooldown.playerId)) {
      val ref = `playerId→gameRef`(cooldown.playerId)
      ref ! UpdateItemStatesMsg(cooldown.dto)
    }

  def getPlayerId = `gameRef→playerId`(sender)

  def assertCanPlay() = {
    val playerId = getPlayerId
    assert(!leaved.contains(playerId))
    assert(!losers.contains(playerId))
  }

  override def receive = {
    /**
     * Аккаунт присоединяется к игре и сообщает о рефах куда слать игровые сообщения
     * Сохраняем рефы в мапах
     */
    case Join(accountId, enterGameRef, gameRef) ⇒
      val playerId = `accountId→playerId`(accountId)
      `playerId→accountRef` = `playerId→accountRef` + (playerId → sender)
      `playerId→enterGameRef` = `playerId→enterGameRef` + (playerId → enterGameRef)
      `playerId→gameRef` = `playerId→gameRef` + (playerId → gameRef)

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
      val playerId = `enterGameRef→playerId`(sender)
      println("JoinMsg from playerId " + playerId)
      online = online + playerId
      sender ! JoinGameMsg(gameState.dto(playerId))

    /**
     * Игрок сдается
     */
    case SurrenderMsg() ⇒
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

      val enterGameRef = `playerId→enterGameRef`(playerId)
      enterGameRef ! LeaveGameMsg()

      // Говорим аккаунту

      `playerId→accountRef`(playerId) ! LeaveGame(gameState.gameItems.states(playerId).usedItems)

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
      val (newGameState, messages, cooldowns) = updateGameState
      gameState = newGameState

      val newLosers = getNewLosers

      val loseMessages = `losers→gameOverMessages`(newLosers, place = losers.size)

      losers = losers ++ newLosers

      val winMessages = if (onePlayerLeft) List(getWinMessage) else List.empty

      if (winMessages.nonEmpty) scheduler.cancel()

      sendMessagesToPlayers(messages ++ loseMessages ++ winMessages)
      sendCooldownsToPlayers(cooldowns)

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

  def onePlayerLeft = losers.size + leaved.size == players.size - 1

  def getWinner = playersList.find(p ⇒ !losers.contains(p.id) && !leaved.contains(p.id)).get

  def getWinMessage =
    GameOverMsg(GameOverDTO.newBuilder()
      .setPlayerId(getWinner.id.dto)
      .setWin(true)
      .setPlace(1)
      .setReward(config.winReward)
      .build())

  def getNewLosers =
    for (player ← playersList
         if !losers.contains(player.id)
         if gameState.isPlayerLose(player.id))
    yield player.id

  def `losers→gameOverMessages`(losers: Iterable[PlayerId], place: Int) =
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