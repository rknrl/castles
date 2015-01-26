package ru.rknrl.castles.database

import ru.rknrl.StoppingStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.castles.database.AccountStateDb.{Get, Insert, NoExist, Update}
import ru.rknrl.dto.AccountDTO.AccountStateDTO

class InMemoryDb extends StoppingStrategyActor {
  private var map = Map[AccountId, Array[Byte]]()

  override def receive = {
    case Insert(accountId, accountState) ⇒
      map = map + (accountId → accountState.toByteArray)
      sender ! accountState

    case Update(accountId, accountState) ⇒
      map = map + (accountId → accountState.toByteArray)
      sender ! accountState

    case Get(accountId) ⇒
      if (map.contains(accountId)) {
        val byteArray = map(accountId)
        val accountStateDto = AccountStateDTO.parseFrom(byteArray)
        sender ! accountStateDto
      } else
        sender ! NoExist
  }
}
