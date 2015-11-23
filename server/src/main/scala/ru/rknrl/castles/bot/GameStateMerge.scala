//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.bot

import protos._

object GameStateMerge {
  def merge(gameState: GameState, gameStateUpdate: GameStateUpdate) = {
    GameState(
      gameState.width,
      gameState.height,
      gameState.players,
      gameState.slots,
      gameState.selfId,
      mergeBuildings(gameState.buildings, gameStateUpdate.buildingUpdates),
      gameState.units,
      gameState.fireballs,
      gameState.tornadoes,
      gameState.volcanoes,
      gameState.bullets,
      mergeItemStates(gameState.itemStates, gameStateUpdate.itemStatesUpdates.find(_.playerId == gameState.selfId)),
      gameState.gameOvers
    )
  }

  private def mergeBuildings(buildings: Seq[BuildingDTO], updates: Seq[BuildingUpdate]) =
    buildings.map(b ⇒ mergeBuilding(b, updates.find(_.id == b.id)))

  private def mergeBuilding(building: BuildingDTO, update: Option[BuildingUpdate]) =
    if (update.isDefined)
      BuildingDTO(
        id = building.id,
        building = building.building,
        pos = building.pos,
        population = update.get.population,
        owner = update.get.owner,
        strengthened = update.get.strengthened
      )
    else
      building

  private def mergeItemStates(itemStates: ItemStatesDTO, updates: Option[ItemStatesDTO]) =
    if (updates.isDefined)
      ItemStatesDTO(
        playerId = itemStates.playerId,
        items = itemStates.items.map(i ⇒ mergeItemState(i, updates.get.items.find(_.itemType == i.itemType)))
      )
    else
      itemStates

  private def mergeItemState(itemState: ItemStateDTO, update: Option[ItemStateDTO]) =
    if (update.isDefined)
      update.get
    else
      itemState
}