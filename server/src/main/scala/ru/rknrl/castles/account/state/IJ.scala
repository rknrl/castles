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

class IJ private (val i: Int, val j: Int) {
  override def equals(obj: scala.Any): Boolean = obj match {
    case that: IJ ⇒ this.i == that.i && this.j == that.j
    case _ ⇒ false
  }

  val cellSize = CellSize.SIZE_VALUE

  def centerXY = Point((i + 0.5) * cellSize, (j + 0.5) * cellSize)

  def leftTopXY = Point(i * cellSize, j * cellSize)
}

object IJ {
  def apply(i: Int, j: Int) = new IJ(i, j)
}