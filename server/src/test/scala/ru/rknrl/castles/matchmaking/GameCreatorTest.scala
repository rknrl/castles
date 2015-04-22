//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.init.GameStateInit
import ru.rknrl.castles.game.state.Player
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.GameCreator.NewGame
import ru.rknrl.castles.matchmaking.Matcher.MatchedGameOrders
import ru.rknrl.castles.matchmaking.NewMatchmaking.GameOrder
import ru.rknrl.dto.AccountType.{DEV, VKONTAKTE}
import ru.rknrl.dto.DeviceType.{PC, PHONE, TABLET}
import ru.rknrl.dto.{AccountId, PlayerId}

class GameCreatorTest extends WordSpec with Matchers {

  val o0 = newGameOrder(
    AccountId(VKONTAKTE, "0"),
    accountState = accountStateMock(
      gamesCount = 8,
      rating = 1400
    ),
    deviceType = TABLET
  )
  val o1 = newGameOrder(
    AccountId(VKONTAKTE, "1"),
    accountState = accountStateMock(
      gamesCount = 0, // <- isTutor
      rating = 1400
    ),
    deviceType = TABLET
  )
  val o2 = newGameOrder(
    AccountId(VKONTAKTE, "2"),
    accountState = accountStateMock(
      gamesCount = 1,
      rating = 1450
    ),
    deviceType = PC
  )
  val o3 = newGameOrder(
    AccountId(VKONTAKTE, "3"),
    accountState = accountStateMock(
      gamesCount = 2,
      rating = 1800
    ),
    deviceType = PHONE
  )

  "newGame1" in {
    val matched = MatchedGameOrders(playersCount = 2, orders = List(o3), isTutor = true)

    val gameCreator = gameCreatorMock()

    val expectedOrders = List(o3) ++ gameCreatorMock().createBotOrders(1, o3)

    val expectedNewGame = NewGame(
      isTutor = true,
      orders = expectedOrders,
      gameState = GameStateInit.init(
        time = 0,
        players = gameCreator.ordersToPlayers(expectedOrders).toList,
        big = false,
        isTutor = true,
        config = configMock().game,
        gameMap = tutorMap2
      )
    )

    val newGame = gameCreator.newGame(matched)

    newGame.isTutor shouldBe expectedNewGame.isTutor
    newGame.orders shouldBe expectedNewGame.orders
    newGame.gameState.dto(PlayerId(0), List.empty) shouldBe expectedNewGame.gameState.dto(PlayerId(0), List.empty)
  }

  "newGame4" in {
    val matched = MatchedGameOrders(playersCount = 4, orders = List(o1, o2), isTutor = false)

    val gameCreator = gameCreatorMock()

    val expectedOrders = List(o1, o2) ++ gameCreatorMock().createBotOrders(2, o1)

    val expectedNewGame = NewGame(
      isTutor = false,
      orders = expectedOrders,
      gameState = GameStateInit.init(
        time = 0,
        players = gameCreator.ordersToPlayers(expectedOrders).toList,
        big = true,
        isTutor = false,
        config = configMock().game,
        gameMap = notTutorMap4
      )
    )

    val newGame = gameCreator.newGame(matched)

    newGame.isTutor shouldBe expectedNewGame.isTutor
    newGame.orders shouldBe expectedNewGame.orders
    newGame.gameState.dto(PlayerId(0), List.empty) shouldBe expectedNewGame.gameState.dto(PlayerId(0), List.empty)
  }

  "createBotOrders" in {
    val accountState = accountStateMock()

    val order = newGameOrder(
      accountId = AccountId(VKONTAKTE, "0"),
      deviceType = PC,
      accountState = accountState,
      isBot = false
    )

    val gameCreator = gameCreatorMock()
    gameCreator.createBotOrders(3, order) shouldBe Vector(

      GameOrder(
        AccountId(DEV, "bot0"),
        PC,
        configMock().botUserInfo(AccountId(DEV, "bot0"), 0),
        gameCreator.botAccountState(order),
        isBot = true
      ),
      GameOrder(
        AccountId(DEV, "bot1"),
        PC,
        configMock().botUserInfo(AccountId(DEV, "bot1"), 1),
        gameCreator.botAccountState(order),
        isBot = true
      ),
      GameOrder(
        AccountId(DEV, "bot2"),
        PC,
        configMock().botUserInfo(AccountId(DEV, "bot2"), 2),
        gameCreator.botAccountState(order),
        isBot = true
      )
    )
  }

  "ordersToPlayers" in {
    val accountState = accountStateMock()

    val order0 = newGameOrder(
      accountId = AccountId(VKONTAKTE, "0"),
      deviceType = PC,
      accountState = accountState,
      isBot = false
    )

    val order1 = newGameOrder(
      accountId = AccountId(VKONTAKTE, "1"),
      deviceType = TABLET,
      accountState = accountState,
      isBot = true
    )

    val accountConfig = accountConfigMock()

    gameCreatorMock().ordersToPlayers(List(order0, order1)) shouldBe List(
      Player(
        PlayerId(0),
        AccountId(VKONTAKTE, "0"),
        order0.userInfo,
        accountState.slots,
        accountConfig.skillsToStat(accountState.skills),
        accountState.items,
        isBot = false
      ),
      Player(
        PlayerId(1),
        AccountId(VKONTAKTE, "1"),
        order1.userInfo,
        accountState.slots,
        accountConfig.skillsToStat(accountState.skills),
        accountState.items,
        isBot = true
      )
    )
  }
}
