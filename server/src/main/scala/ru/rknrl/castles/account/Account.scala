package ru.rknrl.castles.account

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.MatchMaking._
import ru.rknrl.castles._
import ru.rknrl.castles.config.Config
import ru.rknrl.castles.game.Game.{Join, Offline}
import ru.rknrl.castles.rmi.b2c._
import ru.rknrl.core.rmi.{RegisterReceiver, UnregisterReceiver}
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.AuthDTO.DeviceType
import ru.rknrl.dto.CommonDTO.{ItemType, NodeLocator}

case object GetAccountState

case class LeaveGame(usedItems: Map[ItemType, Int])

class Account(externalAccountId: AccountId,
              deviceType: DeviceType,
              tcpSender: ActorRef, tcpReceiver: ActorRef,
              matchmaking: ActorRef,
              auth: ActorRef,
              config: Config,
              name: String) extends Actor {

  private val accountRmi = context.actorOf(Props(classOf[AccountRMI], tcpSender, self), "account-rmi" + name)
  tcpReceiver ! RegisterReceiver(accountRmi)

  private var state = AccountState.initAccount(config.account)

  def receive = {
    case EnterGameMsg() ⇒
      matchmaking ! PlaceGameOrder(new GameOrder(externalAccountId, deviceType, self, state.startLocation, state.skills, state.items, isBot = false))

    case SwapSlotsMsg(swap: SwapSlotsDTO) ⇒
      state = state.swapSlots(swap.getId1, swap.getId2)
      sender ! StartLocationUpdatedMsg(state.startLocation.dto)

    case BuyBuildingMsg(buy: BuyBuildingDTO) ⇒
      state = state.buyBuilding(buy.getId, buy.getBuildingType)
      sender ! GoldUpdatedMsg(state.gold)
      sender ! StartLocationUpdatedMsg(state.startLocation.dto)

    case UpgradeBuildingMsg(dto: UpgradeBuildingDTO) ⇒
      state = state.upgradeBuilding(dto.getId)
      sender ! GoldUpdatedMsg(state.gold)
      sender ! StartLocationUpdatedMsg(state.startLocation.dto)

    case RemoveBuildingMsg(dto: RemoveBuildingDTO) ⇒
      state = state.removeBuilding(dto.getId)
      sender ! StartLocationUpdatedMsg(state.startLocation.dto)

    case UpgradeSkillMsg(upgrade: UpgradeSkillDTO) ⇒
      state = state.upgradeSkill(upgrade.getType)
      sender ! GoldUpdatedMsg(state.gold)
      sender ! PricesUpdatedMsg(state.prices)
      sender ! SkillsUpdatedMsg(state.skills.dto)

    case BuyItemMsg(buy: BuyItemDTO) ⇒
      state = state.buyItem(buy.getType)
      sender ! GoldUpdatedMsg(state.gold)
      sender ! ItemsUpdatedMsg(state.items.dto)

    case BuyGoldMsg() ⇒
      state = state.addGold(state.config.goldByDollar)
      sender ! GoldUpdatedMsg(state.gold)

    /**
     * Auth спрашивает accountState
     * Спрашиваем матчмейкинг находится ли этот игрок в бою
     */
    case GetAccountState ⇒
      matchmaking ! InGame(externalAccountId)

    /**
     * Matchmaking ответил на InGame
     * Отправляем Auth accountState
     */
    case InGameResponse(gameRef) ⇒
      val builder = state.dto

      if (gameRef.isDefined)
        builder.setGame(
          NodeLocator.newBuilder()
            .setHost("localhost")
            .setPort(config.tcpPort)
            .build()
        )

      auth ! builder.build

      if (gameRef.isDefined) connectToGame(gameRef.get)

    /**
     * Matchmaking говорит к какой игре коннектится
     */
    case ConnectToGame(gameRef) ⇒
      connectToGame(gameRef)

      accountRmi ! EnteredGameMsg(
        NodeLocator.newBuilder()
          .setHost(config.tcpIp) // todo: если localhost то клиент не приконнкится
          .setPort(config.tcpPort)
          .build()
      )

    /**
     * Game говорит, что для этого игрока бой завершен
     */
    case LeaveGame(usedItems) ⇒
      assert(game.isDefined)
      assert(enterGameRmi.isDefined)
      assert(gameRmi.isDefined)

      for ((itemType, count) ← usedItems)
        state = state.addItem(itemType, -count)

      tcpReceiver ! UnregisterReceiver(enterGameRmi.get)
      context stop enterGameRmi.get

      tcpReceiver ! UnregisterReceiver(gameRmi.get)
      context stop gameRmi.get

      game = None
      enterGameRmi = None
      gameRmi = None

      accountRmi ! ItemsUpdatedMsg(state.items.dto)
  }

  protected def connectToGame(game: ActorRef) = {
    assert(this.game.isEmpty)
    assert(this.enterGameRmi.isEmpty)
    assert(this.gameRmi.isEmpty)

    val enterGameRmi = context.actorOf(Props(classOf[EnterGameRMI], tcpSender, game), "enter-game-rmi" + name)
    tcpReceiver ! RegisterReceiver(enterGameRmi)

    val gameRmi = context.actorOf(Props(classOf[GameRMI], tcpSender, game), "game-rmi" + name)
    tcpReceiver ! RegisterReceiver(gameRmi)

    game ! Join(externalAccountId, enterGameRmi, gameRmi)

    this.enterGameRmi = Some(enterGameRmi)
    this.gameRmi = Some(gameRmi)
    this.game = Some(game)
  }

  private var game: Option[ActorRef] = None

  private var enterGameRmi: Option[ActorRef] = None

  private var gameRmi: Option[ActorRef] = None

  override def preStart(): Unit = println("AccountService start " + name)

  override def postStop(): Unit = {
    if (game.isDefined) game.get ! Offline(externalAccountId)
    println("AccountService stop " + name)
  }
}