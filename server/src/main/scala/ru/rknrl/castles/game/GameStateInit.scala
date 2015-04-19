//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game

import ru.rknrl.Assertion
import ru.rknrl.castles.account.IJ
import ru.rknrl.castles.game.GameArea._
import ru.rknrl.castles.game.state._
import ru.rknrl.dto.{SlotsOrientation, SlotsPosDTO}

object GameStateInit {
  def getPlayerBuildings(players: List[Player], playersSlotsPositions: PlayerIdToSlotsPositions, buildingIdIterator: BuildingIdIterator, config: GameConfig) =
    for (player ← players;
         (slotId, buildingPrototype) ← player.slots
         if buildingPrototype.isDefined)
      yield {
        val ij = playersSlotsPositions(player.id.id)(slotId)
        val xy = ij.centerXY

        val stat = config.units(buildingPrototype.get) * player.stat

        val prototype = buildingPrototype.get
        new Building(
          id = buildingIdIterator.next,
          buildingPrototype = prototype,
          pos = xy,
          count = config.startCount(prototype),
          owner = Some(player),
          strengthening = None,
          lastShootTime = 0,
          buildingStat = stat)
      }

  def slotsPosDto(players: List[Player], positions: Map[Int, IJ], orientations: Map[Int, SlotsOrientation]) =
    for (player ← players)
      yield {
        val id = player.id.id
        val pos = positions(id)
        SlotsPosDTO(
          player.id,
          pos.centerXY.dto,
          orientations(id)
        )
      }

  def init(time: Long, players: List[Player], big: Boolean, isTutor: Boolean, config: GameConfig, gameMap: GameMap) = {
    if (big)
      Assertion.check(players.size == 4)
    else
      Assertion.check(players.size == 2)

    val gameArea = GameArea(big)

    val slotsPositions = gameArea.slotsPositions

    val playersSlotsPositions = gameArea.getPlayersSlotPositions(slotsPositions)

    val buildingIdIterator = new BuildingIdIterator

    val playersBuildings = getPlayerBuildings(players, playersSlotsPositions, buildingIdIterator, config)

    val buildings = playersBuildings ++ gameMap.buildings(gameArea, buildingIdIterator, config)

    val slotsPos = slotsPosDto(players, slotsPositions, gameArea.playerIdToOrientation)

    new GameState(
      time = time,
      width = gameArea.width,
      height = gameArea.height,
      players = players.map(p ⇒ p.id → p).toMap,
      buildings = buildings,
      units = List.empty,
      fireballs = List.empty,
      tornadoes = List.empty,
      volcanoes = List.empty,
      bullets = List.empty,
      items = new GameItems(players.map(p ⇒ p.id → GameItems.init(p.items)).toMap),
      unitIdIterator = new UnitIdIterator,
      slotsPos = slotsPos,
      assistancePositions = gameArea.assistancePositions,
      config = config
    )
  }
}
