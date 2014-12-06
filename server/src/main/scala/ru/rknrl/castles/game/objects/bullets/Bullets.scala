package ru.rknrl.castles.game.objects.bullets

import ru.rknrl.castles.game.GameConfig
import ru.rknrl.castles.game.objects.buildings.{Building, Buildings}
import ru.rknrl.castles.game.objects.players.{PlayerStates, Players}
import ru.rknrl.castles.game.objects.units.{GameUnit, GameUnits}
import ru.rknrl.utils.{PeriodObjectCollection, Point}
import ru.rknrl.castles.rmi.b2c.AddBulletMsg
import ru.rknrl.dto.GameDTO.BulletDTO

object Bullets {
  type Bullets = PeriodObjectCollection[BulletDTO, Bullet]

  def getNearestUnits(buildingPos: Point, units: GameUnits, time: Long, config: GameConfig, shootRadius: Double) =
    for (u ← units.units;
         unitPos = u.getPos(time)
         if buildingPos.distance(unitPos) < shootRadius)
    yield u

  def `option→bullets`(list: Iterable[Option[Bullet]]) =
    list.filter { case Some(b) ⇒ true case _ ⇒ false}.map { case Some(b) ⇒ b}

  def createBullets(buildings: Buildings, units: GameUnits, time: Long, config: GameConfig, playerState: PlayerStates) =
    `option→bullets`(createOptionBullets(buildings, units, time, config, playerState))

  def createOptionBullets(buildings: Buildings, units: GameUnits, time: Long, config: GameConfig, playerStates: PlayerStates) =
    for (b ← buildings.canShoot(time, config, playerStates);
         playerState = if (b.owner.isDefined) Some(playerStates(b.owner.get)) else None;
         shootRadius = config.shootRadius(b, playerState);
         nearestUnits = getNearestUnits(b.pos, units, time, config, shootRadius)
         if nearestUnits.size > 0)
    yield {
      val units = filterNearest(b, nearestUnits, time, config)

      if (units.isEmpty)
        None
      else
        Some(units.head)
    }

  private def filterNearest(b: Building, nearestUnits: Iterable[GameUnit], time: Long, config: GameConfig) =
    nearestUnits
      .map(createBullet(b, _, time, config))
      .filter(bullet ⇒ bullet.unit.getPos(time + bullet.duration) != bullet.unit.endPos)

  def createBullet(b: Building, unit: GameUnit, time: Long, config: GameConfig) = {
    val unitPos = unit.getPos(time)
    val duration = b.pos.duration(unitPos, config.bulletSpeed)
    new Bullet(b, unit, startTime = time, duration = duration.toLong)
  }

  def `bullets→addMessage`(bullets: Iterable[Bullet], time: Long) =
    bullets.map(b ⇒ AddBulletMsg(b.dto(time)))
}
