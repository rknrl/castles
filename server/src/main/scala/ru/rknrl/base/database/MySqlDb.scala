package ru.rknrl.base.database

import akka.actor.ActorLogging
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, RowData}
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.base.MatchMaking.TopItem
import ru.rknrl.base.database.AccountStateDb._
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, UserInfoDTO}

import scala.concurrent.Await
import scala.concurrent.duration._

class DbConfiguration(username: String,
                      host: String,
                      port: Int,
                      password: String,
                      database: String) {
  def configuration = new Configuration(username, host, port, Some(password), Some(database))
}

class MySqlDb(configuration: DbConfiguration) extends StoppingStrategyActor with ActorLogging {
  private val connection = new MySQLConnection(configuration.configuration)

  Await.result(connection.connect, 5 seconds)

  private def rowDataToTopItem(rowData: RowData) = {
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

    case Insert(accountId, accountState, userInfo) ⇒
      val ref = sender()
      connection.sendPreparedStatement("INSERT INTO account_state (id,rating,state,userInfo) VALUES (?,?,?,?);", Seq(accountId.dto.toByteArray, accountState.getRating, accountState.toByteArray, userInfo.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! accountState
          else
            log.error("Insert rowsAffected=" + queryResult.rowsAffected)
      )

    case Update(accountId, accountState) ⇒
      val ref = sender()
      connection.sendPreparedStatement("UPDATE account_state SET rating=?,state=? WHERE id=?;", Seq(accountState.getRating, accountState.toByteArray, accountId.dto.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! accountState
          else
            log.error("Update rowsAffected=" + queryResult.rowsAffected)
      )

    case UpdateUserInfo(accountId, userInfo) ⇒
      val ref = sender()
      connection.sendPreparedStatement("UPDATE account_state SET userInfo=? WHERE id=?;", Seq(userInfo.toByteArray, accountId.dto.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! userInfo
          else
            log.error("Update rowsAffected=" + queryResult.rowsAffected)
      )

    case Get(accountId) ⇒
      val ref = sender()
      connection.sendPreparedStatement("SELECT state FROM account_state WHERE id=?;", Seq(accountId.dto.toByteArray)).map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            if (resultSet.size == 0) {
              ref ! NoExist
            } else if (resultSet.size == 1) {
              val row: RowData = resultSet.head

              val byteArray = row(0).asInstanceOf[Array[Byte]]
              val state = AccountStateDTO.parseFrom(byteArray)

              ref ! state
            } else
              log.error("Update rows=" + resultSet.size)

          case None ⇒
            log.error("Get response None")
        }
      )
  }
}
