//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.account.AccountState.{Slots, Items}
import ru.rknrl.core.Stat
import ru.rknrl.dto.{AccountId, PlayerId, UserInfoDTO}

case class Player(id: PlayerId,
                  accountId: AccountId,
                  userInfo: UserInfoDTO,
                  slots: Slots,
                  stat: Stat,
                  items: Items,
                  isBot: Boolean)