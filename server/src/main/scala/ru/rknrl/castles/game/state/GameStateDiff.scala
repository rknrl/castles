//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.state.GameItems._
import ru.rknrl.castles.rmi.B2C._
import ru.rknrl.core.rmi.Msg
import ru.rknrl.dto.GameStateUpdateDTO

object GameStateDiff {
  def diff(oldState: GameState, newState: GameState) = {
    val newTime = newState.time
    val units = oldState.units
    val newUnits = newState.units
    val buildings = oldState.buildings
    val newBuildings = newState.buildings
    val items = oldState.items
    val newItems = newState.items
    val createdUnits = newState.units.filter(u ⇒ !oldState.units.exists(_.id == u.id))
    val removedUnits = oldState.units.filter(u ⇒ !newState.units.exists(_.id == u.id))
    val killedUnits = removedUnits.filter(u ⇒ !u.isFinish(newTime))
    val createdFireballs = newState.fireballs.filter(f ⇒ !oldState.fireballs.exists(_ == f))
    val createdVolcanoes = newState.volcanoes.filter(v ⇒ !oldState.volcanoes.exists(_ == v))
    val createdTornadoes = newState.tornadoes.filter(t ⇒ !oldState.tornadoes.exists(_ == t))
    val createdBullets = newState.bullets.filter(b ⇒ !oldState.bullets.exists(_ == b))

    val updateDto = GameStateUpdateDTO(
      newUnits = createdUnits.map(_.dto(newTime)).toSeq,
      newFireballs = createdFireballs.map(_.dto(newTime)).toSeq,
      newVolcanoes = createdVolcanoes.map(_.dto(newTime)).toSeq,
      newTornadoes = createdTornadoes.map(_.dto(newTime)).toSeq,
      newBullets = createdBullets.map(_.dto(newTime)).toSeq,
      unitUpdates = GameUnit.getUpdateMessages(units, newUnits).map(_.unitUpdate).toSeq,
      buildingUpdates = Building.getUpdateMessages(buildings, newBuildings).map(_.building).toSeq,
      itemStatesUpdates = getUpdateItemsStatesMessages(items, newItems, oldState.config, newTime).map(_.states).toSeq,
      newGameOvers = Nil
    )

    val addUnitMessages = createdUnits.map(u ⇒ AddUnit(u.dto(newTime)))
    val addFireballMessages = createdFireballs.map(f ⇒ AddFireball(f.dto(newTime)))
    val addVolcanoMessages = createdVolcanoes.map(v ⇒ AddVolcano(v.dto(newTime)))
    val addTornadoMessages = createdTornadoes.map(t ⇒ AddTornado(t.dto(newTime)))
    val addBulletsMessages = createdBullets.map(b ⇒ AddBullet(b.dto(newTime)))

    val updateUnitMessages = GameUnit.getUpdateMessages(units, newUnits)
    val updateBuildingMessages = Building.getUpdateMessages(buildings, newBuildings)

    val killUnitMessages = killedUnits.map(u ⇒ KillUnit(u.id))

    val updateItemsStatesMessages = getUpdateItemsStatesMessages(items, newItems, oldState.config, newTime)

    val messages: Iterable[Msg] = addUnitMessages ++ addFireballMessages ++ addVolcanoMessages ++ addTornadoMessages ++ addBulletsMessages ++
      updateBuildingMessages ++ updateUnitMessages ++ killUnitMessages ++
    updateItemsStatesMessages

    messages
  }
}
