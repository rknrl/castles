//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.players

import ru.rknrl.castles.AccountId
import ru.rknrl.castles.account.state.{Items, Slots}
import ru.rknrl.castles.game.state.Stat
import ru.rknrl.dto.CommonDTO.UserInfoDTO
import ru.rknrl.dto.GameDTO.PlayerIdDTO

class PlayerId private(val id: Int) {
  override def equals(obj: Any) = obj match {
    case playerId: PlayerId ⇒ playerId.id == id
    case _ ⇒ false
  }

  override def hashCode = id.hashCode

  def dto = PlayerIdDTO.newBuilder().setId(id).build()
}

object PlayerId {
  def apply(id: Int) = new PlayerId(id)
}

class Player(val id: PlayerId,
             val accountId: AccountId,
             val userInfo: UserInfoDTO,
             val slots: Slots,
             val stat: Stat,
             val items: Items,
             val isBot: Boolean)

class Players(val players: Map[PlayerId, Player]) {
  def apply(id: PlayerId) = players(id)
}
