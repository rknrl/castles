//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef}
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, RowData}
import ru.rknrl.castles.database.Database._
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.dto._
import ru.rknrl.logging.ActorLog

class DbConfiguration(username: String,
                      host: String,
                      port: Int,
                      password: String,
                      database: String,
                      poolMaxObjects: Int,
                      poolMaxIdle: Long,
                      poolMaxQueueSize: Int) {
  def configuration = new Configuration(username, host, port, Some(password), Some(database))

  def poolConfiguration = new PoolConfiguration(poolMaxObjects, poolMaxIdle, poolMaxQueueSize)
}

object Database {

  /** Ответом будет List[TopItem] */
  case object GetTop

  /** Ответом будет AccountDeleted */
  case class DeleteAccount(accountId: AccountId)

  case class AccountDeleted(accountId: AccountId)

  /** Ответом будет AccountStateResponse или AccountNoExists */
  case class GetAccountState(accountId: AccountId)

  case class AccountStateResponse(accountId: AccountId, state: AccountStateDTO)

  case object AccountNoExists

  /** Ответом будет TutorStateResponse */
  case class GetTutorState(accountId: AccountId)

  case class TutorStateResponse(accountId: AccountId, tutorState: TutorStateDTO)

  /** Ответом будет AccountStateResponse */
  case class Insert(accountId: AccountId, accountState: AccountStateDTO, userInfo: UserInfoDTO, tutorState: TutorStateDTO)

  /** Ответом будет AccountStateResponse */
  case class UpdateAccountState(accountId: AccountId, accountState: AccountStateDTO)

  /** Без ответа */
  case class UpdateTutorState(accountId: AccountId, tutorState: TutorStateDTO)

  /** Без ответа */
  case class UpdateUserInfo(accountId: AccountId, userInfo: UserInfoDTO)

}

class Database(configuration: DbConfiguration, val bugs: ActorRef) extends Actor with ActorLog {

  val factory = new MySQLConnectionFactory(configuration.configuration)
  val pool = new ConnectionPool(factory, configuration.poolConfiguration)

  def `rowData→topUser`(rowData: RowData) = {
    val idByteArray = rowData("id").asInstanceOf[Array[Byte]]
    val id = AccountId.parseFrom(idByteArray)

    val rating = rowData("rating").asInstanceOf[Double]

    val userInfoByteArray = rowData("userInfo").asInstanceOf[Array[Byte]]
    val userInfo = UserInfoDTO.parseFrom(userInfoByteArray)

    TopUser(id, rating, userInfo)
  }

  override def receive = logged {
    case GetTop ⇒
      val ref = sender()
      pool.sendQuery("SELECT id, rating, userInfo FROM account_state ORDER BY rating DESC LIMIT 5;").map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            ref ! Top(resultSet.map(`rowData→topUser`).toList)
          case None ⇒
            log.error("Get top: get none " + queryResult)
        }
      )

    case DeleteAccount(accountId) ⇒
      val ref = sender()
      pool.sendPreparedStatement("DELETE FROM account_state WHERE id=?;", Seq(accountId.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            pool.sendPreparedStatement("DELETE FROM tutor_state WHERE id=?;", Seq(accountId.toByteArray)).map(
              queryResult ⇒
                if (queryResult.rowsAffected == 1)
                  ref ! AccountDeleted(accountId)
                else
                  log.error("Delete tutor state: invalid rows affected count " + queryResult)
            )
          else
            log.error("Delete account: invalid rows affected count " + queryResult)
      )

    case Insert(accountId, accountState, userInfo, tutorState) ⇒
      val ref = sender()

      pool.sendPreparedStatement("INSERT INTO account_state (id,rating,state,userInfo) VALUES (?,?,?,?);", Seq(accountId.toByteArray, accountState.rating, accountState.toByteArray, userInfo.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            pool.sendPreparedStatement("INSERT INTO tutor_state (id,state) VALUES (?,?);", Seq(accountId.toByteArray, tutorState.toByteArray)).map(
              queryResult ⇒
                if (queryResult.rowsAffected == 1)
                  ref ! AccountStateResponse(accountId, accountState)
                else
                  log.error("Insert tutor state: invalid rows affected count " + queryResult)
            )
          else
            log.error("Insert account state: invalid rows affected count " + queryResult)
      )

    case UpdateAccountState(accountId, accountState) ⇒
      val ref = sender()
      pool.sendPreparedStatement("UPDATE account_state SET rating=?,state=? WHERE id=?;", Seq(accountState.rating, accountState.toByteArray, accountId.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1)
            ref ! AccountStateResponse(accountId, accountState)
          else
            log.error("Update account state: invalid rows affected count " + queryResult)
      )

    case UpdateUserInfo(accountId, userInfo) ⇒
      pool.sendPreparedStatement("UPDATE account_state SET userInfo=? WHERE id=?;", Seq(userInfo.toByteArray, accountId.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1) {
            // ok
          } else
            log.error("Update user info: invalid rows affected count " + queryResult)
      )

    case UpdateTutorState(accountId, tutorState) ⇒
      pool.sendPreparedStatement("UPDATE tutor_state SET state=? WHERE id=?;", Seq(tutorState.toByteArray, accountId.toByteArray)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1) {
            // ok
          } else
            log.error("Update tutor state: invalid rows affected count " + queryResult)
      )

    case GetAccountState(accountId) ⇒
      val ref = sender()
      pool.sendPreparedStatement("SELECT state FROM account_state WHERE id=?;", Seq(accountId.toByteArray)).map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            if (resultSet.size == 0)
              ref ! AccountNoExists
            else if (resultSet.size == 1) {
              val row: RowData = resultSet.head

              val byteArray = row("state").asInstanceOf[Array[Byte]]
              val state = AccountStateDTO.parseFrom(byteArray)

              ref ! AccountStateResponse(accountId, state)
            } else
              log.error("Get account state: invalid result rows count = " + resultSet.size)

          case None ⇒
            log.error("Get account state: get none " + queryResult)
        }
      )

    case GetTutorState(accountId) ⇒
      val ref = sender()
      pool.sendPreparedStatement("SELECT state FROM tutor_state WHERE id=?;", Seq(accountId.toByteArray)).map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            if (resultSet.size == 1) {
              val row: RowData = resultSet.head

              val byteArray = row("state").asInstanceOf[Array[Byte]]
              val state = TutorStateDTO.parseFrom(byteArray)

              ref ! TutorStateResponse(accountId, state)
            } else
              log.error("Get tutor state: invalid result rows count = " + resultSet.size)

          case None ⇒
            log.error("Get tutor state: get none " + queryResult)
        }
      )

    case statAction: StatAction ⇒
      pool.sendPreparedStatement("UPDATE stat SET count=count+1 WHERE action=?;", Seq(statAction.id)).map(
        queryResult ⇒
          if (queryResult.rowsAffected == 1) {
            // ok
          } else
            log.error("Update stat: invalid rows affected count " + queryResult)
      )
  }
}
