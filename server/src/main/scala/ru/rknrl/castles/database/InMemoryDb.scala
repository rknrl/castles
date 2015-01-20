package ru.rknrl.castles.database

import java.io.DataOutputStream

import akka.actor.Actor
import akka.util.ByteString
import ru.rknrl.StoppingStrategyActor
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.database.AccountStateDb.{Get, NoExist, Put}
import ru.rknrl.dto.AccountDTO.AccountStateDTO

class InMemoryDb extends StoppingStrategyActor {
  private var map = Map[AccountId, ByteString]()

  override def receive = {
    case put@Put(key, value) ⇒
      val builder = ByteString.newBuilder
      val os = new DataOutputStream(builder.asOutputStream)
      value.writeDelimitedTo(os)
      val byteString = builder.result()
      map = map + (key → byteString)
      sender ! put

    case Get(key) ⇒
      if (map.contains(key)) {
        val byteString = map(key)
        val is = byteString.iterator.asInputStream
        val accountStateDto = AccountStateDTO.parseDelimitedFrom(is)
        sender ! accountStateDto
      } else
        sender ! NoExist(key)
  }
}
