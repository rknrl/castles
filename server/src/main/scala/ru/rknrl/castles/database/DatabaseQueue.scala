//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.{Actor, ActorRef, Props}
import protos.AccountId
import ru.rknrl.logging.ActorLog

object DatabaseQueue {
  def props(database: ActorRef) = Props(classOf[DatabaseQueue], database)
}

class DatabaseQueue(database: ActorRef) extends Actor with ActorLog {

  case class QueueRequest(msg: DatabaseTransaction.Request, sender: ActorRef)

  var queues = Map.empty[AccountId, List[QueueRequest]]

  def receive = logged {
    case msg: DatabaseTransaction.Request ⇒
      val accountId = msg.accountId
      val request = QueueRequest(msg, sender)
      if (queues contains accountId) {
        val newRequests = queues(accountId) :+ request
        queues = queues + (accountId → newRequests)
      } else {
        queues = queues + (accountId → List(request))
        send(database, msg)
      }

    case msg: DatabaseTransaction.Response ⇒
      val accountId = msg.accountId
      val requests = queues(accountId)
      val request = requests.head
      if (requests.size > 1) {
        val newRequests = requests drop 1
        send(database, newRequests.head.msg)
        queues = queues + (accountId → newRequests)
      } else
        queues = queues - accountId
      send(request.sender, msg)

    case msg ⇒ forward(database, msg)
  }
}
