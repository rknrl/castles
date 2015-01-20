package ru.rknrl.castles.database

import java.io.DataOutputStream

import akka.util.ByteString
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{Configuration, QueryResult, RowData}
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.castles.database.AccountStateDb.{Get, NoExist, Put}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class DbConfiguration(username: String,
                      host: String,
                      port: Int,
                      password: String,
                      database: String) {
  def configuration = new Configuration(username, host, port, Some(password), Some(database))
}

class MySqlDb(configuration: DbConfiguration) extends StoppingStrategyActor {
  val connection = new MySQLConnection(configuration.configuration)

  Await.result(connection.connect, 5 seconds)

  override def receive: Receive = {
    case put@Put(key, value) ⇒
      val builder = ByteString.newBuilder
      val os = new DataOutputStream(builder.asOutputStream)
      value.writeDelimitedTo(os)
      val byteString = builder.result()

      val future: Future[QueryResult] = connection.sendQuery("SELECT 0")

      val mapResult: Future[Any] = future.map(
        queryResult => queryResult.rows match {
          case Some(resultSet) =>
            val row: RowData = resultSet.head
            row(0)
            sender ! put

          case None => -1
        }
      )

    case Get(key) ⇒
      val future: Future[QueryResult] = connection.sendQuery("SELECT 0")

      val mapResult: Future[Any] = future.map(
        queryResult => queryResult.rows match {
          case Some(resultSet) =>
            val row: RowData = resultSet.head
            row(0)

          /*
                      val byteString = map(key)
                      val is = byteString.iterator.asInputStream
                      val accountStateDto = AccountStateDTO.parseDelimitedFrom(is)
                      sender ! accountStateDto
          */

          case None => -1
            sender ! NoExist(key)
        }
      )
  }
}
