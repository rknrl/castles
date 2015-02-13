//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.ActorLogging
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, RowData}
import ru.rknrl.EscalateStrategyActor
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.MatchMaking.TopItem
import ru.rknrl.castles.database.Database._
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, TutorStateDTO, UserInfoDTO}

import scala.concurrent.Await
import scala.concurrent.duration._

class DbConfiguration(username: String,
                      host: String,
                      port: Int,
                      password: String,
                      database: String) {
  def configuration = new Configuration(username, host, port, Some(password), Some(database))
}

class MySqlDb(configuration: DbConfiguration) extends EscalateStrategyActor with ActorLogging {
  val connection = new MySQLConnection(configuration.configuration)

  Await.result(connection.connect, 5 seconds)

  def rowDataToTopItem(rowData: RowData) = {
    val idByteArray = rowData(0).asInstanceOf[Array[Byte]]
    val id = AccountIdDTO.parseFrom(idByteArray)
    val rating = rowData(1).asInstanceOf[Double]
    val userInfoByteArray = rowData(2).asInstanceOf[Array[Byte]]
    val userInfo = UserInfoDTO.parseFrom(userInfoByteArray)
    TopItem(new AccountId(id), rating, userInfo)
  }

  override def receive: Receive = {
    case GetTop ⇒
      val ref = sender()
      connection.sendQuery("SELECT id, rating, userInfo FROM account_state ORDER BY rating DESC LIMIT 5;").map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            ref ! resultSet.map(rowDataToTopItem).toList
          case None ⇒
            log.error("Top response None")
        }
      )

    case Insert(accountId, accountState, userInfo, tutorState) ⇒
      val ref = sender()

      connection.sendPreparedStatement("INSERT INTO account_state (id,rating,state,userInfo) VALUES (?,?,?,?);", Seq(accountId.toByteArray, accountState.getRating, accountState.toByteArray, userInfo.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            connection.sendPreparedStatement("INSERT INTO tutor_state (id,state) VALUES (?,?);", Seq(accountId.toByteArray, tutorState.toByteArray)).map(
              queryResult ⇒
                if (queryResult.rowsAffected == 1) {
                  ref ! AccountStateResponse(accountId, accountState)
                } else
                  log.error("Insert tutorState rowsAffected=" + queryResult.rowsAffected)
            )
          else
            log.error("Insert rowsAffected=" + queryResult.rowsAffected)
      )

    case Update(accountId, accountState) ⇒
      val ref = sender()
      connection.sendPreparedStatement("UPDATE account_state SET rating=?,state=? WHERE id=?;", Seq(accountState.getRating, accountState.toByteArray, accountId.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! AccountStateResponse(accountId, accountState)
          else
            log.error("Update rowsAffected=" + queryResult.rowsAffected)
      )

    case UpdateUserInfo(accountId, userInfo) ⇒
      val ref = sender()
      connection.sendPreparedStatement("UPDATE account_state SET userInfo=? WHERE id=?;", Seq(userInfo.toByteArray, accountId.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! userInfo
          else
            log.error("Update rowsAffected=" + queryResult.rowsAffected)
      )

    case UpdateTutorState(accountId, tutorState) ⇒
      connection.sendPreparedStatement("UPDATE tutor_state SET state=? WHERE id=?;", Seq(tutorState.toByteArray, accountId.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1) {
            // ok
          } else
            log.error("Update tutorState rowsAffected=" + queryResult.rowsAffected)
      )

    case GetAccountState(accountId) ⇒
      val ref = sender()
      connection.sendPreparedStatement("SELECT state FROM account_state WHERE id=?;", Seq(accountId.toByteArray)).map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            if (resultSet.size == 0) {
              ref ! AccountNoExists
            } else if (resultSet.size == 1) {
              val row: RowData = resultSet.head

              val byteArray = row(0).asInstanceOf[Array[Byte]]
              val state = AccountStateDTO.parseFrom(byteArray)

              ref ! AccountStateResponse(accountId, state)
            } else
              log.error("Update rows=" + resultSet.size)

          case None ⇒
            log.error("Get response None")
        }
      )

    case GetTutorState(accountId) ⇒
      val ref = sender()
      connection.sendPreparedStatement("SELECT state FROM tutor_state WHERE id=?;", Seq(accountId.toByteArray)).map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            if (resultSet.size == 1) {
              val row: RowData = resultSet.head

              val byteArray = row(0).asInstanceOf[Array[Byte]]
              val state = TutorStateDTO.parseFrom(byteArray)

              ref ! TutorStateResponse(accountId, state)
            } else
              log.error("Get tutorState rows=" + resultSet.size)

          case None ⇒
            log.error("Get response None")
        }
      )

  }
}
