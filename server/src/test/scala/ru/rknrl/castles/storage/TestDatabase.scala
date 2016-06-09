//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.storage

import akka.actor.Props
import com.github.mauricio.async.db.RowData
import protos.{AccountId, UserInfo}
import ru.rknrl.castles.storage.Storage.UserInfoResponse
import ru.rknrl.castles.storage.TestDatabase.{GetUserInfo, TableTruncated, TruncateTable}

import scala.concurrent.Future

object TestDatabase {
  def props(config: StorageConfig) = Props(classOf[TestDatabase], config, new RealCalendar)

  case class TruncateTable(table: String)

  case class TableTruncated(table: String)

  case class GetUserInfo(accountId: AccountId)

}

class TestDatabase(configuration: StorageConfig, calendar: Calendar) extends Storage(configuration, calendar) {

  import context.dispatcher

  def testReceive: Receive = logged {
    case TruncateTable(table) ⇒
      val ref = sender
      pool.sendPreparedStatement("TRUNCATE TABLE " + table + ";", Seq.empty).map(
        queryResult ⇒ send(ref, TableTruncated(table))
      ) onFailure {
        case t: Throwable ⇒ log.error("Database error", t)
      }

    case GetUserInfo(accountId) ⇒
      answer(implicit connection ⇒
        read(
          "SELECT userInfo FROM user_info WHERE id=?;",
          Seq(accountId.toByteArray)
        ) flatMap {
          resultSet ⇒
            if (resultSet.isEmpty)
              Future.successful(UserInfoResponse(accountId, None))
            else if (resultSet.size == 1)
              Future.successful(UserInfoResponse(accountId, Some(rowDataToUserInfo(resultSet.head))))
            else
              Future.failed(new Throwable("Get user info: invalid result rows count = " + resultSet.size))
        }
      )
  }

  def rowDataToUserInfo(row: RowData) = {
    val byteArray = row("userInfo").asInstanceOf[Array[Byte]]
    UserInfo.parseFrom(byteArray)
  }

  override def receive = testReceive.orElse(super.receive)
}
