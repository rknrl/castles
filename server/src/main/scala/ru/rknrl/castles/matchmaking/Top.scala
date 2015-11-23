//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.matchmaking

import protos.{AccountId, TopUserInfo, UserInfo}

case class TopUser(accountId: AccountId,
                   rating: Double,
                   info: UserInfo)

case class Top(users: Seq[TopUser], weekNumber: Int) {
  def insert(user: TopUser) =
    new Top(
      (users.filter(_.accountId != user.accountId) :+ user)
        .sortBy(_.rating)(Ordering.Double.reverse)
        .take(5),
      weekNumber
    )

  private def usersDto =
    for (i ← users.indices)
      yield TopUserInfo(
        place = i + 1,
        info = users(i).info
      )

  def dto = protos.Top(weekNumber, usersDto)
}
