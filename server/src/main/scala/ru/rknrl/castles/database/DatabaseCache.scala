//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef}
import ru.rknrl.castles.database.DatabaseTransaction.Request
import ru.rknrl.dto.{AccountId, AccountStateDTO, TutorStateDTO}
import ru.rknrl.logging.ActorLog

class LRU[K, V](capacity: Int) extends java.util.LinkedHashMap[K, V](capacity, 0.7f, true) {
  override def removeEldestEntry(entry: java.util.Map.Entry[K, V]): Boolean = {
    size() > capacity
  }

  def apply(key: K): V = get(key)
}

class DatabaseCache(database: ActorRef) extends Actor with ActorLog {

  val capacity = 100
  val accountStates = new LRU[AccountId, Option[AccountStateDTO]](capacity)
  val tutorStates = new LRU[AccountId, Option[TutorStateDTO]](capacity)
  val ratings = new LRU[AccountId, Option[Double]](capacity)

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

    case msg@Database.GetRating(accountId) ⇒
      client = Some(sender)
      if (ratings containsKey accountId)
        send(sender, Database.RatingResponse(accountId, ratings(accountId)))
      else
        send(database, msg)

    case msg@Database.AccountStateResponse(accountId, state) ⇒
      accountStates.put(accountId, state)
      send(client.get, msg)

    case msg@Database.TutorStateResponse(accountId, state) ⇒
      tutorStates.put(accountId, state)
      send(client.get, msg)

    case msg@Database.RatingResponse(accountId, rating) ⇒
      ratings.put(accountId, rating)
      send(client.get, msg)

    case msg: Request ⇒
      client = Some(sender)
      send(database, msg)

    case msg ⇒
      forward(database, msg)
  }
}
