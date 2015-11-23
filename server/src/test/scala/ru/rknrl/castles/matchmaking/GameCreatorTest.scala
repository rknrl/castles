//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import org.scalatest.{Matchers, WordSpec}
import protos.AccountType.{DEV, VKONTAKTE}
import protos.DeviceType.{PC, PHONE, TABLET}
import protos.{AccountId, PlayerId}
import ru.rknrl.castles.game.init.GameStateInit
import ru.rknrl.castles.game.state.Player
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.matchmaking.GameCreator.NewGame
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.castles.matchmaking.Matcher.MatchedGameOrders

class GameCreatorTest extends WordSpec with Matchers {

  val o0 = newGameOrder(
    AccountId(VKONTAKTE, "0"),
    accountState = accountStateMock(
      gamesCount = 8
    ),
    rating = 1400,
    deviceType = TABLET
  )
  val o1 = newGameOrder(
    AccountId(VKONTAKTE, "1"),
    accountState = accountStateMock(
      gamesCount = 0 // <- isTutor
    ),
    rating = 1400,
    deviceType = TABLET
  )
  val o2 = newGameOrder(
    AccountId(VKONTAKTE, "2"),
    accountState = accountStateMock(
      gamesCount = 1
    ),
    rating = 1450,
    deviceType = PC
  )
  val o3 = newGameOrder(
    AccountId(VKONTAKTE, "3"),
    accountState = accountStateMock(
      gamesCount = 2
    ),
    rating = 1800,
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
        players = gameCreator.ordersToPlayers(expectedOrders, isTutor = true).toList,
        big = false,
        isTutor = true,
        config = configMock().game,
        gameMap = tutorMap2
      )
    )

    val newGame = gameCreator.newGame(matched, time = 0)

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
        players = gameCreator.ordersToPlayers(expectedOrders, isTutor = false).toList,
        big = true,
        isTutor = false,
        config = configMock().game,
        gameMap = notTutorMap4
      )
    )

    val newGame = gameCreator.newGame(matched, time = 0)

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
      rating = 1400,
      isBot = false
    )

    val gameCreator = gameCreatorMock()
    gameCreator.createBotOrders(3, order) shouldBe Vector(

      GameOrder(
        AccountId(DEV, "bot0"),
        PC,
        configMock().botUserInfo(AccountId(DEV, "bot0"), 0),
        gameCreator.botAccountState(order),
        rating = 1400,
        isBot = true
      ),
      GameOrder(
        AccountId(DEV, "bot1"),
        PC,
        configMock().botUserInfo(AccountId(DEV, "bot1"), 1),
        gameCreator.botAccountState(order),
        rating = 1400,
        isBot = true
      ),
      GameOrder(
        AccountId(DEV, "bot2"),
        PC,
        configMock().botUserInfo(AccountId(DEV, "bot2"), 2),
        gameCreator.botAccountState(order),
        rating = 1400,
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

    gameCreatorMock().ordersToPlayers(List(order0, order1), isTutor = false) shouldBe List(
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

  "ordersToPlayers tutor" in {
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

    val gameCreator = gameCreatorMock()
    gameCreator.ordersToPlayers(List(order0, order1), isTutor = true) shouldBe List(
      Player(
        PlayerId(0),
        AccountId(VKONTAKTE, "0"),
        order0.userInfo,
        accountState.slots,
        gameCreator.tutorHumanStat,
        accountState.items,
        isBot = false
      ),
      Player(
        PlayerId(1),
        AccountId(VKONTAKTE, "1"),
        order1.userInfo,
        accountState.slots,
        gameCreator.tutorBotStat,
        accountState.items,
        isBot = true
      )
    )
  }
}
