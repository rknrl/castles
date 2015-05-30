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
import ru.rknrl.castles.database.DatabaseTransaction._
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.dto.AccountType.DEV
import ru.rknrl.dto.{AccountId, AccountStateDTO, TutorStateDTO, UserInfoDTO}
import ru.rknrl.test.ActorsTest

class DatabaseTransactionTest extends ActorsTest {
  def newDatabaseTransaction(database: ActorRef) = system.actorOf(DatabaseTransaction.props(database, new FakeCalendar(1)))

  "GetAccount" in {
    val database = new TestProbe(system)
    val client = new TestProbe(system)
    val transaction = newDatabaseTransaction(database.ref)

    val accountId = AccountId(DEV, "1")
    client.send(transaction, GetAccount(accountId))

    database.expectMsg(GetAccountState(accountId))
    val accountState = Mocks.accountStateMock()
    database.reply(Database.AccountStateResponse(accountId, Some(accountState)))

    database.expectMsg(GetTutorState(accountId))
    val tutorState = TutorStateDTO()
    database.reply(TutorStateResponse(accountId, Some(tutorState)))

    database.expectMsg(GetRating(accountId, weekNumber = 1))
    val rating = 1500
    database.reply(RatingResponse(accountId, weekNumber = 1, Some(rating)))

    database.expectMsg(GetPlace(weekNumber = 1, rating = rating))
    val place = 123
    database.reply(PlaceResponse(weekNumber = 1, rating = rating, place = place))

    database.expectMsg(GetTop(weekNumber = 1))
    val top = Top(
      Seq(
        TopUser(AccountId(DEV, "1"), rating = 1500, info = UserInfoDTO(AccountId(DEV, "1"))),
        TopUser(AccountId(DEV, "2"), rating = 1400, info = UserInfoDTO(AccountId(DEV, "2"))),
        TopUser(AccountId(DEV, "3"), rating = 1300, info = UserInfoDTO(AccountId(DEV, "3"))),
        TopUser(AccountId(DEV, "4"), rating = 1200, info = UserInfoDTO(AccountId(DEV, "4"))),
        TopUser(AccountId(DEV, "5"), rating = 1100, info = UserInfoDTO(AccountId(DEV, "5")))
      ),
      weekNumber = 1
    )
    database.reply(top)


    database.expectMsg(GetRating(accountId, weekNumber = 0))
    val lastWeekRating = 1600
    database.reply(RatingResponse(accountId, weekNumber = 0, Some(lastWeekRating)))

    database.expectMsg(GetPlace(weekNumber = 0, rating = lastWeekRating))
    val lastWeekPlace = 120
    database.reply(PlaceResponse(weekNumber = 0, rating = lastWeekRating, place = lastWeekPlace))

    database.expectMsg(GetTop(weekNumber = 0))
    val lastWeekTop = Top(
      Seq(
        TopUser(AccountId(DEV, "1"), rating = 1500, info = UserInfoDTO(AccountId(DEV, "1"))),
        TopUser(AccountId(DEV, "2"), rating = 1400, info = UserInfoDTO(AccountId(DEV, "2"))),
        TopUser(AccountId(DEV, "3"), rating = 1300, info = UserInfoDTO(AccountId(DEV, "3"))),
        TopUser(AccountId(DEV, "4"), rating = 1200, info = UserInfoDTO(AccountId(DEV, "4"))),
        TopUser(AccountId(DEV, "5"), rating = 1100, info = UserInfoDTO(AccountId(DEV, "5")))
      ),
      weekNumber = 0
    )
    database.reply(lastWeekTop)

    client.expectMsg(AccountResponse(
      accountId,
      Some(accountState),
      Some(rating),
      Some(tutorState),
      top,
      Some(place),
      Some(lastWeekPlace),
      lastWeekTop
    ))
  }

  "GetAccount New" in {
    val database = new TestProbe(system)
    val client = new TestProbe(system)
    val transaction = newDatabaseTransaction(database.ref)

    val accountId = AccountId(DEV, "1")
    client.send(transaction, GetAccount(accountId))

    database.expectMsg(GetAccountState(accountId))
    database.reply(Database.AccountStateResponse(accountId, None))

    database.expectMsg(GetTutorState(accountId))
    database.reply(TutorStateResponse(accountId, None))

    database.expectMsg(GetRating(accountId, weekNumber = 1))
    database.reply(RatingResponse(accountId, weekNumber = 1, None))

    database.expectMsg(GetTop(weekNumber = 1))
    val top = Top(
      Seq(
        TopUser(AccountId(DEV, "1"), rating = 1500, info = UserInfoDTO(AccountId(DEV, "1"))),
        TopUser(AccountId(DEV, "2"), rating = 1400, info = UserInfoDTO(AccountId(DEV, "2"))),
        TopUser(AccountId(DEV, "3"), rating = 1300, info = UserInfoDTO(AccountId(DEV, "3"))),
        TopUser(AccountId(DEV, "4"), rating = 1200, info = UserInfoDTO(AccountId(DEV, "4"))),
        TopUser(AccountId(DEV, "5"), rating = 1100, info = UserInfoDTO(AccountId(DEV, "5")))
      ),
      weekNumber = 1
    )
    database.reply(top)


    database.expectMsg(GetRating(accountId, weekNumber = 0))
    database.reply(RatingResponse(accountId, weekNumber = 0, None))

    database.expectMsg(GetTop(weekNumber = 0))
    val lastWeekTop = Top(
      Seq(
        TopUser(AccountId(DEV, "10"), rating = 1500, info = UserInfoDTO(AccountId(DEV, "1"))),
        TopUser(AccountId(DEV, "20"), rating = 1400, info = UserInfoDTO(AccountId(DEV, "2"))),
        TopUser(AccountId(DEV, "30"), rating = 1300, info = UserInfoDTO(AccountId(DEV, "3"))),
        TopUser(AccountId(DEV, "40"), rating = 1200, info = UserInfoDTO(AccountId(DEV, "4"))),
        TopUser(AccountId(DEV, "50"), rating = 1100, info = UserInfoDTO(AccountId(DEV, "5")))
      ),
      weekNumber = 0
    )
    database.reply(lastWeekTop)

    client.expectMsg(AccountResponse(
      accountId,
      None,
      None,
      None,
      top,
      None,
      None,
      lastWeekTop
    ))
  }

  "GetAndUpdateAccountState" in {
    val database = new TestProbe(system)
    val client = new TestProbe(system)
    val transaction = newDatabaseTransaction(database.ref)

    val accountId = AccountId(DEV, "1")
    client.send(transaction, GetAndUpdateAccountState(accountId, state ⇒ state.get.copy(gold = state.get.gold + 100)))

    database.expectMsg(GetAccountState(accountId))
    val accountState = Mocks.accountStateMock()
    database.reply(Database.AccountStateResponse(accountId, Some(accountState)))

    val newState = Mocks.accountStateMock().copy(gold = 110)
    database.expectMsg(UpdateAccountState(accountId, newState))
    database.reply(Database.AccountStateResponse(accountId, Some(newState)))

    client.expectMsg(DatabaseTransaction.AccountStateResponse(accountId, newState))
  }

  "GetAndUpdateAccountStateAndRating" in {
    val database = new TestProbe(system)
    val client = new TestProbe(system)
    val transaction = newDatabaseTransaction(database.ref)

    val accountId = AccountId(DEV, "1")
    val transform = (state: Option[AccountStateDTO], rating: Option[Double]) ⇒ (state.get.copy(gold = state.get.gold + 100), rating.get + 200)
    val userInfo = UserInfoDTO(accountId)
    client.send(transaction, GetAndUpdateAccountStateAndRating(accountId, transform, userInfo))

    database.expectMsg(GetAccountState(accountId))
    val accountState = Mocks.accountStateMock()
    database.reply(Database.AccountStateResponse(accountId, Some(accountState)))

    database.expectMsg(GetRating(accountId, weekNumber = 1))
    val lastWeekRating = 1600
    database.reply(RatingResponse(accountId, weekNumber = 1, Some(lastWeekRating)))

    val newState = Mocks.accountStateMock().copy(gold = 110)
    database.expectMsg(UpdateAccountState(accountId, newState))
    database.reply(Database.AccountStateResponse(accountId, Some(newState)))

    val newRating = 1800
    database.expectMsg(UpdateRating(accountId, weekNumber = 1, newRating, userInfo))
    database.reply(Database.RatingResponse(accountId, weekNumber = 1, Some(newRating)))

    database.expectMsg(GetPlace(weekNumber = 1, rating = newRating))
    val place = 120
    database.reply(PlaceResponse(weekNumber = 1, rating = newRating, place = place))

    database.expectMsg(GetTop(weekNumber = 1))
    val top = Top(
      Seq(
        TopUser(AccountId(DEV, "1"), rating = 1500, info = UserInfoDTO(AccountId(DEV, "1"))),
        TopUser(AccountId(DEV, "2"), rating = 1400, info = UserInfoDTO(AccountId(DEV, "2"))),
        TopUser(AccountId(DEV, "3"), rating = 1300, info = UserInfoDTO(AccountId(DEV, "3"))),
        TopUser(AccountId(DEV, "4"), rating = 1200, info = UserInfoDTO(AccountId(DEV, "4"))),
        TopUser(AccountId(DEV, "5"), rating = 1100, info = UserInfoDTO(AccountId(DEV, "5")))
      ),
      weekNumber = 1
    )
    database.reply(top)

    client.expectMsg(DatabaseTransaction.AccountStateAndRatingResponse(accountId, newState, newRating, place, top))
  }

  "UpdateTutorState" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val transaction = newDatabaseTransaction(database.ref)
    val accountId = AccountId(DEV, "1")

    val tutorState = TutorStateDTO()
    client.send(transaction, UpdateTutorState(accountId, tutorState))
    database.expectMsg(UpdateTutorState(accountId, tutorState))
    database.reply(TutorStateResponse(accountId, Some(tutorState)))
    client.expectMsg(TutorStateResponse(accountId, Some(tutorState)))
  }

  "UpdateUserInfo" in {
    val client = new TestProbe(system)
    val database = new TestProbe(system)

    val transaction = newDatabaseTransaction(database.ref)
    val accountId = AccountId(DEV, "1")

    val userInfo = UserInfoDTO(accountId)
    client.send(transaction, UpdateUserInfo(accountId, userInfo))
    database.expectMsg(UpdateUserInfo(accountId, userInfo))
    database.reply(UserInfoResponse(accountId, Some(userInfo)))
    client.expectMsg(UserInfoResponse(accountId, Some(userInfo)))
  }
}
