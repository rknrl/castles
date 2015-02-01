package ru.rknrl.castles.game.objects.players

import ru.rknrl.base.AccountId
import ru.rknrl.castles.account.objects.{Items, Skills, Slots}
import ru.rknrl.dto.CommonDTO.UserInfoDTO
import ru.rknrl.dto.GameDTO.{PlayerDTO, PlayerIdDTO}

class PlayerId(val id: Int) {
  override def equals(obj: Any) = obj match {
    case playerId: PlayerId ⇒ playerId.id == id
    case _ ⇒ false
  }

  override def hashCode = id.hashCode

  def dto = PlayerIdDTO.newBuilder().setId(id).build()
}

class Player(val id: PlayerId,
             val accountId: AccountId,
             val userInfo: UserInfoDTO,
             val slots: Slots,
             val skills: Skills,
             val items: Items,
             val isBot: Boolean) {
  def dto = PlayerDTO.newBuilder().setId(id.dto).build()
}

class Players(val players: Map[PlayerId, Player]) {
  def apply(id: PlayerId) = players(id)

  def dto = for ((id, player) ← players) yield player.dto
}
