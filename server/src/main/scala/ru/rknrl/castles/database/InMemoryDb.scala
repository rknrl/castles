package ru.rknrl.castles.database

import ru.rknrl.StoppingStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.castles.database.AccountStateDb.{Get, Insert, NoExist, Update}
import ru.rknrl.dto.AccountDTO.AccountStateDTO

class InMemoryDb extends StoppingStrategyActor {
  private var map = Map[AccountId, Array[Byte]]()

  override def receive = {
    case insert@Insert(key, value) ⇒
      map = map + (key → value.toByteArray)
      sender ! value

    case update@Update(key, value) ⇒
      map = map + (key → value.toByteArray)
      sender ! update

    case Get(key) ⇒
      if (map.contains(key)) {
        val byteArray = map(key)
        val accountStateDto = AccountStateDTO.parseFrom(byteArray)
        sender ! accountStateDto
      } else
        sender ! NoExist
  }
}
