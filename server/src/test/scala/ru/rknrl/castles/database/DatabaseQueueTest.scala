//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

import akka.actor.ActorRef
import akka.testkit.TestProbe
import protos.AccountType.DEV
import protos.{AccountId, TutorState}
import ru.rknrl.castles.database.Database.{GetTutorState, TutorStateResponse}
import ru.rknrl.test.ActorsTest

class DatabaseQueueTest extends ActorsTest {

  def newDatabaseQueue(database: ActorRef) = system.actorOf(DatabaseQueue.props(database))

  "queue" in {
    val database = new TestProbe(system)
    val client1 = new TestProbe(system)
    val client2 = new TestProbe(system)
    val queue = newDatabaseQueue(database.ref)

    val accountId1 = AccountId(DEV, "1")
    val accountId2 = AccountId(DEV, "2")

    client1.send(queue, GetTutorState(accountId1))
    database.expectMsg(GetTutorState(accountId1))

    client2.send(queue, GetTutorState(accountId1))
    database.expectNoMsg(noMsgTimeout)

    client1.send(queue, GetTutorState(accountId2))
    database.expectMsg(GetTutorState(accountId2))

    database.send(queue, TutorStateResponse(accountId1, None))
    client1.expectMsg(TutorStateResponse(accountId1, None))

    database.expectMsg(GetTutorState(accountId1))
    database.send(queue, TutorStateResponse(accountId1, None))
    client2.expectMsg(TutorStateResponse(accountId1, None))

    database.send(queue, TutorStateResponse(accountId2, Some(TutorState())))
    client1.expectMsg(TutorStateResponse(accountId2, Some(TutorState())))

    // accountId again

    client1.send(queue, GetTutorState(accountId1))
    database.expectMsg(GetTutorState(accountId1))
    database.send(queue, TutorStateResponse(accountId1, None))
    client1.expectMsg(TutorStateResponse(accountId1, None))
  }

}
