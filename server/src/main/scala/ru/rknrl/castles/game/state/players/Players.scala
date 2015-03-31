//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state.players

import ru.rknrl.castles.AccountId
import ru.rknrl.castles.account.state.Items
import ru.rknrl.castles.account.state.Slots.Slots
import ru.rknrl.castles.game.state.Stat
import ru.rknrl.dto.CommonDTO.UserInfoDTO
import ru.rknrl.dto.GameDTO.PlayerIdDTO

case class PlayerId(id: Int) {
  def dto = PlayerIdDTO.newBuilder.setId(id).build
}

class Player(val id: PlayerId,
             val accountId: AccountId,
             val userInfo: UserInfoDTO,
             val slots: Slots,
             val stat: Stat,
             val items: Items,
             val isBot: Boolean)