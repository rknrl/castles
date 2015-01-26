package ru.rknrl.castles.account

import akka.actor.ActorRef
import ru.rknrl.base.AccountId
import ru.rknrl.base.account.Account
import ru.rknrl.castles._
import ru.rknrl.base.database.AccountStateDb.Update
import ru.rknrl.castles.rmi._
import ru.rknrl.dto.AccountDTO._
import ru.rknrl.dto.AuthDTO.{AuthenticationSuccessDTO, TopUserInfoDTO}
import ru.rknrl.dto.CommonDTO._

import scala.collection.JavaConverters._

class CastlesAccount(accountId: AccountId,
                     accountState: AccountState,
                     deviceType: DeviceType,
                     userInfo: UserInfoDTO,
                     tcpSender: ActorRef, tcpReceiver: ActorRef,
                     matchmaking: ActorRef,
                     accountStateDb: ActorRef,
                     auth: ActorRef,
                     config: Config,
                     name: String) extends Account(accountId, deviceType, userInfo, tcpSender, tcpReceiver, matchmaking, accountStateDb, auth, config, name) {

  override var state = accountState

  override protected def authenticationSuccessDto(enterGame: Boolean, gameAddress: Option[NodeLocator]) = {
    val builder = AuthenticationSuccessDTO.newBuilder()
      .setAccountState(state.dto)
      .setConfig(config.account.dto)
      .addAllTop(topMock.asJava)
      .addAllProducts(config.productsDto(accountId.accountType).asJava)
      .setEnterGame(enterGame)

    if (gameAddress.isDefined) builder.setGame(gameAddress.get)

    builder.build
  }

  override def receive = {
    super.receive orElse {
      case SwapSlotsMsg(swap: SwapSlotsDTO) ⇒
        state = state.swapSlots(swap.getId1, swap.getId2)
        accountStateDb ! Update(accountId, state.dto)

      case BuyBuildingMsg(buy: BuyBuildingDTO) ⇒
        state = state.buyBuilding(buy.getId, buy.getBuildingType)
        accountStateDb ! Update(accountId, state.dto)

      case UpgradeBuildingMsg(dto: UpgradeBuildingDTO) ⇒
        state = state.upgradeBuilding(dto.getId)
        accountStateDb ! Update(accountId, state.dto)

      case RemoveBuildingMsg(dto: RemoveBuildingDTO) ⇒
        state = state.removeBuilding(dto.getId)
        accountStateDb ! Update(accountId, state.dto)

      case UpgradeSkillMsg(upgrade: UpgradeSkillDTO) ⇒
        state = state.upgradeSkill(upgrade.getType, config.account)
        accountStateDb ! Update(accountId, state.dto)

      case BuyItemMsg(buy: BuyItemDTO) ⇒
        state = state.buyItem(buy.getType)
        accountStateDb ! Update(accountId, state.dto)
    }
  }

  private def topMock =
    for (i ← 1 to 5)
    yield TopUserInfoDTO.newBuilder()
      .setPlace(i)
      .setInfo(
        UserInfoDTO.newBuilder()
          .setAccountId(
            AccountIdDTO.newBuilder()
              .setId("1")
              .setType(AccountType.DEV)
              .build()
          )
          .setFirstName("name" + i)
          .setPhoto256(i.toString)
          .build()
      ).build()
}