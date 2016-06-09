//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.storage

import akka.actor.Props
import com.github.mauricio.async.db.{Connection, RowData}
import protos.{AccountId, UserInfo}
import ru.rknrl.castles.storage.TestStorage.{TableTruncated, TruncateTable}

import scala.concurrent.Future

object TestStorage {
  def props(config: StorageConfig) = Props(classOf[TestStorage], config, new RealCalendar)

  case class TruncateTable(table: String)

  case class TableTruncated(table: String)

}

class TestStorage(configuration: StorageConfig, calendar: Calendar) extends Storage(configuration, calendar) {

  import context.dispatcher

  def testReceive: Receive = logged {
    case TruncateTable(table) ⇒
      val ref = sender
      pool.sendPreparedStatement("TRUNCATE TABLE " + table + ";", Seq.empty).map(
        queryResult ⇒ send(ref, TableTruncated(table))
      ) onFailure {
        case t: Throwable ⇒ log.error("Database error", t)
      }
  }

  def getUserInfo(accountId: AccountId)(implicit connection: Connection): Future[Option[UserInfo]] =
    get(
      "SELECT userInfo FROM user_info WHERE id=?;",
      Seq(accountId.toByteArray)
    ) flatMap {
      resultSet ⇒
        if (resultSet.isEmpty)
          Future.successful(None)
        else if (resultSet.size == 1)
          Future.successful(Some(rowDataToUserInfo(resultSet.head)))
        else
          Future.failed(new Throwable("Get user info: invalid result rows count = " + resultSet.size))
    }

  def rowDataToUserInfo(row: RowData) = {
    val byteArray = row("userInfo").asInstanceOf[Array[Byte]]
    UserInfo.parseFrom(byteArray)
  }

  override def receive = testReceive.orElse(super.receive)
}
