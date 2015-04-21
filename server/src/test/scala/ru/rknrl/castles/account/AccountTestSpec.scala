//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.{ActorRef, Props}
import ru.rknrl.castles.Config
import ru.rknrl.castles.kit.ActorsTest
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.dto.AccountType.VKONTAKTE
import ru.rknrl.dto._

class AccountTestSpec extends ActorsTest {

  var accountIterator = 0

  def newAccount(matchmaking: ActorRef = self,
                 auth: ActorRef = self,
                 database: ActorRef = self,
                 bugs: ActorRef = self,
                 config: Config = configMock()) = {
    accountIterator += 1
    system.actorOf(Props(classOf[NewAccount], matchmaking, auth, database, bugs, config), "account-" + accountIterator)
  }

  def authenticateMock(userInfo: UserInfoDTO = UserInfoDTO(AccountId(VKONTAKTE, "1")),
                       secret: AuthenticationSecretDTO = AuthenticationSecretDTO(body = "body"),
                       platformType: PlatformType = PlatformType.CANVAS,
                       deviceType: DeviceType = DeviceType.PC) =
    AuthenticateDTO(userInfo, platformType, deviceType, secret)
}
