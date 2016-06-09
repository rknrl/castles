//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.storage

import akka.testkit.TestActorRef
import com.github.mauricio.async.db.Connection
import protos.AccountType.DEV
import protos._
import ru.rknrl.castles.kit.Mocks
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.castles.storage.Storage._
import ru.rknrl.castles.storage.TestStorage.{TableTruncated, TruncateTable}
import ru.rknrl.test.ActorsTest

import scala.concurrent.{Await, Future}

class DatabaseTest extends ActorsTest {
  val config = new StorageConfig(
    username = "testuser",
    host = "localhost",
    port = 3306,
    password = "password",
    database = "test",
    poolMaxObjects = 8,
    poolMaxIdle = 60000,
    poolMaxQueueSize = 1000
  )

  val storageRef = TestActorRef[TestStorage](TestStorage.props(config))
  val storage = storageRef.underlyingActor

  def await[T](f: (Connection ⇒ Future[T])): T =
    Await.result(storage.pool.inTransaction(f), noMsgTimeout)

  "AccountState" in {
    val accountId = AccountId(DEV, "1")

    storageRef ! TruncateTable("account_state")
    expectMsg(TableTruncated("account_state"))

    await(implicit c ⇒ storage.getAccountState(accountId)) shouldBe None

    def updateAccountState(accountState: AccountState): Unit = {
      await(implicit c ⇒ storage.replaceAccountState(accountId, accountState)) shouldBe()
      await(implicit c ⇒ storage.getAccountState(accountId)) shouldBe Some(accountState)
    }

    updateAccountState(Mocks.accountStateMock())
    updateAccountState(Mocks.accountStateMock().copy(gold = 100))
  }

  "TutorState" in {
    val accountId = AccountId(DEV, "1")

    storageRef ! TruncateTable("tutor_state")
    expectMsg(TableTruncated("tutor_state"))

    await(implicit c ⇒ storage.getTutorState(accountId)) shouldBe None

    def updateTutorState(tutorState: TutorState): Unit = {
      storageRef ! ReplaceTutorState(accountId, tutorState)
      expectMsg(TutorStateUpdated(accountId, tutorState))

      await(implicit c ⇒ storage.getTutorState(accountId)) shouldBe Some(tutorState)
    }

    updateTutorState(TutorState())
    updateTutorState(TutorState(navigate = Some(true)))
  }

  "Rating" in {
    val accountId = AccountId(DEV, "1")

    storageRef ! TruncateTable("ratings")
    expectMsg(TableTruncated("ratings"))

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 1)) shouldBe None

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 2)) shouldBe None

    // weekNumber = 1

    await(implicit c ⇒ storage.replaceRating(accountId, weekNumber = 1, newRating = 1567, UserInfo(accountId))) shouldBe()

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 1)) shouldBe Some(1567)

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 2)) shouldBe None

    // weekNumber = 2

    await(implicit c ⇒ storage.replaceRating(accountId, weekNumber = 2, newRating = 1400, UserInfo(accountId))) shouldBe()

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 1)) shouldBe Some(1567)

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 2)) shouldBe Some(1400)

    // weekNumber = 1 again

    await(implicit c ⇒ storage.replaceRating(accountId, weekNumber = 1, newRating = 1600, UserInfo(accountId))) shouldBe()

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 1)) shouldBe Some(1600)

    await(implicit c ⇒ storage.getRating(accountId, weekNumber = 2)) shouldBe Some(1400)
  }

  "UpdateUserInfo" in {
    val accountId = AccountId(DEV, "1")

    storageRef ! TruncateTable("user_info")
    expectMsg(TableTruncated("user_info"))

    await(implicit c ⇒ storage.getUserInfo(accountId)) shouldBe None

    def updateUserInfo(userInfo: UserInfo): Unit = {
      storageRef ! ReplaceUserInfo(accountId, userInfo)
      expectMsg(UserInfoUpdated(accountId, userInfo))

      await(implicit c ⇒ storage.getUserInfo(accountId)) shouldBe Some(userInfo)
    }

    updateUserInfo(UserInfo(accountId, firstName = Some("tolya"), lastName = Some("yanot")))
    updateUserInfo(UserInfo(accountId, firstName = Some("kurt"), lastName = Some("cobain")))
  }

  "GetPlace" in {
    storageRef ! TruncateTable("ratings")
    expectMsg(TableTruncated("ratings"))

    def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
      val accountId = AccountId(DEV, id)
      await(implicit c ⇒ storage.replaceRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))) shouldBe()
    }

    writeRating("1", 1, 1400)
    writeRating("2", 1, 1300)
    writeRating("3", 2, 1600)
    writeRating("4", 1, 1500)
    writeRating("5", 2, 1340)
    writeRating("6", 1, 1000)

    // 1 week

    await(implicit c ⇒ storage.getPlace(rating = 1400, weekNumber = 1)) shouldBe 2

    await(implicit c ⇒ storage.getPlace(rating = 900, weekNumber = 1)) shouldBe 5

    // 2 week

    await(implicit c ⇒ storage.getPlace(rating = 1400, weekNumber = 2)) shouldBe 2

    await(implicit c ⇒ storage.getPlace(rating = 900, weekNumber = 2)) shouldBe 3

    // 3 week

    await(implicit c ⇒ storage.getPlace(rating = 900, weekNumber = 3)) shouldBe 1
  }

  "GetTop" should {

    "empty" in {
      storageRef ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      storageRef ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      await(implicit c ⇒ storage.getTop(weekNumber = 1)) shouldBe Top(Seq.empty, weekNumber = 1)
    }

    "without user info" in {
      storageRef ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      storageRef ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        await(implicit c ⇒ storage.replaceRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))) shouldBe()
      }

      for (i ← 0 to 50) {
        writeRating((50 - i).toString, i % 2, 50 - i)
      }

      await(implicit c ⇒ storage.getTop(weekNumber = 0)) shouldBe Top(
        Seq(
          TopUser(AccountId(DEV, "50"), 50, UserInfo(AccountId(DEV, "50"))),
          TopUser(AccountId(DEV, "48"), 48, UserInfo(AccountId(DEV, "48"))),
          TopUser(AccountId(DEV, "46"), 46, UserInfo(AccountId(DEV, "46"))),
          TopUser(AccountId(DEV, "44"), 44, UserInfo(AccountId(DEV, "44"))),
          TopUser(AccountId(DEV, "42"), 42, UserInfo(AccountId(DEV, "42")))
        ),
        weekNumber = 0
      )

      await(implicit c ⇒ storage.getTop(weekNumber = 1)) shouldBe Top(
        Seq(
          TopUser(AccountId(DEV, "49"), 49, UserInfo(AccountId(DEV, "49"))),
          TopUser(AccountId(DEV, "47"), 47, UserInfo(AccountId(DEV, "47"))),
          TopUser(AccountId(DEV, "45"), 45, UserInfo(AccountId(DEV, "45"))),
          TopUser(AccountId(DEV, "43"), 43, UserInfo(AccountId(DEV, "43"))),
          TopUser(AccountId(DEV, "41"), 41, UserInfo(AccountId(DEV, "41")))
        ),
        weekNumber = 1
      )

    }

    "with user info" in {
      storageRef ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      storageRef ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        await(implicit c ⇒ storage.replaceRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))) shouldBe()
        val userInfo = UserInfo(accountId, firstName = Some(id))
        storageRef ! ReplaceUserInfo(accountId, userInfo)
        expectMsg(UserInfoUpdated(accountId, userInfo))
      }

      for (i ← 0 to 50) {
        writeRating((50 - i).toString, i % 2, 50 - i)
      }

      await(implicit c ⇒ storage.getTop(weekNumber = 0)) shouldBe Top(
        Seq(
          TopUser(AccountId(DEV, "50"), 50, UserInfo(AccountId(DEV, "50"), Some("50"))),
          TopUser(AccountId(DEV, "48"), 48, UserInfo(AccountId(DEV, "48"), Some("48"))),
          TopUser(AccountId(DEV, "46"), 46, UserInfo(AccountId(DEV, "46"), Some("46"))),
          TopUser(AccountId(DEV, "44"), 44, UserInfo(AccountId(DEV, "44"), Some("44"))),
          TopUser(AccountId(DEV, "42"), 42, UserInfo(AccountId(DEV, "42"), Some("42")))
        ),
        weekNumber = 0
      )

      await(implicit c ⇒ storage.getTop(weekNumber = 1)) shouldBe Top(
        Seq(
          TopUser(AccountId(DEV, "49"), 49, UserInfo(AccountId(DEV, "49"), Some("49"))),
          TopUser(AccountId(DEV, "47"), 47, UserInfo(AccountId(DEV, "47"), Some("47"))),
          TopUser(AccountId(DEV, "45"), 45, UserInfo(AccountId(DEV, "45"), Some("45"))),
          TopUser(AccountId(DEV, "43"), 43, UserInfo(AccountId(DEV, "43"), Some("43"))),
          TopUser(AccountId(DEV, "41"), 41, UserInfo(AccountId(DEV, "41"), Some("41")))
        ),
        weekNumber = 1
      )
    }

    "without ratings" in {
      storageRef ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      storageRef ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        val userInfo = UserInfo(accountId, firstName = Some(id))
        storageRef ! ReplaceUserInfo(accountId, userInfo)
        expectMsg(UserInfoUpdated(accountId, userInfo))
      }

      for (i ← 0 to 50) {
        writeRating(i.toString, i % 2, 50 - i)
      }

      await(implicit c ⇒ storage.getTop(weekNumber = 1)) shouldBe Top(Seq.empty, weekNumber = 1)
    }

    "5 entries" in {
      storageRef ! TruncateTable("ratings")
      expectMsg(TableTruncated("ratings"))
      storageRef ! TruncateTable("user_info")
      expectMsg(TableTruncated("user_info"))

      def writeRating(id: String, weekNumber: Int, rating: Double): Unit = {
        val accountId = AccountId(DEV, id)
        await(implicit c ⇒ storage.replaceRating(accountId, weekNumber = weekNumber, newRating = rating, UserInfo(accountId))) shouldBe()
        val userInfo = UserInfo(accountId, firstName = Some(id))
        storageRef ! ReplaceUserInfo(accountId, userInfo)
        expectMsg(UserInfoUpdated(accountId, userInfo))
      }

      for (i ← 0 to 5) {
        writeRating((50 - i).toString, i % 2, 50 - i)
      }

      await(implicit c ⇒ storage.getTop(weekNumber = 0)) shouldBe Top(
        Seq(
          TopUser(AccountId(DEV, "50"), 50, UserInfo(AccountId(DEV, "50"), Some("50"))),
          TopUser(AccountId(DEV, "48"), 48, UserInfo(AccountId(DEV, "48"), Some("48"))),
          TopUser(AccountId(DEV, "46"), 46, UserInfo(AccountId(DEV, "46"), Some("46")))
        ),
        weekNumber = 0
      )

      await(implicit c ⇒ storage.getTop(weekNumber = 1)) shouldBe Top(
        Seq(
          TopUser(AccountId(DEV, "49"), 49, UserInfo(AccountId(DEV, "49"), Some("49"))),
          TopUser(AccountId(DEV, "47"), 47, UserInfo(AccountId(DEV, "47"), Some("47"))),
          TopUser(AccountId(DEV, "45"), 45, UserInfo(AccountId(DEV, "45"), Some("45")))
        ),
        weekNumber = 1
      )
    }
  }
}
