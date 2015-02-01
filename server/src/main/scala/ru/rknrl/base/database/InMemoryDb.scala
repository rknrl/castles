package ru.rknrl.base.database

import ru.rknrl.StoppingStrategyActor
import ru.rknrl.base.MatchMaking.TopItem
import ru.rknrl.base.database.AccountStateDb._
import ru.rknrl.dto.AccountDTO.AccountStateDTO
import ru.rknrl.dto.CommonDTO.AccountIdDTO

class InMemoryDb(config: Any) extends StoppingStrategyActor {
  private var map = Map[AccountIdDTO, Array[Byte]]()

  override def receive = {
    case GetTop ⇒ sender() ! List.empty[TopItem]

    case Insert(accountId, accountState, userInfo) ⇒
      if (map.contains(accountId)) throw new Error("db already has accountId=" + accountId)
      map = map + (accountId → accountState.toByteArray)
      sender ! accountState

    case Update(accountId, accountState) ⇒
      if (!map.contains(accountId)) throw new Error("db hasn't accountId=" + accountId)
      map = map + (accountId → accountState.toByteArray)
      sender ! accountState

    case UpdateUserInfo(accountId, userInfo) ⇒
      if (!map.contains(accountId)) throw new Error("db hasn't accountId=" + accountId)
      sender ! userInfo

    case Get(accountId) ⇒
      if (map.contains(accountId)) {
        val byteArray = map(accountId)
        val accountStateDto = AccountStateDTO.parseFrom(byteArray)
        sender ! StateResponse(accountId, accountStateDto)
      } else
        sender ! NoExist
  }
}
