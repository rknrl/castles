//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef, Props}
import protos._
import ru.rknrl.castles.database.Database.{UpdateAccountState, UpdateRating, UpdateTutorState}
import ru.rknrl.castles.matchmaking.{Top, TopUser}
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
  val accountStates = new LRU[AccountId, Option[AccountState]](capacity)
  val tutorStates = new LRU[AccountId, Option[TutorState]](capacity)
  val ratings = new LRU[RatingKey, Option[Double]](capacity)
  var tops = new LRU[Int, Top](2)

  var accountIdToSender = Map.empty[AccountId, ActorRef]
  var topSenders = List.empty[ActorRef]

  def receive = logged {
    case msg@Database.GetTop(weekNumber) ⇒
      if (tops containsKey weekNumber)
        send(sender, tops(weekNumber))
      else {
        topSenders = topSenders :+ sender
        if (topSenders.size == 1) send(database, msg)
      }

    case top: Top ⇒
      tops.put(top.weekNumber, top)
      topSenders.foreach(send(_, top))
      topSenders = List.empty

    case msg@Database.GetAccountState(accountId) ⇒
      if (accountStates containsKey accountId)
        send(sender, Database.AccountStateResponse(accountId, accountStates(accountId)))
      else {
        accountIdToSender = accountIdToSender + (accountId → sender)
        send(database, msg)
      }

    case msg@Database.GetTutorState(accountId) ⇒
      if (tutorStates containsKey accountId)
        send(sender, Database.TutorStateResponse(accountId, tutorStates(accountId)))
      else {
        accountIdToSender = accountIdToSender + (accountId → sender)
        send(database, msg)
      }

    case msg@Database.GetRating(accountId, weekNumber) ⇒
      val key = RatingKey(weekNumber, accountId)
      if (ratings containsKey key)
        send(sender, Database.RatingResponse(accountId, weekNumber, ratings(key)))
      else {
        accountIdToSender = accountIdToSender + (accountId → sender)
        send(database, msg)
      }

    case msg@Database.AccountStateResponse(accountId, state) ⇒
      accountStates.put(accountId, state)
      send(accountIdToSender(accountId), msg)
      accountIdToSender = accountIdToSender - accountId

    case msg@Database.TutorStateResponse(accountId, state) ⇒
      tutorStates.put(accountId, state)
      send(accountIdToSender(accountId), msg)
      accountIdToSender = accountIdToSender - accountId

    case msg@Database.RatingResponse(accountId, weekNumber, rating) ⇒
      ratings.put(RatingKey(weekNumber, accountId), rating)
      send(accountIdToSender(accountId), msg)
      accountIdToSender = accountIdToSender - accountId

    case msg@UpdateRating(accountId, weekNumber, newRating, userInfo) ⇒
      if (tops containsKey weekNumber) {
        val top = tops(weekNumber)
        tops.put(weekNumber, top.insert(TopUser(accountId, newRating, userInfo)))
      }
      accountIdToSender = accountIdToSender + (accountId → sender)
      send(database, msg)

    case msg: UpdateTutorState ⇒
      accountIdToSender = accountIdToSender + (msg.accountId → sender)
      send(database, msg)

    case msg: UpdateAccountState ⇒
      accountIdToSender = accountIdToSender + (msg.accountId → sender)
      send(database, msg)

    case msg ⇒
      forward(database, msg)
  }
}
