//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.account.state.{IJ, Slots}
import ru.rknrl.castles.game.points.Point
import ru.rknrl.castles.game.state.GameArea.PlayerIdToSlotsPositions
import ru.rknrl.castles.game.state.players.PlayerId
import ru.rknrl.dto.CommonDTO.SlotId
import ru.rknrl.dto.GameDTO.{CellSize, SlotsOrientation}

object GameArea {
  type SlotsPositions = Map[SlotId, IJ]
  type PlayerIdToSlotsPositions = Map[Int, SlotsPositions]

  def apply(big: Boolean) = if (big) new GameAreaBig else new GameAreaSmall

  def toIJs(playerIdToSlotsPositions: PlayerIdToSlotsPositions) =
    playerIdToSlotsPositions.flatMap { case (playerId, slotsPos) ⇒ slotsPos.map { case (slotId, ij) ⇒ ij } }
}

trait GameArea {
  def h: Int

  def v: Int

  def width = h * CellSize.SIZE_VALUE

  def height = v * CellSize.SIZE_VALUE

  def assistancePositions: Map[PlayerId, Point]

  def slotsPositions: Map[Int, IJ]

  def playerIdToOrientation: Map[Int, SlotsOrientation]

  def mirrorH(pos: Point) = Point(width - pos.x, pos.y)

  def mirrorV(pos: Point) = Point(pos.x, height - pos.y)

  protected def mirrorH(pos: IJ) = IJ(h - 1 - pos.i, pos.j)

  protected def mirrorV(pos: IJ) = IJ(pos.i, v - 1 - pos.j)

  private def getPlayerSlotsPositions(playerId: Int, slotsPos: IJ) =
    for (slotId ← SlotId.values();
         slotPos = Slots.positions(slotId);

         orientation = playerIdToOrientation(playerId);
         isMirrorH = orientation == SlotsOrientation.TOP_RIGHT || orientation == SlotsOrientation.BOTTOM_RIGHT;
         isMirrorV = orientation == SlotsOrientation.TOP_LEFT || orientation == SlotsOrientation.TOP_RIGHT;

         i = if (isMirrorH) slotsPos.i - slotPos.i else slotsPos.i + slotPos.i;
         j = if (isMirrorV) slotsPos.j - slotPos.j else slotsPos.j + slotPos.j)
      yield slotId → IJ(i, j)


  def getPlayersSlotPositions(slotsPos: Map[Int, IJ]): PlayerIdToSlotsPositions =
    for ((playerId, pos) ← slotsPos)
      yield playerId → getPlayerSlotsPositions(playerId, pos).toMap
}

/** 2 players, phones */
class GameAreaSmall extends GameArea {
  val h = 8
  val v = 11

  def slotsPositions = createSlotsPositions(IJ(3, 0))

  private def createSlotsPositions(top: IJ) =
    Map(
      0 → top,
      1 → mirrorH(mirrorV(top))
    )

  val playerIdToOrientation = Map(
    0 → SlotsOrientation.TOP_LEFT,
    1 → SlotsOrientation.BOTTOM_RIGHT
  )

  val assistancePositions = Map(
    PlayerId(0) → Point(0, 0),
    PlayerId(1) → IJ(h, v).centerXY
  )
}

/** 4 players, tables and canvas */
class GameAreaBig extends GameArea {
  val h = 15
  val v = 15

  val slotsPositions = createSlotsPositions(IJ(2, 0))

  private def createSlotsPositions(topLeft: IJ) =
    Map(
      0 → topLeft,
      1 → mirrorH(topLeft),
      2 → mirrorV(topLeft),
      3 → mirrorH(mirrorV(topLeft))
    )

  val playerIdToOrientation = Map(
    0 → SlotsOrientation.TOP_LEFT,
    1 → SlotsOrientation.TOP_RIGHT,
    2 → SlotsOrientation.BOTTOM_LEFT,
    3 → SlotsOrientation.BOTTOM_RIGHT
  )

  val assistancePositions = Map(
    PlayerId(0) → Point(0, 0),
    PlayerId(1) → IJ(h, 0).leftTopXY,
    PlayerId(2) → IJ(0, v).leftTopXY,
    PlayerId(3) → IJ(h, v).leftTopXY
  )
}