package ru.rknrl.castles.game

import ru.rknrl.castles.account.objects.{IJ, StartLocation}
import ru.rknrl.castles.game.GameArea._
import ru.rknrl.dto.CommonDTO.SlotId
import ru.rknrl.dto.GameDTO.{CellSize, StartLocationOrientation}
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

  def randomStartLocationPositions: Map[Int, IJ]

  def playerIdToOrientation: Map[Int, StartLocationOrientation]

  def mirrorH(pos: Point) = new Point(width - pos.x, pos.y)

  def mirrorV(pos: Point) = new Point(pos.x, height - pos.y)

  protected def mirrorH(pos: IJ) = new IJ(h - 1 - pos.i, pos.j)

  protected def mirrorV(pos: IJ) = new IJ(pos.i, v - 1 - pos.j)

  private def getPlayerSlotsPositions(playerId: Int, startLocationPos: IJ) =
    for (slotId ← SlotId.values();
         slotPos = StartLocation.positions(slotId);

         orientation = playerIdToOrientation(playerId);
         isMirrorH = orientation == StartLocationOrientation.TOP_RIGHT || orientation == StartLocationOrientation.BOTTOM_RIGHT;
         isMirrorV = orientation == StartLocationOrientation.TOP_LEFT || orientation == StartLocationOrientation.TOP_RIGHT;

         i = if (isMirrorH) startLocationPos.i - slotPos.i else startLocationPos.i + slotPos.i;
         j = if (isMirrorV) startLocationPos.j - slotPos.j else startLocationPos.j + slotPos.j)
    yield slotId → new IJ(i, j)


  def getPlayersSlotPositions(startLocationPositions: Map[Int, IJ]): PlayerIdToSlotsPositions =
    for ((playerId, pos) ← startLocationPositions)
    yield playerId → getPlayerSlotsPositions(playerId, pos).toMap
}

/**
 * 2 players, phones
 */
class GameAreaSmall extends GameArea {
  val h = 8
  val v = 12

  private val hForRandom = h - 1 - StartLocation.left - StartLocation.right
  private val vForRandom = Math.floor((v - 1) / 2).toInt - StartLocation.top - StartLocation.bottom

  def randomStartLocationPositions = {
    val pos = new IJ(
      i = Math.round(Math.random() * hForRandom).toInt + StartLocation.left, // [2,5]
      j = 0
    )

    Map(
      0 → pos,
      1 → mirrorH(mirrorV(pos))
    )
  }

  val playerIdToOrientation = Map(
    0 → StartLocationOrientation.TOP_LEFT,
    1 → StartLocationOrientation.BOTTOM_RIGHT
  )
}

/**
 * 4 players, tables and canvas
 */
class GameAreaBig extends GameArea {
  val h = 17
  val v = 17

  def randomStartLocationPositions = {
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
    0 → StartLocationOrientation.TOP_LEFT,
    1 → StartLocationOrientation.TOP_RIGHT,
    2 → StartLocationOrientation.BOTTOM_LEFT,
    3 → StartLocationOrientation.BOTTOM_RIGHT
  )
}
