package ru.rknrl.castles.database

import akka.actor.ActorLogging
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, RowData}
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.base.MatchMaking.TopItem
import ru.rknrl.castles.database.AccountStateDb._
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.AccountType

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
  val connection = new MySQLConnection(configuration.configuration)

  Await.result(connection.connect, 5 seconds)

  override def receive: Receive = {

    case GetTop ⇒
      log.info("GetTop")
      //val future: Future[QueryResult] = connection.sendQuery("SELECT * FROM account_state LIMIT 5 ORDER BY rating DESC;")
      sender ! List(TopItem(new AccountId(AccountType.VKONTAKTE, "1"), 1400))

    case insert@Insert(id, accountState) ⇒
      log.info("Insert " + id)
      val ref = sender()
      connection.sendPreparedStatement("INSERT INTO account_state (id,rating,state) VALUES (?,?,?);", Seq(id.dto.toByteArray, accountState.getRating, accountState.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! accountState
          else
            log.error("Insert rowsAffected=" + queryResult.rowsAffected)
      )

    case update@Update(id, accountState) ⇒
      log.info("Update " + id)
      val ref = sender()
      connection.sendPreparedStatement("UPDATE account_state SET rating=?,state=? WHERE id=?;", Seq(accountState.getRating, accountState.toByteArray, id.dto.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! update
          else
            log.error("Update rowsAffected=" + queryResult.rowsAffected)
      )

    case Get(id) ⇒
      log.info("Get " + id)
      val ref = sender()
      connection.sendPreparedStatement("SELECT state FROM account_state WHERE id=?;", Seq(id.dto.toByteArray)).map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            if (resultSet.size == 0) {
              log.info("Get response Some.size == 0")
              ref ! NoExist
            } else {
              log.info("Get response Some.size != 0")
              val row: RowData = resultSet.head

              val byteArray = row(0).asInstanceOf[Array[Byte]]
              val state = AccountStateDTO.parseFrom(byteArray)

              ref ! state
            }
          case None ⇒
            log.error("Get response None")
        }
      )
  }
}
