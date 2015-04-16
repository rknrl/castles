//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.Stat
import ru.rknrl.core.points.Point
import ru.rknrl.dto.BuildingLevel.{LEVEL_1, LEVEL_2}
import ru.rknrl.dto.BuildingType.{HOUSE, TOWER}
import ru.rknrl.dto.{BuildingId, BuildingPrototype, PlayerId}

// todo: dto, updateDto
class BuildingTest extends WordSpec with Matchers {

  "copy equals" in {
    val b = buildingMock()
    checkBuilding(b.copy(), b)
  }

  "copy" in {
    val b = buildingMock(
      count = 1.1,
      owner = None,
      strengthening = None
    )
    val player = playerMock()
    val strengthening = strengtheningMock()

    checkBuilding(
      b.copy(
        newCount = 3.14,
        newOwner = Some(player),
        newStrengthening = Some(strengthening)
      ),
      buildingMock(
        count = 3.14,
        owner = Some(player),
        strengthening = Some(strengthening)
      )
    )
  }

  "count < 0" in {
    a[Exception] shouldBe thrownBy {
      buildingMock(count = -0.1)
    }

    buildingMock(count = 0) // ok
  }

  "regenerate" in {
    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        house1 = buildingConfigMock(regeneration = 3.2),
        tower2 = buildingConfigMock(regeneration = 4.2)
      )
    )

    val a = buildingMock(
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      count = 1.1
    )
    val b = buildingMock(
      buildingPrototype = BuildingPrototype(TOWER, LEVEL_2),
      count = 2.1
    )

    checkBuilding(
      a.regenerate(2, config),
      a.copy(newCount = 7.5)
    )
    checkBuilding(
      b.regenerate(2, config),
      b.copy(newCount = 10.5)
    )
  }

  "regeneration & maxCount" in {
    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        house1 = buildingConfigMock(regeneration = 3.2, maxCount = 7.7)
      )
    )

    val a = buildingMock(
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      count = 1.1
    )

    checkBuilding(
      a.regenerate(999, config),
      a.copy(newCount = 7.7)
    )
  }

  "stat" in {
    val b = buildingMock(buildingStat = Stat(1.1, 2.2, 3.3), owner = None)
    b.stat shouldBe Stat(1.1, 2.2, 3.3)
  }

  "stat with owner" in {
    val player = playerMock(stat = Stat(1.2, 1.3, 1.4))
    val b = buildingMock(buildingStat = Stat(1.1, 2.2, 3.3), owner = Some(player))
    checkStat(b.stat, Stat(1.32, 2.86, 4.62))
  }

  "stat with strengthening" in {
    val strengthening = strengtheningMock(stat = Stat(1.2, 1.3, 1.4))
    val b = buildingMock(buildingStat = Stat(1.1, 2.2, 3.3), strengthening = Some(strengthening))
    checkStat(b.stat, Stat(1.32, 2.86, 4.62))
  }

  "stat with owner & strengthening" in {
    val player = playerMock(stat = Stat(1.2, 1.3, 1.4))
    val strengthening = strengtheningMock(stat = Stat(2, 3, 4))
    val b = buildingMock(buildingStat = Stat(1.1, 2.2, 3.3), owner = Some(player), strengthening = Some(strengthening))
    checkStat(b.stat, Stat(1.32 * 2, 2.86 * 3, 4.62 * 4))
  }

  "applyStrengtheningCast" should {
    "выбирает касты относящиеся к этому зданию" +
      "если такие есть:" +
      "изменяет strengthened,strengtheningStartTime,strengtheningDuration,stat больше ничего не изменяет" in {

      val a = Strengthening(buildingId = BuildingId(2), startTime = 123, duration = 321, stat = Stat(1.75, 1.75, 1))
      val b = Strengthening(buildingId = BuildingId(1), startTime = 234, duration = 432, stat = Stat(2.75, 3.75, 1))
      val c = Strengthening(buildingId = BuildingId(2), startTime = 456, duration = 564, stat = Stat(1.45, 3.55, 1))

      val strenghtenings = List(a, b, c)

      val oldBuilding = buildingMock(
        id = BuildingId(2),
        strengthening = None
      )

      val newBuilding = oldBuilding.applyStrengthening(strenghtenings)

      checkBuilding(newBuilding, oldBuilding.copy(newStrengthening = Some(a)))
    }
  }

  "cleanupStrengthening" in {
    val strengthening = strengtheningMock(
      startTime = 2,
      duration = 5,
      stat = Stat(2, 3, 4))
    val b = buildingMock(strengthening = Some(strengthening))
    checkBuilding(b.cleanupStrengthening(time = 4), b)
    checkBuilding(b.cleanupStrengthening(time = 8), b.copy(newStrengthening = None))
  }

  "applyExitUnits" should {
    "выбирает тех кто выходит из этого здания" +
      "уменьшает count и ничего больше" in {

      val exits = List(
        unitMock(
          fromBuilding = buildingMock(id = BuildingId(7), owner = Some(playerMock(PlayerId(0)))),
          toBuilding = buildingMock(id = BuildingId(2)),
          count = 5
        ),
        unitMock(
          fromBuilding = buildingMock(id = BuildingId(1), owner = Some(playerMock(PlayerId(0)))),
          toBuilding = buildingMock(id = BuildingId(2)),
          count = 33
        ),
        unitMock(
          fromBuilding = buildingMock(id = BuildingId(7), owner = Some(playerMock(PlayerId(0)))),
          toBuilding = buildingMock(id = BuildingId(2)),
          count = 2
        )
      )

      val oldBuilding = buildingMock(id = BuildingId(7), count = 10)

      val newBuilding = oldBuilding.applyExitUnits(exits)

      // (10 - 5) - 2 = 3

      checkBuilding(newBuilding, oldBuilding.copy(newCount = 3.0))
    }

    "кидает эксепшн если floorCount = 1" in {

      a[Exception] shouldBe thrownBy {

        buildingMock(id = BuildingId(7), count = 1).applyExitUnits(
          List(unitMock(
            fromBuilding = buildingMock(id = BuildingId(7)),
            toBuilding = buildingMock(BuildingId(2))
          ))
        )

      }

    }
  }

  "applyEnterUnits" should {
    "выбирает тех кто входит в это здание" +
      "если отряд дружественный - изменяет только count" in {

      val enters = List(
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(2)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(7)
          ),
          count = 1
        )
        ,
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(2)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(1)
          ),
          count = 3
        )
        ,
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(2)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(7)
          ),
          count = 4
        )

      )

      val oldBuilding = buildingMock(
        owner = Some(playerMock(PlayerId(2))),
        id = BuildingId(7),
        count = 2
      )

      val newBuilding = oldBuilding.applyEnterUnits(
        enters,
        gameConfigMock()
      )

      // второй не походит по toBuilding

      newBuilding.count shouldBe (7.0 +- 0.01)

      checkBuilding(newBuilding, oldBuilding.copy(newCount = 7.0))

    }

    "выбирает тех кто входит в это здание" +
      "если отряд дружественный - ограчичивает maxCount'ом, изменяет только count" in {

      val enters = List(
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(2)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(7)
          ),
          count = 1
        )
        ,
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(2)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(1)
          ),
          count = 3
        )
        ,
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(2)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(7)
          ),
          count = 99
        )
      )

      val oldBuilding = buildingMock(
        owner = Some(playerMock(PlayerId(2))),
        id = BuildingId(7),
        count = 2,
        buildingPrototype = BuildingPrototype(TOWER, LEVEL_2)
      )

      val newBuilding = oldBuilding.applyEnterUnits(
        enters,
        gameConfigMock(
          buildings = buildingsConfigMock(
            tower2 = buildingConfigMock(
              maxCount = 45
            )
          )
        )
      )

      // второй не походит по toBuilding

      checkBuilding(newBuilding, oldBuilding.copy(newCount = 45.0))

    }

    "выбирает тех кто входит в это здание" +
      "если отряд вражеский - изменяет только count" in {

      val enters = List(
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(1))),
            buildingStat = statMock(attack = 1.1)
          ),
          toBuilding = buildingMock(
            id = BuildingId(7)
          ),
          count = 1
        ),
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(1)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(1)
          ),
          count = 3
        ),
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(1))),
            buildingStat = statMock(attack = 2.2)
          ),
          toBuilding = buildingMock(
            id = BuildingId(7)
          ),
          count = 4
        )
      )

      val oldBuilding = buildingMock(
        owner = Some(playerMock(PlayerId(2))),
        id = BuildingId(7),
        count = 30,
        buildingStat = statMock(defence = 2.4),
        buildingPrototype = BuildingPrototype(TOWER, LEVEL_2)
      )

      val newBuilding = oldBuilding.applyEnterUnits(
        enters,
        gameConfigMock(
          buildings = buildingsConfigMock(
            tower2 = buildingConfigMock(
              fortification = 0.22
            )
          )
        )
      )

      // второй не походит по toBuilding
      // (30 * 2.4 * 0.22 - 1 * 1.1) / (2.4 * 0.22) = 27.91
      // (27.91 * 2.4 * 0.22 - 4 * 2.2) / (2.4 * 0.22) = 11.24

      checkBuilding(newBuilding, oldBuilding.copy(newCount = 11.24))

    }

    "выбирает тех кто входит в это здание" +
      "если отряд вражеский - изменяет только count и owner" in {

      val enters = List(
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(1)))
          ),
          toBuilding = buildingMock(
            id = BuildingId(1)
          ),
          count = 3
        ),
        unitMock(
          fromBuilding = buildingMock(
            owner = Some(playerMock(PlayerId(1))),
            buildingStat = statMock(attack = 2.2)
          ),
          toBuilding = buildingMock(
            id = BuildingId(7)
          ),
          count = 45
        )
      )

      val oldBuilding = buildingMock(
        owner = Some(playerMock(PlayerId(2))),
        id = BuildingId(7),
        count = 30,
        buildingStat = statMock(defence = 2.4),
        buildingPrototype = BuildingPrototype(TOWER, LEVEL_2)
      )

      val newBuilding = oldBuilding.applyEnterUnits(
        enters,
        gameConfigMock(
          buildings = buildingsConfigMock(
            tower2 = buildingConfigMock(
              fortification = 0.22,
              maxCount = 99
            )
          )
        )
      )

      // первый не походит по toBuilding
      // (30 * 2.4 * 0.22 - 45 * 2.2) / 2.2 = 37.8

      checkBuilding(newBuilding, oldBuilding.copy(newCount = 37.8, newOwner = Some(playerMock(PlayerId(1)))))
    }

  }

  "applyDamagers" should {
    "выбирает дамагеров, в радиус которых попадает юнит" +
      "уменьшает count на их дамаг" +
      "не меняет ничего больше" in {

      val a = TDamager(Point(2.1, 2.2), damagerConfigMock(radius = 0.4, powerVsBuilding = 1.2))
      val b = TDamager(Point(10.1, 20.2), damagerConfigMock(radius = 1.1, powerVsBuilding = 3.3))
      val c = TDamager(Point(3.1, 1.1), damagerConfigMock(radius = 1.5, powerVsBuilding = 2.2))

      val damagers = List(a, b, c)

      val oldBuilding = buildingMock(
        count = 14,
        pos = Point(2, 2),
        buildingStat = statMock(defence = 1.3)
      )

      val newBuilding = oldBuilding.applyDamagers(damagers, time = 0)

      // По радиусу подходят только A и C
      // (14 - 1.2 / 1.3) - 2.2 / 1.3 = 11.38

      checkBuilding(newBuilding, oldBuilding.copy(newCount = 11.38))
    }
  }

  "differentWith" should {
    "true только если различаются: округленный count или owner или strengthened" in {

      buildingMock(count = 1.2) differentWith buildingMock(count = 3.12) shouldBe true
      buildingMock(count = 1.2) differentWith buildingMock(count = 1.4) shouldBe false
      buildingMock(strengthening = Some(Strengthening(BuildingId(1), 0, 10, Stat(1, 1, 1)))) differentWith buildingMock(strengthening = None) shouldBe true
      buildingMock(owner = None) differentWith buildingMock(owner = Some(playerMock())) shouldBe true

      buildingMock(lastShootTime = 0) differentWith buildingMock(lastShootTime = 10) shouldBe false

    }
  }


  "getUpdateMessages" should {
    "Возвращает апдейт мессадж" +
      "если здание было и в старом и в новом списке" +
      "и они различаются по differentWith" in {

      val oldBuildings = List(
        buildingMock(id = BuildingId(1), count = 11.2),
        buildingMock(id = BuildingId(2), count = 12.2),
        buildingMock(id = BuildingId(3), count = 25, owner = Some(playerMock(PlayerId(2))))
      )

      val newBuildings = List(
        buildingMock(id = BuildingId(1), count = 11.2),
        buildingMock(id = BuildingId(4), count = 7),
        buildingMock(id = BuildingId(2), count = 9),
        buildingMock(id = BuildingId(3), count = 25, owner = Some(playerMock(PlayerId(1))))
      )

      val updateMessages = Building.getUpdateMessages(oldBuildings, newBuildings).toList
      updateMessages should have size 2
      updateMessages(0).building.id shouldBe BuildingId(2)
      updateMessages(0).building.population shouldBe 9

      updateMessages(1).building.id shouldBe BuildingId(3)
      updateMessages(1).building.owner should be(Some(PlayerId(1)))

    }
  }

  "applyShots" should {
    "если есть хотя бы один выстрел относящиеся к этому зданию" +
      "изменяет lastShootTime и ничего больше" in {

      val bullets = List(
        bulletMock(building = buildingMock(id = BuildingId(3))),
        bulletMock(building = buildingMock(id = BuildingId(2))),
        bulletMock(building = buildingMock(id = BuildingId(3)))
      )

      val oldBuilding = buildingMock(
        id = BuildingId(3),
        lastShootTime = 0
      )

      val newBuilding = oldBuilding.applyShots(bullets = bullets, time = 77)

      checkBuilding(newBuilding, oldBuilding.copy(newLastShootTime = 77))
    }

    "если ни одного выстрела относящегося к этому зданию" +
      "ничего не изменяет" in {

      val bullets = List(
        bulletMock(building = buildingMock(id = BuildingId(1))),
        bulletMock(building = buildingMock(id = BuildingId(2))),
        bulletMock(building = buildingMock(id = BuildingId(1)))
      )

      val oldBuilding = buildingMock(
        id = BuildingId(3),
        lastShootTime = 2
      )

      val newBuilding = oldBuilding.applyShots(bullets = bullets, time = 77)

      checkBuilding(oldBuilding, newBuilding)
    }
  }

  "canShoot" should {
    "Возвращает башни у которых прошло достаточное время после последнего выстрела" in {

      val a = buildingMock(id = BuildingId(1), lastShootTime = 0, buildingPrototype = BuildingPrototype(TOWER, LEVEL_1))
      val b = buildingMock(id = BuildingId(2), lastShootTime = 10, buildingPrototype = BuildingPrototype(TOWER, LEVEL_1))
      val c = buildingMock(id = BuildingId(3), lastShootTime = 8, buildingPrototype = BuildingPrototype(TOWER, LEVEL_1))

      val buildings = List(a, b, c)

      val canShootBuildings = Building.canShoot(
        buildings,
        time = 11,
        gameConfigMock(
          shooting = shootingConfigMock(shootInterval = 2)
        )
      )

      canShootBuildings should have size 2
      canShootBuildings should contain(a)
      canShootBuildings should contain(c)
    }
  }
}
