//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.Props
import com.github.mauricio.async.db.RowData
import ru.rknrl.castles.database.Database.UserInfoResponse
import ru.rknrl.castles.database.TestDatabase.{GetUserInfo, TableTruncated, TruncateTable}
import ru.rknrl.dto.{AccountId, UserInfoDTO}

object TestDatabase {
  def props(config: DbConfiguration) = Props(classOf[TestDatabase], config)

  case class TruncateTable(table: String)

  case class TableTruncated(table: String)

  case class GetUserInfo(accountId: AccountId)

}

class TestDatabase(configuration: DbConfiguration) extends Database(configuration) {

  import context.dispatcher

  def testReceive = logged {
    case TruncateTable(table) ⇒
      val ref = sender
      pool.sendPreparedStatement("TRUNCATE TABLE " + table + ";", Seq.empty).map(
        queryResult ⇒ send(ref, TableTruncated(table))
      ) onFailure {
        case t: Throwable ⇒ log.error("Database error", t)
      }

    case GetUserInfo(accountId) ⇒
      val ref = sender
      read(
        "SELECT userInfo FROM user_info WHERE id=?;",
        Seq(accountId.toByteArray),
        resultSet ⇒
          if (resultSet.isEmpty)
            send(ref, UserInfoResponse(accountId, None))
          else if (resultSet.size == 1)
            send(ref, UserInfoResponse(accountId, Some(rowDataToUserInfo(resultSet.head))))
          else
            log.error("Get user info: invalid result rows count = " + resultSet.size)
      )
  }

  def rowDataToUserInfo(row: RowData) = {
    val byteArray = row("userInfo").asInstanceOf[Array[Byte]]
    UserInfoDTO.parseFrom(byteArray)
  }

  override def receive = testReceive.orElse(super.receive)
}
