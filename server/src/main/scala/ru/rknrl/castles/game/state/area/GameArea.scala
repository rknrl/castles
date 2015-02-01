package ru.rknrl.castles.game.state.area

import ru.rknrl.castles.account.state.{IJ, Slots}
import ru.rknrl.castles.game.state.area.GameArea.PlayerIdToSlotsPositions
import ru.rknrl.dto.CommonDTO.SlotId
import ru.rknrl.dto.GameDTO.{CellSize, SlotsOrientation}
import ru.rknrl.utils.Point

object GameArea {
  type SlotsPositions = Map[SlotId, IJ]
  type PlayerIdToSlotsPositions = Map[Int, SlotsPositions]

  def apply(big: Boolean) = if (big) new GameAreaBig else new GameAreaSmall

  def toIJs(playerIdToSlotsPositions: PlayerIdToSlotsPositions) =
    playerIdToSlotsPositions.flatMap { case (playerId, slotsPos) ⇒ slotsPos.map { case (slotId, ij) ⇒ ij}}
}

trait GameArea {
  def h: Int

  def v: Int

  def width = h * CellSize.SIZE_VALUE

  def height = v * CellSize.SIZE_VALUE

  def randomSlotsPositions: Map[Int, IJ]

  def playerIdToOrientation: Map[Int, SlotsOrientation]

  def mirrorH(pos: Point) = new Point(width - pos.x, pos.y)

  def mirrorV(pos: Point) = new Point(pos.x, height - pos.y)

  protected def mirrorH(pos: IJ) = new IJ(h - 1 - pos.i, pos.j)

  protected def mirrorV(pos: IJ) = new IJ(pos.i, v - 1 - pos.j)

  private def getPlayerSlotsPositions(playerId: Int, slotsPos: IJ) =
    for (slotId ← SlotId.values();
         slotPos = Slots.positions(slotId);

         orientation = playerIdToOrientation(playerId);
         isMirrorH = orientation == SlotsOrientation.TOP_RIGHT || orientation == SlotsOrientation.BOTTOM_RIGHT;
         isMirrorV = orientation == SlotsOrientation.TOP_LEFT || orientation == SlotsOrientation.TOP_RIGHT;

         i = if (isMirrorH) slotsPos.i - slotPos.i else slotsPos.i + slotPos.i;
         j = if (isMirrorV) slotsPos.j - slotPos.j else slotsPos.j + slotPos.j)
    yield slotId → new IJ(i, j)


  def getPlayersSlotPositions(slotsPos: Map[Int, IJ]): PlayerIdToSlotsPositions =
    for ((playerId, pos) ← slotsPos)
    yield playerId → getPlayerSlotsPositions(playerId, pos).toMap
}

/**
 * 2 players, phones
 */
class GameAreaSmall extends GameArea {
  val h = 8
  val v = 11

  private val hForRandom = h - 1 - Slots.left - Slots.right
  private val vForRandom = Math.floor((v - 1) / 2).toInt - Slots.top - Slots.bottom

  def randomSlotsPositions = {
    val pos = new IJ(
      i = Math.round(Math.random() * hForRandom).toInt + Slots.left, // [2,5]
      j = 0
    )

    Map(
      0 → pos,
      1 → mirrorH(mirrorV(pos))
    )
  }

  val playerIdToOrientation = Map(
    0 → SlotsOrientation.TOP_LEFT,
    1 → SlotsOrientation.BOTTOM_RIGHT
  )
}

/**
 * 4 players, tables and canvas
 */
class GameAreaBig extends GameArea {
  val h = 15
  val v = 15

  def randomSlotsPositions = {
    val pos = new IJ(
      i = 2,
      j = 0
    )

    Map(
      0 → pos,
      1 → mirrorH(pos),
      2 → mirrorV(pos),
      3 → mirrorH(mirrorV(pos))
    )
  }

  val playerIdToOrientation = Map(
    0 → SlotsOrientation.TOP_LEFT,
    1 → SlotsOrientation.TOP_RIGHT,
    2 → SlotsOrientation.BOTTOM_LEFT,
    3 → SlotsOrientation.BOTTOM_RIGHT
  )
}
