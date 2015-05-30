//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef, Props}
import ru.rknrl.castles.database.Database.{UpdateTutorState, UpdateAccountState, UpdateRating}
import ru.rknrl.castles.database.DatabaseTransaction.Request
import ru.rknrl.castles.matchmaking.{Top, TopUser}
import ru.rknrl.dto.{AccountId, AccountStateDTO, TutorStateDTO}
import ru.rknrl.logging.ActorLog

class LRU[K, V](capacity: Int) extends java.util.LinkedHashMap[K, V](capacity, 0.7f, true) {
  override def removeEldestEntry(entry: java.util.Map.Entry[K, V]): Boolean = {
    size() > capacity
  }

  def apply(key: K): V = get(key)
}

object DatabaseCache {
  def props(database: ActorRef) = Props(classOf[DatabaseCache], database)
}

class DatabaseCache(database: ActorRef) extends Actor with ActorLog {

  case class RatingKey(weekNumber: Int, accountId: AccountId)

  val capacity = 100
  val accountStates = new LRU[AccountId, Option[AccountStateDTO]](capacity)
  val tutorStates = new LRU[AccountId, Option[TutorStateDTO]](capacity)
  val ratings = new LRU[RatingKey, Option[Double]](capacity)
  var tops = new LRU[Int, Top](2)

  var client: Option[ActorRef] = None

  def receive = logged {
    case msg@Database.GetAccountState(accountId) ⇒
      client = Some(sender)
      if (accountStates containsKey accountId)
        send(sender, Database.AccountStateResponse(accountId, accountStates(accountId)))
      else
        send(database, msg)

    case msg@Database.GetTutorState(accountId) ⇒
      client = Some(sender)
      if (tutorStates containsKey accountId)
        send(sender, Database.TutorStateResponse(accountId, tutorStates(accountId)))
      else
        send(database, msg)

    case msg@Database.GetRating(accountId, weekNumber) ⇒
      client = Some(sender)
      val key = RatingKey(weekNumber, accountId)
      if (ratings containsKey key)
        send(sender, Database.RatingResponse(accountId, weekNumber, ratings(key)))
      else
        send(database, msg)

    case msg@Database.GetTop(weekNumber) ⇒
      client = Some(sender)
      if (tops containsKey weekNumber)
        send(sender, tops(weekNumber))
      else
        send(database, msg)

    case msg@Database.AccountStateResponse(accountId, state) ⇒
      accountStates.put(accountId, state)
      send(client.get, msg)

    case msg@Database.TutorStateResponse(accountId, state) ⇒
      tutorStates.put(accountId, state)
      send(client.get, msg)

    case msg@Database.RatingResponse(accountId, weekNumber, rating) ⇒
      ratings.put(RatingKey(weekNumber, accountId), rating)
      send(client.get, msg)

    case top: Top ⇒
      tops.put(top.weekNumber, top)
      send(client.get, top)

    case msg@UpdateRating(accountId, weekNumber, newRating, userInfo) ⇒
      client = Some(sender)
      if (tops containsKey weekNumber) {
        val top = tops(weekNumber)
        tops.put(weekNumber, top.insert(TopUser(accountId, newRating, userInfo)))
      }
      send(database, msg)

    case msg:UpdateTutorState ⇒
      client = Some(sender)
      send(database, msg)

    case msg:UpdateAccountState ⇒
      client = Some(sender)
      send(database, msg)

    case msg ⇒
      forward(database, msg)
  }
}
