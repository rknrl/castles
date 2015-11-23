//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import protos.AccountType.DEV
import protos._
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.database.TestDatabase.{GetUserInfo, TableTruncated, TruncateTable}
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.test.ActorsTest

class DatabaseTest extends ActorsTest {
  val config = new DbConfiguration(
    username = "testuser",
    host = "localhost",
    port = 3306,
    password = "password",
    database = "test",
    poolMaxObjects = 8,
    poolMaxIdle = 60000,
    poolMaxQueueSize = 1000
  )

  val database = system.actorOf(TestDatabase.props(config))

  "AccountState" in {
    val accountId = AccountId(DEV, "1")

    database ! TruncateTable("account_state")
    expectMsg(TableTruncated("account_state"))

    database ! GetAccountState(accountId)
    expectMsg(AccountStateResponse(accountId, None))

    def updateAccountState(accountState: AccountState): Unit = {
      database ! UpdateAccountState(accountId, accountState)
      expectMsg(AccountStateResponse(accountId, Some(accountState)))

      database ! GetAccountState(accountId)
      expectMsg(AccountStateResponse(accountId, Some(accountState)))
    }

    updateAccountState(Mocks.accountStateMock())
    updateAccountState(Mocks.accountStateMock().copy(gold = 100))
  }

  "TutorState" in {
    val accountId = AccountId(DEV, "1")

    database ! TruncateTable("tutor_state")
    expectMsg(TableTruncated("tutor_state"))

    database ! GetTutorState(accountId)
    expectMsg(TutorStateResponse(accountId, None))

    def updateTutorState(tutorState: TutorState): Unit = {
      database ! UpdateTutorState(accountId, tutorState)
      expectMsg(TutorStateResponse(accountId, Some(tutorState)))

      database ! GetTutorState(accountId)
      expectMsg(TutorStateResponse(accountId, Some(tutorState)))
    }

    updateTutorState(TutorState())
    updateTutorState(TutorState(navigate = Some(true)))
  }

  "Rating" in {
    val accountId = AccountId(DEV, "1")

    database ! TruncateTable("ratings")
    expectMsg(TableTruncated("ratings"))

    database ! GetRating(accountId, weekNumber = 1)
    expectMsg(RatingResponse(accountId, 1, None))

    database ! GetRating(accountId, weekNumber = 2)
    expectMsg(RatingResponse(accountId, 2, None))

    // weekNumber = 1

    database ! UpdateRating(accountId, weekNumber = 1, newRating = 1567, UserInfo(accountId))
    expectMsg(RatingResponse(accountId, 1, Some(1567)))

    database ! GetRating(accountId, weekNumber = 1)
    expectMsg(RatingResponse(accountId, 1, Some(1567)))

    database ! GetRating(accountId, weekNumber = 2)
    expectMsg(RatingResponse(accountId, 2, None))

    // weekNumber = 2

    database ! UpdateRating(accountId, weekNumber = 2, newRating = 1400, UserInfo(accountId))
    expectMsg(RatingResponse(accountId, 2, Some(1400)))

    database ! GetRating(accountId, weekNumber = 1)
    expectMsg(RatingResponse(accountId, 1, Some(1567)))

    database ! GetRating(accountId, weekNumber = 2)
    expectMsg(RatingResponse(accountId, 2, Some(1400)))

    // weekNumber = 1 again

    database ! UpdateRating(accountId, weekNumber = 1, newRating = 1600, UserInfo(accountId))
    expectMsg(RatingResponse(accountId, 1, Some(1600)))

    database ! GetRating(accountId, weekNumber = 1)
    expectMsg(RatingResponse(accountId, 1, Some(1600)))

    database ! GetRating(accountId, weekNumber = 2)
    expectMsg(RatingResponse(accountId, 2, Some(1400)))
  }

  "UpdateUserInfo" in {
    val accountId = AccountId(DEV, "1")

    database ! TruncateTable("user_info")
    expectMsg(TableTruncated("user_info"))

    database ! GetUserInfo(accountId)
    expectMsg(UserInfoResponse(accountId, None))

    def updateUserInfo(userInfo: UserInfo): Unit = {
      database ! UpdateUserInfo(accountId, userInfo)
      expectMsg(UserInfoResponse(accountId, Some(userInfo)))

      database ! GetUserInfo(accountId)
      expectMsg(UserInfoResponse(accountId, Some(userInfo)))
    }

    updateUserInfo(UserInfo(accountId, firstName = Some("tolya"), lastName = Some("yanot")))
    updateUserInfo(UserInfo(accountId, firstName = Some("kurt"), lastName = Some("cobain")))
  }

  "GetPlace" in {
    database ! TruncateTable("ratings")
    expectMsg(TableTruncated("ratings"))

    def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
      val accountId = AccountId(DEV, id)
      database ! UpdateRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))
      expectMsg(RatingResponse(accountId, weekNumber, Some(rating)))
    }

    writeRating("1", 1, 1400)
    writeRating("2", 1, 1300)
    writeRating("3", 2, 1600)
    writeRating("4", 1, 1500)
    writeRating("5", 2, 1340)
    writeRating("6", 1, 1000)

    // 1 week

    database ! GetPlace(rating = 1400, weekNumber = 1)
    expectMsg(PlaceResponse(rating = 1400, weekNumber = 1, place = 2))

    database ! GetPlace(rating = 900, weekNumber = 1)
    expectMsg(PlaceResponse(rating = 900, weekNumber = 1, place = 5))

    // 2 week

    database ! GetPlace(rating = 1400, weekNumber = 2)
    expectMsg(PlaceResponse(rating = 1400, weekNumber = 2, place = 2))

    database ! GetPlace(rating = 900, weekNumber = 2)
    expectMsg(PlaceResponse(rating = 900, weekNumber = 2, place = 3))

    // 3 week

    database ! GetPlace(rating = 900, weekNumber = 3)
    expectMsg(PlaceResponse(rating = 900, weekNumber = 3, place = 1))
  }

  "GetTop" should {

    "empty" in {
      database ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      database ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      database ! GetTop(weekNumber = 1)
      expectMsg(Top(Seq.empty, weekNumber = 1))

    }

    "without user info" in {
      database ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      database ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        database ! UpdateRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))
        expectMsg(RatingResponse(accountId, weekNumber, Some(rating)))
      }

      for (i ← 0 to 50) {
        writeRating((50 - i).toString, i % 2, 50 - i)
      }

      database ! GetTop(weekNumber = 0)
      expectMsg(
        Top(
          Seq(
            TopUser(AccountId(DEV, "50"), 50, UserInfo(AccountId(DEV, "50"))),
            TopUser(AccountId(DEV, "48"), 48, UserInfo(AccountId(DEV, "48"))),
            TopUser(AccountId(DEV, "46"), 46, UserInfo(AccountId(DEV, "46"))),
            TopUser(AccountId(DEV, "44"), 44, UserInfo(AccountId(DEV, "44"))),
            TopUser(AccountId(DEV, "42"), 42, UserInfo(AccountId(DEV, "42")))
          ),
          weekNumber = 0
        )
      )

      database ! GetTop(weekNumber = 1)
      expectMsg(
        Top(
          Seq(
            TopUser(AccountId(DEV, "49"), 49, UserInfo(AccountId(DEV, "49"))),
            TopUser(AccountId(DEV, "47"), 47, UserInfo(AccountId(DEV, "47"))),
            TopUser(AccountId(DEV, "45"), 45, UserInfo(AccountId(DEV, "45"))),
            TopUser(AccountId(DEV, "43"), 43, UserInfo(AccountId(DEV, "43"))),
            TopUser(AccountId(DEV, "41"), 41, UserInfo(AccountId(DEV, "41")))
          ),
          weekNumber = 1
        )
      )

    }

    "with user info" in {
      database ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      database ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        database ! UpdateRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))
        expectMsg(RatingResponse(accountId, weekNumber, Some(rating)))
        val userInfo = UserInfo(accountId, firstName = Some(id))
        database ! UpdateUserInfo(accountId, userInfo)
        expectMsg(UserInfoResponse(accountId, Some(userInfo)))
      }

      for (i ← 0 to 50) {
        writeRating((50 - i).toString, i % 2, 50 - i)
      }

      database ! GetTop(weekNumber = 0)
      expectMsg(
        Top(
          Seq(
            TopUser(AccountId(DEV, "50"), 50, UserInfo(AccountId(DEV, "50"), Some("50"))),
            TopUser(AccountId(DEV, "48"), 48, UserInfo(AccountId(DEV, "48"), Some("48"))),
            TopUser(AccountId(DEV, "46"), 46, UserInfo(AccountId(DEV, "46"), Some("46"))),
            TopUser(AccountId(DEV, "44"), 44, UserInfo(AccountId(DEV, "44"), Some("44"))),
            TopUser(AccountId(DEV, "42"), 42, UserInfo(AccountId(DEV, "42"), Some("42")))
          ),
          weekNumber = 0
        )
      )

      database ! GetTop(weekNumber = 1)
      expectMsg(
        Top(
          Seq(
            TopUser(AccountId(DEV, "49"), 49, UserInfo(AccountId(DEV, "49"), Some("49"))),
            TopUser(AccountId(DEV, "47"), 47, UserInfo(AccountId(DEV, "47"), Some("47"))),
            TopUser(AccountId(DEV, "45"), 45, UserInfo(AccountId(DEV, "45"), Some("45"))),
            TopUser(AccountId(DEV, "43"), 43, UserInfo(AccountId(DEV, "43"), Some("43"))),
            TopUser(AccountId(DEV, "41"), 41, UserInfo(AccountId(DEV, "41"), Some("41")))
          ),
          weekNumber = 1
        )
      )
    }

    "without ratings" in {
      database ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      database ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        val userInfo = UserInfo(accountId, firstName = Some(id))
        database ! UpdateUserInfo(accountId, userInfo)
        expectMsg(UserInfoResponse(accountId, Some(userInfo)))
      }

      for (i ← 0 to 50) {
        writeRating(i.toString, i % 2, 50 - i)
      }

      database ! GetTop(weekNumber = 1)
      expectMsg(Top(Seq.empty, weekNumber = 1))
    }

    "5 entries" in {
      database ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      database ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        database ! UpdateRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))
        expectMsg(RatingResponse(accountId, weekNumber, Some(rating)))
        val userInfo = UserInfo(accountId, firstName = Some(id))
        database ! UpdateUserInfo(accountId, userInfo)
        expectMsg(UserInfoResponse(accountId, Some(userInfo)))
      }

      for (i ← 0 to 5) {
        writeRating((50 - i).toString, i % 2, 50 - i)
      }

      database ! GetTop(weekNumber = 0)
      expectMsg(
        Top(
          Seq(
            TopUser(AccountId(DEV, "50"), 50, UserInfo(AccountId(DEV, "50"), Some("50"))),
            TopUser(AccountId(DEV, "48"), 48, UserInfo(AccountId(DEV, "48"), Some("48"))),
            TopUser(AccountId(DEV, "46"), 46, UserInfo(AccountId(DEV, "46"), Some("46")))
          ),
          weekNumber = 0
        )
      )

      database ! GetTop(weekNumber = 1)
      expectMsg(
        Top(
          Seq(
            TopUser(AccountId(DEV, "49"), 49, UserInfo(AccountId(DEV, "49"), Some("49"))),
            TopUser(AccountId(DEV, "47"), 47, UserInfo(AccountId(DEV, "47"), Some("47"))),
            TopUser(AccountId(DEV, "45"), 45, UserInfo(AccountId(DEV, "45"), Some("45")))
          ),
          weekNumber = 1
        )
      )
    }
  }
}
