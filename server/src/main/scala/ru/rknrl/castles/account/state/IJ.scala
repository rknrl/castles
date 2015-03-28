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

class IJ(val i: Int, val j: Int) {
  override def equals(obj: scala.Any): Boolean = obj match {
    case that: IJ ⇒ this.i == that.i && this.j == that.j
    case _ ⇒ false
  }

  def toXY = new Point((i + 0.5) * CellSize.SIZE_VALUE, (j + 0.5) * CellSize.SIZE_VALUE)

  def leftTopXY = new Point(i * CellSize.SIZE_VALUE, j * CellSize.SIZE_VALUE)
}