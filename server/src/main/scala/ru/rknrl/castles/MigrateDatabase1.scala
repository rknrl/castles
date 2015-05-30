//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import akka.actor.{Actor, ActorSystem, Props}
import com.github.mauricio.async.db.RowData
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.ConnectionPool
import ru.rknrl.castles.MigrateDatabase1.Migrate
import ru.rknrl.castles.database.DatabaseTransaction.RealCalendar
import ru.rknrl.castles.database.DbConfiguration
import ru.rknrl.dto.{AccountId, AccountStateDTO, UserInfoDTO}
import ru.rknrl.logging.ActorLog

object MigrateDatabase1 {
  def main(args: Array[String]) {
    implicit val system = ActorSystem("main-actor-system")
    val config = new DbConfiguration(
      username = "root",
      host = args(0),
      port = 3306,
      password = args(1),
      "castles",
      8,
      60000,
      1000
    )
    val migrate = system.actorOf(Props(classOf[MigrateDatabase1], config), "migrate-database")
    migrate ! Migrate
  }

  case object Migrate

}

class MigrateDatabase1(configuration: DbConfiguration) extends Actor with ActorLog {
  val factory = new MySQLConnectionFactory(configuration.configuration)
  val pool = new ConnectionPool(factory, configuration.poolConfiguration)

  import context.dispatcher

  def receive = logged {
    case Migrate ⇒
      val weekNumber = new RealCalendar().getCurrentWeek

      pool.sendPreparedStatement("SELECT * FROM account_state").map(
        queryResult ⇒ queryResult.rows match {
          case Some(resultSet) ⇒
            write("DROP TABLE account_state;", Seq(), 0)
            write("create table account_state(id VARBINARY(128) NOT NULL, state VARBINARY(256) NOT NULL, PRIMARY KEY (id)) engine=InnoDb;", Seq(), 0)
            write("create table ratings(id VARBINARY(128) NOT NULL, weekNumber INT, rating DOUBLE, PRIMARY KEY(id, weekNumber)) engine=InnoDb;", Seq(), 0)
            write("CREATE INDEX rating ON ratings(rating);", Seq(), 0)
            write("create table user_info(id VARBINARY(128) NOT NULL, userInfo VARBINARY(1024) NOT NULL, PRIMARY KEY (id)) engine=InnoDb;", Seq(), 0)

            println("users:" + resultSet.size)

            resultSet
              .map(rowToUserData)
              .foreach(userData ⇒ {
              write("REPLACE INTO account_state (id,state) VALUES (?,?);", Seq(userData.id.toByteArray, userData.state.toByteArray), 1)
              write("REPLACE INTO ratings (id,weekNumber,rating) VALUES (?,?,?);", Seq(userData.id.toByteArray, weekNumber, userData.rating), 1)
              write("REPLACE INTO user_info (id,userInfo) VALUES (?,?);", Seq(userData.id.toByteArray, userData.info.toByteArray), 1)
            })

          case None ⇒ log.error("Get none " + queryResult)
        }
      ) onFailure {
        case t: Throwable ⇒ log.error("select account_state error ", t)
      }
  }

  def rowToUserData(rowData: RowData) = {
    val idByteArray = rowData("id").asInstanceOf[Array[Byte]]
    val id = AccountId.parseFrom(idByteArray)

    val rating = rowData("rating").asInstanceOf[Double]

    val stateByteArray = rowData("state").asInstanceOf[Array[Byte]]
    val state = AccountStateDTO.parseFrom(stateByteArray)

    val userInfoByteArray = rowData("userInfo").asInstanceOf[Array[Byte]]
    val info = UserInfoDTO.parseFrom(userInfoByteArray)

    UserData(id, rating, state, info)
  }

  def write(query: String, values: Seq[Any], rowsAffected: Long): Unit =
    pool.sendPreparedStatement(query, values).map(
      queryResult ⇒
        if (queryResult.rowsAffected == rowsAffected) {
          // ok
        } else
          log.error("Invalid rows affected count " + queryResult)
    ) onFailure {
      case t: Throwable ⇒ log.error("Database error", t)
    }

}

case class UserData(id: AccountId,
                    rating: Double,
                    state: AccountStateDTO,
                    info: UserInfoDTO)