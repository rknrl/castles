//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.kit

import org.scalatest.Matchers
import protos.BuildingLevel.LEVEL_1
import protos.BuildingType.{CHURCH, HOUSE, TOWER}
import protos.SkillLevel.SKILL_LEVEL_0
import protos.SlotId._
import protos._
import ru.rknrl.castles.Config
import ru.rknrl.castles.account.{AccountConfig, BuildingPrices, SkillUpgradePrices}
import ru.rknrl.castles.storage.StorageConfig
import ru.rknrl.castles.game._
import ru.rknrl.castles.game.init.{GameMap, GameMaps}
import ru.rknrl.castles.game.state.{Bullet, Fireball, GameState, Player, Tornado, Volcano, _}
import ru.rknrl.castles.matchmaking.GameCreator
import ru.rknrl.castles.matchmaking.MatchMaking.GameOrder
import ru.rknrl.core.Graphite.GraphiteConfig
import ru.rknrl.core.points.{Point, Points}
import ru.rknrl.core.social.{ProductInfo, SocialConfig, SocialConfigs}
import ru.rknrl.core.{Damaged, Damager, Stat}

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


  def slotsMock =
    List(
      Slot(SLOT_1, None),
      Slot(SLOT_2, None),
      Slot(SLOT_3, Some(BuildingPrototype(HOUSE, LEVEL_1))),
      Slot(SLOT_4, Some(BuildingPrototype(TOWER, LEVEL_1))),
      Slot(SLOT_5, Some(BuildingPrototype(CHURCH, LEVEL_1)))
    )

  def skillsMock =
    SkillType.values.map(Skill(_, SKILL_LEVEL_0))

  def itemsMock =
    ItemType.values.map(Item(_, 4))

  def accountStateMock(slots: Seq[Slot] = slotsMock,
                       skills: Seq[Skill] = skillsMock,
                       items: Seq[Item] = itemsMock,
                       gold: Int = 10,
                       gamesCount: Int = 1,
                       weekNumberAccepted: Option[Int] = None,
                       lastPresentTime: Option[Long] = None,
                       lastGamesCountAdvert: Option[Int] = None) =
    protos.AccountState(
      slots = slots,
      skills = skills,
      items = items,
      gold = gold,
      gamesCount = gamesCount,
      weekNumberAccepted = weekNumberAccepted,
      lastPresentTime = lastPresentTime,
      lastGamesCountAdvert = lastGamesCountAdvert
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

  def dbConfigMock(username: String = "root",
                   host: String = "localhost",
                   port: Int = 6767,
                   password: String = "123",
                   database: String = "catles",
                   poolMaxObjects: Int = 10,
                   poolMaxIdle: Long = 11,
                   poolMaxQueueSize: Int = 12) =
    new StorageConfig(
      username = username,
      host = host,
      port = port,
      password = password,
      database = database,
      poolMaxObjects = poolMaxObjects,
      poolMaxIdle = poolMaxIdle,
      poolMaxQueueSize = poolMaxQueueSize
    )

  def buildingPricesMock(level1: Int = 4,
                         level2: Int = 16,
                         level3: Int = 64): BuildingPrices =
    new BuildingPrices(Map(
      BuildingLevel.LEVEL_1 → level1,
      BuildingLevel.LEVEL_2 → level2,
      BuildingLevel.LEVEL_3 → level3
    ))

  def skillUpgradePricesMock(): SkillUpgradePrices =
    new SkillUpgradePrices((for (i ← 1 to 9) yield i → i).toMap)

  def accountConfigMock(buildingPrices: BuildingPrices = buildingPricesMock(),
                        skillUpgradePrices: SkillUpgradePrices = skillUpgradePricesMock(),
                        itemPrice: Int = 1,
                        initGold: Int = 1000,
                        presentGold: Int = 10,
                        presentInterval: Long = 60000,
                        advertGold: Int = 20,
                        advertGamesInterval: Int = 3,
                        initRating: Double = 1400,
                        initItemCount: Int = 4,
                        maxAttack: Double = 3,
                        maxDefence: Double = 3,
                        maxSpeed: Double = 1.2) =
    new AccountConfig(
      buildingPrices = buildingPrices,
      skillUpgradePrices = skillUpgradePrices,
      itemPrice = itemPrice,
      initGold = initGold,
      presentGold = presentGold,
      presentInterval = presentInterval,
      advertGold = advertGold,
      advertGamesInterval = advertGamesInterval,
      initRating = initRating,
      initItemCount = initItemCount,
      maxAttack = maxAttack,
      maxDefence = maxDefence,
      maxSpeed = maxSpeed
    )

  def graphiteConfigMock() = GraphiteConfig(host = "host", port = 2003, aggregatorPort = 2023)

  def configMock(host: String = "localhost",
                 staticHost: String = "localhot",
                 gamePort: Int = 2305,
                 policyPort: Int = 2306,
                 adminPort: Int = 2307,
                 httpPort: Int = 80,
                 adminLogin: String = "adminLogin",
                 adminPassword: String = "adminPass",
                 isDev: Boolean = true,
                 mapsDir: String = "maps/dir/",
                 clientBugsDir: String = "bugs/",
                 db: StorageConfig = dbConfigMock(),
                 graphite: GraphiteConfig = graphiteConfigMock(),
                 products: List[ru.rknrl.core.social.Product] = List.empty,
                 social: SocialConfigs = socialConfigsMock(),
                 account: AccountConfig = accountConfigMock(),
                 game: GameConfig = gameConfigMock()) =
    new Config(
      host = host,
      staticHost = host,
      gamePort = gamePort,
      policyPort = policyPort,
      adminPort = adminPort,
      httpPort = httpPort,
      adminLogin = adminLogin,
      adminPassword = adminPassword,
      isDev = isDev,
      mapsDir = mapsDir,
      clientBugsDir = clientBugsDir,
      db = db,
      graphite = graphite,
      products = products,
      social = social,
      account = account,
      game = game
    )

  def gameStateMock(width: Int = 10,
                    height: Int = 10,
                    slotsPos: Iterable[SlotsPos] = List.empty,
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
                 userInfo: UserInfo = UserInfo(AccountId(AccountType.DEV, "0")),
                 slots: Seq[Slot] = List.empty,
                 stat: Stat = Stat(1, 1, 1),
                 items: Seq[Item] = List.empty,
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
                          tower1: BuildingConfig = buildingConfigMock(shotPower = Some(1.0)),
                          tower2: BuildingConfig = buildingConfigMock(shotPower = Some(1.0)),
                          tower3: BuildingConfig = buildingConfigMock(shotPower = Some(1.0)),
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

  def newGameOrder(accountId: AccountId,
                   deviceType: DeviceType = DeviceType.PC,
                   accountState: AccountState = accountStateMock(),
                   rating: Double = 1400,
                   isBot: Boolean = false) =
    GameOrder(
      accountId = accountId,
      deviceType = deviceType,
      userInfo = UserInfo(accountId),
      accountState = accountState,
      rating = rating,
      isBot = isBot
    )

  val notTutorMap2 =
    new GameMap(List.empty)

  val notTutorMap4 =
    new GameMap(List.empty)

  val tutorMap2 =
    new GameMap(List.empty)

  val tutorMap4 =
    new GameMap(List.empty)

  def gameMapsMock() =
    new GameMaps(
      Array(notTutorMap4),
      Array(notTutorMap2),
      tutorMap4,
      tutorMap2
    )

  def gameCreatorMock() = new GameCreator(gameMapsMock(), configMock())

}
