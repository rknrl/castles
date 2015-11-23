//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account

import protos.CellSize
import ru.rknrl.core.points.Point

case class IJ(i: Int, j: Int) {
  val cellSize = CellSize.SIZE.id

  def centerXY = Point((i + 0.5) * cellSize, (j + 0.5) * cellSize)

  def leftTopXY = Point(i * cellSize, j * cellSize)
}