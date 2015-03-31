//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import ru.rknrl.castles.game.points.Point
import ru.rknrl.dto.GameDTO.CellSize

case class IJ(i: Int, j: Int) {
  val cellSize = CellSize.SIZE_VALUE

  def centerXY = Point((i + 0.5) * cellSize, (j + 0.5) * cellSize)

  def leftTopXY = Point(i * cellSize, j * cellSize)
}