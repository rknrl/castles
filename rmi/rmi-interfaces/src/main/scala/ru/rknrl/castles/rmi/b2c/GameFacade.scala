package ru.rknrl.castles.rmi.b2c

import ru.rknrl.dto.GameDTO._

abstract class GameFacade {
  def updateBuilding(building: BuildingUpdateDTO)

  def updateItemStates(states: ItemsStateDTO)

  def addUnit(unit: UnitDTO)

  def updateUnit(unit: UnitUpdateDTO)

  def removeUnit(id: UnitIdDTO)

  def addFireball(fireball: FireballDTO)

  def addVolcano(volcano: VolcanoDTO)

  def addTornado(tornado: TornadoDTO)

  def addBullet(bullet: BulletDTO)

  def gameOver(dto: GameOverDTO)
}
