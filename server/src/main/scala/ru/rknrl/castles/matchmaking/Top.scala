//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import ru.rknrl.dto.{TopDTO, AccountId, TopUserInfoDTO, UserInfoDTO}

case class TopUser(accountId: AccountId,
                   rating: Double,
                   info: UserInfoDTO)

case class Top(users: Seq[TopUser], weekNumber: Int) {
  def insert(user: TopUser) =
    new Top(
      (users.filter(_.accountId != user.accountId) :+ user)
        .sortBy(_.rating)(Ordering.Double.reverse)
        .take(5),
      weekNumber
    )

  private def usersDto =
    for (i ← 0 until users.size)
      yield TopUserInfoDTO(
        place = i + 1,
        info = users(i).info
      )

  def dto = TopDTO(weekNumber, usersDto)
}
