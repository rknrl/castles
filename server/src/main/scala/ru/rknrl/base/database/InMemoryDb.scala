package ru.rknrl.base.database

import ru.rknrl.StoppingStrategyActor
import ru.rknrl.base.AccountId
import ru.rknrl.base.MatchMaking.TopItem
import ru.rknrl.base.database.AccountStateDb._
import ru.rknrl.dto.AccountDTO.AccountStateDTO

class InMemoryDb(config: Any) extends StoppingStrategyActor {
  private var map = Map[AccountId, Array[Byte]]()

  override def receive = {
    case GetTop ⇒ sender() ! List.empty[TopItem]

    case Insert(accountId, accountState) ⇒
      if (map.contains(accountId)) throw new Error("db already has accountId=" + accountId)
      map = map + (accountId → accountState.toByteArray)
      sender ! accountState

    case Update(accountId, accountState) ⇒
      if (!map.contains(accountId)) throw new Error("db hasn't accountId=" + accountId)
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
