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

  def up = new IJ(i, j - 1)

  def upLeft = new IJ(i - 1, j - 1)

  def upRight = new IJ(i + 1, j - 1)

  def left = new IJ(i - 1, j)

  def right = new IJ(i + 1, j)

  def down = new IJ(i, j + 1)

  def downLeft = new IJ(i - 1, j + 1)

  def downRight = new IJ(i + 1, j + 1)

  def toXY = new Point((i + 0.5) * CellSize.SIZE_VALUE, (j + 0.5) * CellSize.SIZE_VALUE)

  def near(that: IJ) = up == that || upLeft == that || upRight == that ||
    this == that || left == that || right == that ||
    down == that || downLeft == that || downRight == that
}
