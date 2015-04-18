//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.kit

import org.scalatest.Matchers
import ru.rknrl.castles.account.AccountState.{Items, Slots}
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.state._
import ru.rknrl.core.{Stat, Damaged, Damager}
import ru.rknrl.core.points.{Points, Point}
import ru.rknrl.core.social.{ProductInfo, SocialConfig, SocialConfigs}
import ru.rknrl.dto.BuildingLevel.LEVEL_1
import ru.rknrl.dto.BuildingType.HOUSE
import ru.rknrl.dto._

object Mocks extends Matchers {
  def checkPoint(a: Point, b: Point) = {
    a.x shouldBe (b.x +- 0.001)
    a.y shouldBe (b.y +- 0.001)
  }

  def checkStat(a: Stat, b: Stat) = {
    a.attack shouldBe (b.attack +- 0.001)
    a.defence shouldBe (b.defence +- 0.001)
    a.speed shouldBe (b.speed +- 0.001)
  }

  def checkBuilding(a: Building, b: Building) = {
    a.id shouldBe b.id
    a.buildingPrototype shouldBe b.buildingPrototype
    a.count shouldBe (b.count +- 0.01)
    a.pos shouldBe b.pos
    a.buildingStat shouldBe b.buildingStat
    a.owner shouldBe b.owner
    a.strengthening shouldBe b.strengthening
    a.lastShootTime shouldBe b.lastShootTime
  }

  def checkBuildings(a: Seq[Building], b: Seq[Building]) = {
    a.size shouldBe b.size
    for (i ← 0 until a.size) checkBuilding(a(i), b(i))
  }

  def checkUnit(a: GameUnit, b: GameUnit) = {
    a.id shouldBe b.id
    a.startTime shouldBe b.startTime
    a.duration shouldBe b.duration
    checkBuilding(a.fromBuilding, b.fromBuilding)
    checkBuilding(a.toBuilding, b.toBuilding)
    a.count shouldBe (b.count +- 0.01)
  }

  def checkUnits(a: Seq[GameUnit], b: Seq[GameUnit]) = {
    a.size shouldBe b.size
    for (i ← 0 until a.size) checkUnit(a(i), b(i))
  }

  def checkGameState(a: GameState, b: GameState) = {
    a.time shouldBe b.time
    checkBuildings(a.buildings, b.buildings)
    checkUnits(a.units, b.units)
  }

  def statMock(attack: Double = 1.0,
               defence: Double = 1.0,
               speed: Double = 1.0) =
    Stat(
      attack = attack,
      defence = defence,
      speed = speed
    )

  def gameItemStateMock(itemType: ItemType,
                        count: Int = 1,
                        lastUseTime: Long = 0,
                        useCount: Int = 0) =
    new ItemState(
      itemType = itemType,
      count = count,
      lastUseTime = lastUseTime,
      useCount = useCount
    )

  def socialConfigMock(appId: String = "",
                       appSecret: String = "",
                       productsInfo: List[ProductInfo] = List.empty) =
    Some(new SocialConfig(
      appId = appId,
      appSecret = appSecret,
      productsInfo = productsInfo
    ))

  def socialConfigsMock(vk: Option[SocialConfig] = socialConfigMock(),
                        ok: Option[SocialConfig] = socialConfigMock(),
                        mm: Option[SocialConfig] = socialConfigMock(),
                        fb: Option[SocialConfig] = socialConfigMock()) =
    new SocialConfigs(
      vk = vk,
      ok = ok,
      mm = mm,
      fb = fb
    )


  def gameStateMock(width: Int = 10,
                    height: Int = 10,
                    slotsPos: Iterable[SlotsPosDTO] = List.empty,
                    time: Long = 1,
                    players: Map[PlayerId, Player] = Map.empty,
                    buildings: Seq[Building] = List.empty,
                    units: Seq[GameUnit] = List.empty,
                    fireballs: Iterable[Fireball] = List.empty,
                    volcanoes: Iterable[Volcano] = List.empty,
                    tornadoes: Iterable[Tornado] = List.empty,
                    bullets: Iterable[Bullet] = List.empty,
                    items: GameItems = new GameItems(Map.empty),
                    config: GameConfig = gameConfigMock(),
                    unitIdIterator: UnitIdIterator = new UnitIdIterator,
                    assistancePositions: Map[PlayerId, Point] = Map.empty) =
    new GameState(
      width = width,
      height = height,
      slotsPos = slotsPos,
      time = time,
      players = players,
      buildings = buildings,
      units = units,
      fireballs = fireballs,
      volcanoes = volcanoes,
      tornadoes = tornadoes,
      bullets = bullets,
      items = items,
      config = config,
      unitIdIterator = unitIdIterator,
      assistancePositions = assistancePositions
    )

  def buildingMock(id: BuildingId = BuildingId(1),
                   buildingPrototype: BuildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
                   count: Double = 1.1,
                   pos: Point = Point(0, 0),
                   buildingStat: Stat = Stat(1, 1, 1),
                   owner: Option[Player] = None,
                   strengthening: Option[Strengthening] = None,
                   lastShootTime: Long = 0) =
    new Building(
      id = id,
      buildingPrototype = buildingPrototype,
      count = count,
      pos = pos,
      buildingStat = buildingStat,
      owner = owner,
      strengthening = strengthening,
      lastShootTime = lastShootTime
    )

  def playerMock(id: PlayerId = PlayerId(1),
                 accountId: AccountId = AccountId(AccountType.DEV, "0"),
                 userInfo: UserInfoDTO = UserInfoDTO(AccountId(AccountType.DEV, "0")),
                 slots: Slots = Map.empty,
                 stat: Stat = Stat(1, 1, 1),
                 items: Items = Map.empty,
                 isBot: Boolean = false) =
    new Player(
      id = id,
      accountId = accountId,
      userInfo = userInfo,
      slots = slots,
      stat = stat,
      items = items,
      isBot = isBot
    )

  def strengtheningMock(buildingId: BuildingId = BuildingId(0),
                        startTime: Long = 0,
                        duration: Long = 1000,
                        stat: Stat = Stat(1, 1, 1)) =
    new Strengthening(
      buildingId = buildingId,
      startTime = startTime,
      duration = duration,
      stat = stat
    )

  def unitMock(id: UnitId = UnitId(0),
               startTime: Long = 0,
               duration: Long = 1000,
               fromBuilding: Building = buildingMock(pos = Point(0, 0), owner = Some(playerMock(PlayerId(0)))),
               toBuilding: Building = buildingMock(pos = Point(1, 1)),
               count: Double = 10.1) =
    new GameUnit(
      id = id,
      startTime = startTime,
      duration = duration,
      fromBuilding = fromBuilding,
      toBuilding = toBuilding,
      count = count
    )

  def bulletMock(building: Building = buildingMock(),
                 unit: GameUnit = unitMock(),
                 startTime: Long = 0,
                 duration: Long = 10,
                 powerVsUnit: Double = 9) =
    new Bullet(
      building = building,
      unit = unit,
      startTime = startTime,
      duration = duration,
      powerVsUnit = powerVsUnit
    )

  def buildingConfigMock(regeneration: Double = 1.0,
                         startCount: Int = 10,
                         maxCount: Int = 99,
                         fortification: Double = 1.2,
                         shotPower: Option[Double] = None) =
    new BuildingConfig(
      regeneration = regeneration,
      startCount = startCount,
      maxCount = maxCount,
      fortification = fortification,
      shotPower = shotPower
    )

  def buildingsConfigMock(house1: BuildingConfig = buildingConfigMock(),
                          house2: BuildingConfig = buildingConfigMock(),
                          house3: BuildingConfig = buildingConfigMock(),
                          tower1: BuildingConfig = buildingConfigMock(),
                          tower2: BuildingConfig = buildingConfigMock(),
                          tower3: BuildingConfig = buildingConfigMock(),
                          church1: BuildingConfig = buildingConfigMock(),
                          church2: BuildingConfig = buildingConfigMock(),
                          church3: BuildingConfig = buildingConfigMock()) =
    new BuildingsConfig(
      house1 = house1,
      house2 = house2,
      house3 = house3,
      tower1 = tower1,
      tower2 = tower2,
      tower3 = tower3,
      church1 = church1,
      church2 = church2,
      church3 = church3
    )

  def constantsConfigMock(itemCooldown: Long = 1) =
    new Constants(
      itemCooldown = itemCooldown
    )

  def fireballConfigMock(damage: DamagerConfig = damagerConfigMock(),
                         flyDuration: Long = 1000) =
    new FireballConfig(
      damage = damage,
      flyDuration = flyDuration
    )

  def volcanoConfigMock(damage: DamagerConfig = damagerConfigMock(),
                        duration: Long = 6000) =
    new VolcanoConfig(
      damage = damage,
      duration = duration
    )

  def tornadoConfigMock(damage: DamagerConfig = damagerConfigMock(),
                        duration: Long = 10000,
                        speed: Double = 0.033) =
    new TornadoConfig(
      damage = damage,
      duration = duration,
      speed = speed
    )

  def strengtheningConfigMock(factor: Double = 1.5,
                              maxBonusFactor: Double = 0.5,
                              duration: Long = 10000,
                              maxBonusDuration: Long = 4000) =
    new StrengtheningConfig(
      factor = factor,
      maxBonusFactor = maxBonusFactor,
      duration = duration,
      maxBonusDuration = maxBonusDuration
    )

  def shootingConfigMock(speed: Double = 0.4,
                         shootInterval: Long = 1000,
                         shootRadius: Double = 50) =
    new ShootingConfig(
      speed = speed,
      shootInterval = shootInterval,
      shootRadius = shootRadius
    )

  def assistanceConfigMock(power: Double = 0.5,
                           maxBonusPower: Double = 0.5) =
    new AssistanceConfig(
      power = power,
      maxBonusPower = maxBonusPower
    )

  def unitsConfigMock(house: Stat = new Stat(2, 2, 0.05),
                      tower: Stat = new Stat(4.9, 4, 0.04),
                      church: Stat = new Stat(3, 3, 0.07)) =
    new UnitsConfig(
      house = house,
      tower = tower,
      church = church
    )

  def fireballMock(playerId: PlayerId = PlayerId(0),
                   pos: Point = Point(0, 0),
                   startTime: Long = 0,
                   duration: Long = 10,
                   damagerConfig: DamagerConfig = damagerConfigMock()) =
    new Fireball(
      playerId = playerId,
      pos = pos,
      startTime = startTime,
      duration = duration,
      damagerConfig = damagerConfig
    )

  def volcanoMock(playerId: PlayerId = PlayerId(0),
                  pos: Point = Point(0, 0),
                  startTime: Long = 0,
                  duration: Long = 10,
                  damagerConfig: DamagerConfig = damagerConfigMock()) =
    new Volcano(
      playerId = playerId,
      pos = pos,
      startTime = startTime,
      duration = duration,
      damagerConfig = damagerConfig
    )

  def tornadoMock(playerId: PlayerId = PlayerId(0),
                  points: Points = Points(Point(0, 0), Point(1, 1)),
                  startTime: Long = 0,
                  duration: Long = 10,
                  damagerConfig: DamagerConfig = damagerConfigMock()) =
    new Tornado(
      playerId = playerId,
      points = points,
      startTime = startTime,
      duration = duration,
      damagerConfig = damagerConfig
    )

  def gameConfigMock(constants: Constants = constantsConfigMock(),
                     buildings: BuildingsConfig = buildingsConfigMock(),
                     units: UnitsConfig = unitsConfigMock(),
                     fireball: FireballConfig = fireballConfigMock(),
                     volcano: VolcanoConfig = volcanoConfigMock(),
                     tornado: TornadoConfig = tornadoConfigMock(),
                     strengthening: StrengtheningConfig = strengtheningConfigMock(),
                     shooting: ShootingConfig = shootingConfigMock(),
                     assistance: AssistanceConfig = assistanceConfigMock()) =
    new GameConfig(
      constants = constants,
      buildings = buildings,
      units = units,
      fireball = fireball,
      volcano = volcano,
      tornado = tornado,
      strengthening = strengthening,
      shooting = shooting,
      assistance = assistance
    )

  case class TDamager(pos: Point,
                      damagerConfig: DamagerConfig) extends Damager {
    def pos(time: Long) = pos
  }

  case class TDamaged(count: Double,
                      pos: Point,
                      stat: Stat) extends Damaged[TDamaged] {
    def pos(time: Long) = pos

    def setCount(newCount: Double) = new TDamaged(newCount, pos, stat)
  }

  def damagerConfigMock(powerVsUnit: Double = 40,
                        powerVsBuilding: Double = 20,
                        radius: Double = 39,
                        maxPowerBonus: Double = 0.8) =
    new DamagerConfig(
      powerVsUnit = powerVsUnit,
      powerVsBuilding = powerVsBuilding,
      radius = radius,
      maxPowerBonus = maxPowerBonus
    )

  def damagerMock(pos: Point = Point(0, 0),
                  damagerConfig: DamagerConfig = damagerConfigMock()) =
    TDamager(pos, damagerConfig)

  def damagedMock(count: Double = 10,
                  pos: Point = Point(0, 0),
                  stat: Stat = statMock()) =
    TDamaged(count, pos, stat)

}
