//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import akka.actor.Props
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.SecretChecker.SecretChecked
import ru.rknrl.castles.kit.ActorsTest
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.dto.AccountType._
import ru.rknrl.dto.DeviceType._
import ru.rknrl.dto.PlatformType._
import ru.rknrl.dto._

import scala.concurrent.duration._

class SecretCheckerTest extends ActorsTest {

  var authIterator = 0

  def newSecretChecker(config: Config) = {
    authIterator += 1
    system.actorOf(Props(classOf[SecretChecker], config), "auth-" + authIterator)
  }

  "dev" should {

    "isDev=true" in {
      val config = configMock(isDev = true)
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(DEV, "0"))
      val secret = AuthenticationSecretDTO("")
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(true))
    }

    "isDev=false" in {
      val config = configMock(isDev = false)
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(DEV, "0"))
      val secret = AuthenticationSecretDTO("")
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(false))
    }

  }

  "vk" should {

    "valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          vk = socialConfigMock(
            appId = "4628723",
            appSecret = "Y00cvysEueaSqv3lXBHq"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(VKONTAKTE, "264648879"))
      val secret = AuthenticationSecretDTO(
        body = "76721dd553486df115009d68c6bffab2"
      )
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(true))
    }

    "not valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          vk = socialConfigMock(
            appId = "4628723",
            appSecret = "Y00cvysEueaSqv3lXBHq"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(VKONTAKTE, "264648879"))
      val secret = AuthenticationSecretDTO(
        body = "76721dd553486df11__5009d68c6bffab2"
      )
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(false))
    }

    // todo: check access token

  }

  "ok" should {

    "valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          ok = socialConfigMock(
            appId = "1108376832",
            appSecret = "6417C7A172A61A014F17517C"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(ODNOKLASSNIKI, "567041706626"))
      val secret = AuthenticationSecretDTO(
        body = "fa994dd154dc06c7ea75a0a25eae9b3f",
        params = Some("7b2072f949caf76f99798903c8feac5b82451a8f8768a29eec7f92.dd6")
      )
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(true))
    }

    "not valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          ok = socialConfigMock(
            appId = "1108376832",
            appSecret = "6417C7A172A61A014F17517C"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(ODNOKLASSNIKI, "567041706626"))
      val secret = AuthenticationSecretDTO(
        body = "fa994dd154dc06c7ea__75a0a25eae9b3f",
        params = Some("7b2072f949caf76f99798903c8feac5b82451a8f8768a29eec7f92.dd6")
      )
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(false))
    }

    // todo: check access token
  }


  "mm" should {

    "valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          mm = socialConfigMock(
            appId = "726774",
            appSecret = "b679a75c4e3de7c209e411e4ae83925b"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(MOIMIR, "12849865525577489968"))
      val secret = AuthenticationSecretDTO(
        body = "912e7221fcfb595a96bf40db9bcfe350",
        params = Some("app_id=726774authentication_key=6e68582e5a1d0dba5d8c88dbbffec51eext_perm=notifications,emailsis_app_user=1oid=12849865525577489968session_expire=1428772849session_key=f1178b14175d7dac3f9db833dd825b7cvid=12849865525577489968window_id="))
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(true))
    }

    "not valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          mm = socialConfigMock(
            appId = "726774",
            appSecret = "b679a75c4e3de7c209e411e4ae83925b"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(MOIMIR, "12849865525577489968"))
      val secret = AuthenticationSecretDTO(
        body = "912e7221fcfb595a9__6bf40db9bcfe350",
        params = Some("app_id=726774authentication_key=6e68582e5a1d0dba5d8c88dbbffec51eext_perm=notifications,emailsis_app_user=1oid=12849865525577489968session_expire=1428772849session_key=f1178b14175d7dac3f9db833dd825b7cvid=12849865525577489968window_id="))
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(false))
    }

    // todo: check access token
  }

  "fb" should {

    "valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          fb = socialConfigMock(
            appId = "370173203168786",
            appSecret = "4ee8c625b2221acde0a1a6c3b02571e6"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(FACEBOOK, "1386344971679837"))
      val secret = AuthenticationSecretDTO(
        body = "1PdETyfwGvX-ni7ML9ibCXq7CTzfyhYVYj25wdwDC_k.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUURESVBDVEJtNWhUQVJxbEZuaWg1MmJxVlVudUQybkpoZE90dmV1MktKT04yNXZWd3QxZTl3WmV1OWhLUnNseEJMZUVjWWd1TXN3c28zYzhaM2JXazZqbkZBNWNWZFdoaGI5NmpkdlduaGx5R0xaN01aOU9kYW1CanVZMWxSRzBQbmVaQ0pqQXA2TE41dXRRaWFackhxTDlPeThQaDYtb3lGNzFoTUdNNHBlXzFDX3BBLS1lZGkybE5BVkc4eV8zWnBDZkR3bDdka19lZXRydHVyS2ZWX1A4U3NfbmYtM2lRWXRKSVNUb184TW5GYVFxTmFzX3lkT2lURHFJV0s2d0YzNF9TR0dmUm12Mm12YWVXTllEZ1NwbXNieC1NYmFiREVackFqbXFMLWFXaUdvZXBlU0VRVXdiQ21WU090TGYybHhINlFmM3dqaEV4cnFLZlhTM2RabCIsImlzc3VlZF9hdCI6MTQyODY4NjY5MCwidXNlcl9pZCI6IjEzODYzNDQ5NzE2Nzk4MzcifQ"
      )
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(1 second, SecretChecked(true))
    }

    "not valid secret" in {
      val config = configMock(
        social = socialConfigsMock(
          fb = socialConfigMock(
            appId = "370173203168786",
            appSecret = "4ee8c625b2221acde0a1a6c3b02571e6"
          )
        )
      )
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(FACEBOOK, "1386344971679837"))
      val secret = AuthenticationSecretDTO(
        body = "1PdETyfwGvX-ni7ML9ibCX__q7CTzfyhYVYj25wdwDC_k.eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsImNvZGUiOiJBUURESVBDVEJtNWhUQVJxbEZuaWg1MmJxVlVudUQybkpoZE90dmV1MktKT04yNXZWd3QxZTl3WmV1OWhLUnNseEJMZUVjWWd1TXN3c28zYzhaM2JXazZqbkZBNWNWZFdoaGI5NmpkdlduaGx5R0xaN01aOU9kYW1CanVZMWxSRzBQbmVaQ0pqQXA2TE41dXRRaWFackhxTDlPeThQaDYtb3lGNzFoTUdNNHBlXzFDX3BBLS1lZGkybE5BVkc4eV8zWnBDZkR3bDdka19lZXRydHVyS2ZWX1A4U3NfbmYtM2lRWXRKSVNUb184TW5GYVFxTmFzX3lkT2lURHFJV0s2d0YzNF9TR0dmUm12Mm12YWVXTllEZ1NwbXNieC1NYmFiREVackFqbXFMLWFXaUdvZXBlU0VRVXdiQ21WU090TGYybHhINlFmM3dqaEV4cnFLZlhTM2RabCIsImlzc3VlZF9hdCI6MTQyODY4NjY5MCwidXNlcl9pZCI6IjEzODYzNDQ5NzE2Nzk4MzcifQ"
      )
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(1 second, SecretChecked(false))
    }

    // todo: check access token
  }

  "deviceId" should {
    "always true" in {
      val config = configMock()
      val secretChecker = newSecretChecker(config)
      val userInfo = UserInfoDTO(AccountId(DEVICE_ID, "1"))
      val secret = AuthenticationSecretDTO("")
      secretChecker ! AuthenticateDTO(userInfo, CANVAS, PC, secret)
      expectMsg(SecretChecked(true))
    }
  }

}
