//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.players

import ru.rknrl.castles.account.state.Item.Items
import ru.rknrl.castles.account.state.Slots.Slots
import ru.rknrl.castles.game.state.Stat
import ru.rknrl.dto.CommonDTO.{AccountIdDTO, UserInfoDTO}
import ru.rknrl.dto.GameDTO.PlayerIdDTO

object PlayerId {
  def apply(id: Int) = PlayerIdDTO.newBuilder.setId(id).build
}

class Player(val id: PlayerIdDTO,
             val accountId: AccountIdDTO,
             val userInfo: UserInfoDTO,
             val slots: Slots,
             val stat: Stat,
             val items: Items,
             val isBot: Boolean)