//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.ActorRef
import akka.testkit.TestProbe
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.TestDatabase.GetUserInfo
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.dto.AccountType.DEV
import ru.rknrl.dto.{AccountId, TutorStateDTO, UserInfoDTO}
import ru.rknrl.test.ActorsTest

class DatabaseCacheTest extends ActorsTest {

  def newDatabaseCache(database: ActorRef) = system.actorOf(DatabaseCache.props(database))

  "AccountState" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val databaseCache = newDatabaseCache(database.ref)
    val accountId = AccountId(DEV, "1")

    // при первом запросе запрашиваем у бд

    client.send(databaseCache, GetAccountState(accountId))
    database.expectMsg(GetAccountState(accountId))
    val accountState = Mocks.accountStateMock()
    database.send(databaseCache, AccountStateResponse(accountId, Some(accountState)))
    client.expectMsg(AccountStateResponse(accountId, Some(accountState)))

    // при втором запросе отвечаем из кеша

    client.send(databaseCache, GetAccountState(accountId))
    client.expectMsg(AccountStateResponse(accountId, Some(accountState)))

    // update

    val newAccountState = Mocks.accountStateMock().copy(gold = 100)
    client.send(databaseCache, UpdateAccountState(accountId, newAccountState))
    database.expectMsg(UpdateAccountState(accountId, newAccountState))
    database.send(databaseCache, AccountStateResponse(accountId, Some(newAccountState)))
    client.expectMsg(AccountStateResponse(accountId, Some(newAccountState)))

    // при запросе отвечаем из кеша новым значением

    client.send(databaseCache, GetAccountState(accountId))
    client.expectMsg(AccountStateResponse(accountId, Some(newAccountState)))

  }

  "TutorState" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val databaseCache = newDatabaseCache(database.ref)
    val accountId = AccountId(DEV, "1")

    // при первом запросе запрашиваем у бд

    client.send(databaseCache, GetTutorState(accountId))
    database.expectMsg(GetTutorState(accountId))
    val tutorState = TutorStateDTO()
    database.send(databaseCache, TutorStateResponse(accountId, Some(tutorState)))
    client.expectMsg(TutorStateResponse(accountId, Some(tutorState)))

    // при втором запросе отвечаем из кеша

    client.send(databaseCache, GetTutorState(accountId))
    client.expectMsg(TutorStateResponse(accountId, Some(tutorState)))

    // update

    val newTutorState = TutorStateDTO(navigate = Some(true))
    client.send(databaseCache, UpdateTutorState(accountId, newTutorState))
    database.expectMsg(UpdateTutorState(accountId, newTutorState))
    database.send(databaseCache, TutorStateResponse(accountId, Some(newTutorState)))
    client.expectMsg(TutorStateResponse(accountId, Some(newTutorState)))

    // при запросе отвечаем из кеша новым значением

    client.send(databaseCache, GetTutorState(accountId))
    client.expectMsg(TutorStateResponse(accountId, Some(newTutorState)))

  }

  "Rating" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val databaseCache = newDatabaseCache(database.ref)
    val accountId = AccountId(DEV, "1")

    def test(weekNumber: Int, initRating: Double): Unit = {

      // при первом запросе запрашиваем у бд

      client.send(databaseCache, GetRating(accountId, weekNumber = weekNumber))
      database.expectMsg(GetRating(accountId, weekNumber = weekNumber))
      val rating = initRating
      database.send(databaseCache, RatingResponse(accountId, weekNumber = weekNumber, rating = Some(rating)))
      client.expectMsg(RatingResponse(accountId, weekNumber = weekNumber, rating = Some(rating)))

      // при втором запросе отвечаем из кеша

      client.send(databaseCache, GetRating(accountId, weekNumber = weekNumber))
      client.expectMsg(RatingResponse(accountId, weekNumber = weekNumber, rating = Some(rating)))

      // update

      val newRating = initRating + 100
      client.send(databaseCache, UpdateRating(accountId, weekNumber = weekNumber, newRating, UserInfoDTO(accountId)))
      database.expectMsg(UpdateRating(accountId, weekNumber = weekNumber, newRating, UserInfoDTO(accountId)))
      database.send(databaseCache, RatingResponse(accountId, weekNumber = weekNumber, rating = Some(newRating)))
      client.expectMsg(RatingResponse(accountId, weekNumber = weekNumber, rating = Some(newRating)))

      // при запросе отвечаем из кеша новым значением

      client.send(databaseCache, GetRating(accountId, weekNumber = weekNumber))
      client.expectMsg(RatingResponse(accountId, weekNumber = weekNumber, rating = Some(newRating)))
    }

    test(weekNumber = 0, initRating = 1500)
    test(weekNumber = 1, initRating = 400)
  }

  "Top" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val databaseCache = newDatabaseCache(database.ref)

    def test(weekNumber: Int): Unit = {
      val top = Top(
        Seq(
          TopUser(AccountId(DEV, "1"), rating = 1500, info = UserInfoDTO(AccountId(DEV, "1"))),
          TopUser(AccountId(DEV, "2"), rating = 1400, info = UserInfoDTO(AccountId(DEV, "2"))),
          TopUser(AccountId(DEV, "3"), rating = 1300, info = UserInfoDTO(AccountId(DEV, "3"))),
          TopUser(AccountId(DEV, "4"), rating = 1200, info = UserInfoDTO(AccountId(DEV, "4"))),
          TopUser(AccountId(DEV, "5"), rating = 1100, info = UserInfoDTO(AccountId(DEV, "5")))
        ),
        weekNumber = weekNumber
      )

      // при первом запросе запрашиваем у бд

      client.send(databaseCache, GetTop(weekNumber = weekNumber))
      database.expectMsg(GetTop(weekNumber = weekNumber))
      database.send(databaseCache, top)
      client.expectMsg(top)

      // при втором запросе отвечаем из кеша

      client.send(databaseCache, GetTop(weekNumber = weekNumber))
      client.expectMsg(top)

      // update rating

      val accountId = AccountId(DEV, "2")

      client.send(databaseCache, UpdateRating(accountId, weekNumber = weekNumber, newRating = 2000, UserInfoDTO(accountId)))
      database.expectMsg(UpdateRating(accountId, weekNumber = weekNumber, 2000, UserInfoDTO(accountId)))
      database.send(databaseCache, RatingResponse(accountId, weekNumber = weekNumber, rating = Some(2000)))
      client.expectMsg(RatingResponse(AccountId(DEV, "2"), weekNumber = weekNumber, rating = Some(2000)))

      // get updated top

      client.send(databaseCache, GetTop(weekNumber = weekNumber))
      client.expectMsg(Top(
        Seq(
          TopUser(AccountId(DEV, "2"), rating = 2000, info = UserInfoDTO(AccountId(DEV, "2"))),
          TopUser(AccountId(DEV, "1"), rating = 1500, info = UserInfoDTO(AccountId(DEV, "1"))),
          TopUser(AccountId(DEV, "3"), rating = 1300, info = UserInfoDTO(AccountId(DEV, "3"))),
          TopUser(AccountId(DEV, "4"), rating = 1200, info = UserInfoDTO(AccountId(DEV, "4"))),
          TopUser(AccountId(DEV, "5"), rating = 1100, info = UserInfoDTO(AccountId(DEV, "5")))
        ),
        weekNumber = weekNumber
      ))
    }

    test(weekNumber = 0)
    test(weekNumber = 1)
  }

  "GetPlace" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val databaseCache = newDatabaseCache(database.ref)

    client.send(databaseCache, GetPlace(rating = 1200, weekNumber = 1))
    database.expectMsg(GetPlace(rating = 1200, weekNumber = 1))
    database.reply(PlaceResponse(rating = 1200, weekNumber = 1, place = 3))
    client.expectMsg(PlaceResponse(rating = 1200, weekNumber = 1, place = 3))
  }

  "UpdateUserInfo" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val databaseCache = newDatabaseCache(database.ref)
    val accountId = AccountId(DEV, "1")

    val userInfo = UserInfoDTO(accountId)
    client.send(databaseCache, UpdateUserInfo(accountId, userInfo))
    database.expectMsg(UpdateUserInfo(accountId, userInfo))
    database.reply(UserInfoResponse(accountId, Some(userInfo)))
    client.expectMsg(UserInfoResponse(accountId, Some(userInfo)))
  }
}
