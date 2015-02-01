package ru.rknrl.castles.account

import akka.actor.ActorRef
import ru.rknrl.base.AccountId
import ru.rknrl.base.account.Account
import ru.rknrl.base.database.AccountStateDb.Update
import ru.rknrl.castles._
import ru.rknrl.castles.account.state.AccountState
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

  override protected def authenticationSuccessDto(searchOpponents: Boolean, gameAddress: Option[NodeLocator], top: Iterable[TopUserInfoDTO]) = {
    val builder = AuthenticationSuccessDTO.newBuilder()
      .setAccountState(state.dto)
      .setConfig(config.account.dto)
      .addAllTop(top.asJava)
      .addAllProducts(config.productsDto(accountId.accountType).asJava)
      .setSearchOpponents(searchOpponents)

    if (gameAddress.isDefined) builder.setGame(gameAddress.get)

    builder.build
  }

  override def receive = {
    super.receive orElse {
      case BuyBuildingMsg(buy: BuyBuildingDTO) ⇒
        updateState(state.buyBuilding(buy.getId, buy.getBuildingType, config.account))

      case UpgradeBuildingMsg(dto: UpgradeBuildingDTO) ⇒
        updateState(state.upgradeBuilding(dto.getId, config.account))

      case RemoveBuildingMsg(dto: RemoveBuildingDTO) ⇒
        updateState(state.removeBuilding(dto.getId))

      case UpgradeSkillMsg(upgrade: UpgradeSkillDTO) ⇒
        updateState(state.upgradeSkill(upgrade.getType, config.account))

      case BuyItemMsg(buy: BuyItemDTO) ⇒
        updateState(state.buyItem(buy.getType, config.account))
    }
  }

  private def updateState(newState: AccountState) = {
    state = newState
    accountStateDb ! Update(accountId.dto, state.dto)
  }
}