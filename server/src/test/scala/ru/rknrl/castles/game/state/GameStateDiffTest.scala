//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.state.Bullets._
import ru.rknrl.castles.game.state.Fireballs.castToFireball
import ru.rknrl.castles.game.state.GameStateDiff._
import ru.rknrl.castles.game.state.Moving.moveActionsToExitUnits
import ru.rknrl.castles.game.state.Tornadoes.castToTornado
import ru.rknrl.castles.game.state.Volcanoes.castToVolcano
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.points.{Point, Points}
import ru.rknrl.dto.BuildingLevel._
import ru.rknrl.dto.BuildingType._
import ru.rknrl.dto.ItemType._
import ru.rknrl.dto._

class GameStateDiffTest extends WordSpec with Matchers {

  def updateGameState(gameState: GameState,
                      newTime: Long = 1,
                      moveActions: Map[PlayerId, MoveDTO] = Map.empty,
                      fireballCasts: Map[PlayerId, PointDTO] = Map.empty,
                      volcanoCasts: Map[PlayerId, PointDTO] = Map.empty,
                      tornadoCasts: Map[PlayerId, CastTornadoDTO] = Map.empty,
                      strengtheningCasts: Map[PlayerId, BuildingId] = Map.empty,
                      assistanceCasts: Map[PlayerId, BuildingId] = Map.empty) =
    gameState.update(
      newTime = newTime,
      moveActions = moveActions,
      fireballCasts = fireballCasts,
      volcanoCasts = volcanoCasts,
      tornadoCasts = tornadoCasts,
      strengtheningCasts = strengtheningCasts,
      assistanceCasts = assistanceCasts
    )

  def checkEmpty(gameStateUpdate: GameStateUpdateDTO) = {
    gameStateUpdate.buildingUpdates shouldBe empty
    gameStateUpdate.newUnits shouldBe empty
    gameStateUpdate.unitUpdates shouldBe empty
    gameStateUpdate.killUnits shouldBe empty
    gameStateUpdate.newFireballs shouldBe empty
    gameStateUpdate.newVolcanoes shouldBe empty
    gameStateUpdate.newBullets shouldBe empty
    gameStateUpdate.itemStatesUpdates shouldBe empty
  }

  "no changes" in {
    val gameState = gameStateMock(time = 1)
    val gameStateUpdate = diff(gameState, updateGameState(gameState, newTime = 1))
    checkEmpty(gameStateUpdate)
  }

  "time" in {
    val gameState = gameStateMock(time = 1)
    val gameStateUpdate = diff(gameState, updateGameState(gameState, newTime = 2))
    checkEmpty(gameStateUpdate)
  }

  "buildings regeneration" in {
    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        house1 = buildingConfigMock(regeneration = 3.2),
        tower2 = buildingConfigMock(regeneration = 4.2)
      )
    )

    val a = buildingMock(
      BuildingId(0),
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      count = 1.1
    )
    val b = buildingMock(
      BuildingId(1),
      buildingPrototype = BuildingPrototype(TOWER, LEVEL_2),
      count = 2.1
    )

    val buildings = List(a, b)
    val gameState = gameStateMock(time = 1, buildings = buildings, config = config)
    val gameStateUpdate = diff(gameState, updateGameState(gameState, newTime = 3))

    val newA = a.regenerate(2, config)
    val newB = b.regenerate(2, config)

    gameStateUpdate.buildingUpdates shouldBe List(
      newA.updateDto,
      newB.updateDto
    )
  }

  "exitUnits" in {
    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        house1 = buildingConfigMock(regeneration = 0),
        tower2 = buildingConfigMock(regeneration = 0),
        church3 = buildingConfigMock(regeneration = 0)
      )
    )

    val player1 = playerMock(id = PlayerId(0))
    val player2 = playerMock(id = PlayerId(1))

    val a = buildingMock(
      BuildingId(0),
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      count = 10.2,
      owner = Some(player1),
      pos = Point(0, 0)
    )

    val b = buildingMock(
      BuildingId(1),
      buildingPrototype = BuildingPrototype(TOWER, LEVEL_2),
      count = 2.1,
      owner = None,
      pos = Point(2, 3)
    )

    val c = buildingMock(
      BuildingId(2),
      buildingPrototype = BuildingPrototype(CHURCH, LEVEL_3),
      count = 20.1,
      owner = Some(player2),
      pos = Point(5, 7)
    )

    val buildings = List(a, b, c)

    val gameState = gameStateMock(
      time = 1,
      buildings = buildings,
      config = config
    )

    val moveActions = Map(
      PlayerId(0) → MoveDTO(List(BuildingId(0)), BuildingId(1)),
      PlayerId(1) → MoveDTO(List(BuildingId(2)), BuildingId(1))
    )

    val newGameState = updateGameState(
      gameState,
      newTime = 3,
      moveActions = moveActions
    )

    val gameStateUpdate = diff(gameState, newGameState)

    val exitUnits = moveActionsToExitUnits(moveActions, buildings, new UnitIdIterator, time = 3).toSeq

    gameStateUpdate.newUnits shouldBe List(
      exitUnits(0).dto(newGameState.time),
      exitUnits(1).dto(newGameState.time)
    )
  }

  "assistance" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val casts = Map(
      PlayerId(0) → BuildingId(0),
      PlayerId(1) → BuildingId(1),

      PlayerId(2) → BuildingId(1) // not valid
    )

    val assistancePositions = Map(
      PlayerId(0) → Point(1, 2),
      PlayerId(1) → Point(50, 10),
      PlayerId(2) → Point(70, 70)
    )

    val buildings = List(
      buildingMock(
        id = BuildingId(0),
        owner = Some(player0)
      ),
      buildingMock(
        id = BuildingId(1),
        buildingPrototype = BuildingPrototype(CHURCH, LEVEL_1),
        count = 35,
        owner = Some(player1)
      )
    )

    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        church1 = buildingConfigMock(maxCount = 35)
      )
    )

    val items = new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        ASSISTANCE → gameItemStateMock(ASSISTANCE, count = 1)
      )),
      PlayerId(1) → new ItemStates(Map(
        ASSISTANCE → gameItemStateMock(ASSISTANCE, count = 2)
      )),
      PlayerId(2) → new ItemStates(Map(
        ASSISTANCE → gameItemStateMock(ASSISTANCE, count = 2)
      ))
    ))

    val gameState = gameStateMock(
      time = 0,
      players = players,
      buildings = buildings,
      items = items,
      assistancePositions = assistancePositions,
      config = config
    )

    val newGameState = updateGameState(
      gameState,
      newTime = 1,
      assistanceCasts = casts
    )

    val gameStateUpdate = diff(gameState, newGameState)

    gameStateUpdate.itemStatesUpdates shouldBe getItemStatesUpdates(gameState.items, newGameState.items, config, newGameState.time)
  }

  "cast & cleanup strengthening" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        house1 = buildingConfigMock(regeneration = 0)
      )
    )

    val b0 = buildingMock(
      id = BuildingId(0),
      strengthening = None,
      owner = Some(player0),
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1)
    )
    val b1 = buildingMock(
      id = BuildingId(1),
      strengthening = None,
      owner = None,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1)
    )
    val b2 = buildingMock(
      id = BuildingId(2),
      strengthening = None,
      owner = Some(player1),
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1)
    )
    val buildings = List(b0, b1, b2)

    val items = new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        STRENGTHENING → gameItemStateMock(STRENGTHENING, count = 1)
      )),
      PlayerId(1) → new ItemStates(Map(
        STRENGTHENING → gameItemStateMock(STRENGTHENING, count = 2)
      )),
      PlayerId(2) → new ItemStates(Map(
        STRENGTHENING → gameItemStateMock(STRENGTHENING, count = 2)
      ))
    ))

    val gameState = gameStateMock(
      time = 1,
      players = players,
      buildings = buildings,
      items = items,
      config = config
    )

    val cast1 = PlayerId(0) → BuildingId(0)
    val cast2 = PlayerId(1) → BuildingId(2)
    val cast3 = PlayerId(2) → BuildingId(1) // not valid

    val casts = Map(cast1, cast2, cast3)

    val newGameState = updateGameState(
      gameState,
      newTime = 2,
      strengtheningCasts = casts
    )

    val gameStateUpdate = diff(gameState, newGameState)

    gameStateUpdate.itemStatesUpdates shouldBe getItemStatesUpdates(gameState.items, newGameState.items, config, newGameState.time)
  }

  "fireballs cast & cleanup" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val churchesProportion = new ChurchesProportion(Map(
      PlayerId(0) → 0,
      PlayerId(1) → 0,
      PlayerId(2) → 0
    ))

    val items = new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        FIREBALL → gameItemStateMock(FIREBALL, count = 1)
      )),
      PlayerId(1) → new ItemStates(Map(
        FIREBALL → gameItemStateMock(FIREBALL, count = 2)
      )),
      PlayerId(2) → new ItemStates(Map(
        FIREBALL → gameItemStateMock(FIREBALL, count = 2)
      ))
    ))

    val cast1 = PlayerId(0) → PointDTO(0, 0)
    val cast2 = PlayerId(1) → PointDTO(2, 2)
    val casts = Map(cast1, cast2)

    val gameState = gameStateMock(
      time = 1,
      players = players,
      items = items
    )

    val newGameState = updateGameState(gameState, newTime = 2, fireballCasts = casts)
    val gameStateUpdate = diff(gameState, newGameState)

    gameStateUpdate.itemStatesUpdates shouldBe getItemStatesUpdates(gameState.items, newGameState.items, gameState.config, newGameState.time)

    val fireball1 = castToFireball(cast1, newGameState.time, churchesProportion, gameState.config)
    val fireball2 = castToFireball(cast2, newGameState.time, churchesProportion, gameState.config)

    gameStateUpdate.newFireballs shouldBe List(
      fireball1.dto(newGameState.time),
      fireball2.dto(newGameState.time)
    )
  }

  "volcano cast & cleanup" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val churchesProportion = new ChurchesProportion(Map(
      PlayerId(0) → 0,
      PlayerId(1) → 0,
      PlayerId(2) → 0
    ))

    val items = new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        VOLCANO → gameItemStateMock(VOLCANO, count = 1)
      )),
      PlayerId(1) → new ItemStates(Map(
        VOLCANO → gameItemStateMock(VOLCANO, count = 2)
      )),
      PlayerId(2) → new ItemStates(Map(
        VOLCANO → gameItemStateMock(VOLCANO, count = 2)
      ))
    ))

    val cast1 = PlayerId(0) → PointDTO(0, 0)
    val cast2 = PlayerId(1) → PointDTO(2, 2)
    val casts = Map(cast1, cast2)

    val gameState = gameStateMock(
      time = 1,
      players = players,
      items = items
    )

    val newGameState = updateGameState(gameState, newTime = 2, volcanoCasts = casts)
    val gameStateUpdate = diff(gameState, newGameState)

    val volcano1 = castToVolcano(cast1, newGameState.time, churchesProportion, gameState.config)
    val volcano2 = castToVolcano(cast2, newGameState.time, churchesProportion, gameState.config)

    gameStateUpdate.itemStatesUpdates shouldBe getItemStatesUpdates(gameState.items, newGameState.items, gameState.config, newGameState.time)

    gameStateUpdate.newVolcanoes shouldBe List(
      volcano1.dto(newGameState.time),
      volcano2.dto(newGameState.time)
    )
  }

  "tornado cast & cleanup" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val churchesProportion = new ChurchesProportion(Map(
      PlayerId(0) → 0,
      PlayerId(1) → 0,
      PlayerId(2) → 0
    ))

    val items = new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        TORNADO → gameItemStateMock(TORNADO, count = 1)
      )),
      PlayerId(1) → new ItemStates(Map(
        TORNADO → gameItemStateMock(TORNADO, count = 2)
      )),
      PlayerId(2) → new ItemStates(Map(
        TORNADO → gameItemStateMock(TORNADO, count = 2)
      ))
    ))


    val cast1 = PlayerId(0) → CastTornadoDTO(Seq(PointDTO(0, 0), PointDTO(1, 1)))
    val cast2 = PlayerId(1) → CastTornadoDTO(Seq(PointDTO(2, 2), PointDTO(4, 4)))
    val casts = Map(cast1, cast2)

    val gameState = gameStateMock(
      time = 1,
      players = players,
      items = items
    )

    val newGameState = updateGameState(gameState, newTime = 2, tornadoCasts = casts)
    val gameStateUpdate = diff(gameState, newGameState)

    gameStateUpdate.itemStatesUpdates shouldBe getItemStatesUpdates(gameState.items, newGameState.items, gameState.config, newGameState.time)

    val tornado1 = castToTornado(cast1, newGameState.time, churchesProportion, gameState.config)
    val tornado2 = castToTornado(cast2, newGameState.time, churchesProportion, gameState.config)

    gameStateUpdate.newTornadoes shouldBe List(
      tornado1.dto(newGameState.time),
      tornado2.dto(newGameState.time)
    )
  }

  "tornado damage buildings & units" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        house1 = buildingConfigMock(regeneration = 0)
      )
    )

    val b0 = buildingMock(
      id = BuildingId(0),
      pos = Point(1, 1),
      count = 90,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player0)
    )
    val b1 = buildingMock(
      id = BuildingId(1),
      pos = Point(2, 1),
      count = 99,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player1)
    )
    val b2 = buildingMock(
      id = BuildingId(2),
      pos = Point(1, 2),
      count = 89,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player2)
    )
    val buildings = List(b0, b1, b2)

    val u0 = unitMock(
      id = UnitId(0),
      fromBuilding = b0,
      toBuilding = b1,
      startTime = 6,
      duration = 10,
      count = 77
    )
    val u1 = unitMock(
      id = UnitId(1),
      fromBuilding = b1,
      toBuilding = b2,
      startTime = 6,
      duration = 10,
      count = 55
    )
    val u2 = unitMock(
      id = UnitId(2),
      fromBuilding = b2,
      toBuilding = b0,
      startTime = 6,
      duration = 10,
      count = 66
    )
    val units = List(u0, u1, u2)

    val damagerConfig = damagerConfigMock(
      powerVsUnit = 40,
      powerVsBuilding = 40,
      radius = 0.01
    )

    val tornado1 = tornadoMock(points = Points(Point(0, 0), Point(2, 2)), startTime = 1, duration = 10, damagerConfig = damagerConfig)
    val tornado2 = tornadoMock(points = Points(Point(0, 0), Point(4, 2)), startTime = 1, duration = 10, damagerConfig = damagerConfig)

    val gameState = gameStateMock(
      time = 6,
      players = players,
      buildings = buildings,
      units = units,
      tornadoes = List(tornado1, tornado2),
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 6)
    val gameStateUpdate = diff(gameState, newGameState)

    val newU0 = u0.applyDamagers(List(tornado1), time = 6)
    newU0.count should be < u0.count

    val newU1 = u1.applyDamagers(List(tornado2), time = 6)
    newU1.count should be < u1.count

    gameStateUpdate.unitUpdates shouldBe List(
      newU0.updateDto,
      newU1.updateDto
    )
  }

  "units kills" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val u0 = unitMock(
      id = UnitId(0),
      fromBuilding = buildingMock(pos = Point(2, 2), owner = Some(player0)),
      toBuilding = buildingMock(pos = Point(4, 2)),
      startTime = 1,
      duration = 10,
      count = 0
    )
    val u1 = unitMock(
      id = UnitId(1),
      fromBuilding = buildingMock(pos = Point(4, 2), owner = Some(player1)),
      toBuilding = buildingMock(pos = Point(2, 4)),
      startTime = 1,
      duration = 10,
      count = 0.7
    )
    val u2 = unitMock(
      id = UnitId(2),
      fromBuilding = buildingMock(pos = Point(2, 4), owner = Some(player2)),
      toBuilding = buildingMock(pos = Point(2, 2)),
      startTime = 1,
      duration = 10,
      count = 66
    )
    val units = List(u0, u1, u2)

    val gameState = gameStateMock(
      time = 1,
      players = players,
      units = units
    )

    val newGameState = updateGameState(gameState, newTime = 2)
    val gameStateUpdate = diff(gameState, newGameState)

    gameStateUpdate.killUnits shouldBe List(
      UnitId(0),
      UnitId(1)
    )
  }

  "create bullets" in {
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val b = buildingMock(
      pos = Point(2, 2),
      buildingPrototype = BuildingPrototype(TOWER, LEVEL_3),
      owner = Some(player1),
      lastShootTime = -1000
    )

    val u0 = unitMock(
      id = UnitId(0),
      fromBuilding = buildingMock(pos = Point(2, 4), owner = Some(player2)),
      toBuilding = b,
      startTime = 0,
      duration = 10,
      count = 99
    )
    val u1 = unitMock(
      id = UnitId(1),
      fromBuilding = buildingMock(pos = Point(2, 4), owner = Some(player2)),
      toBuilding = b,
      startTime = 0,
      duration = 10,
      count = 88
    )
    val u2 = unitMock(
      id = UnitId(2),
      fromBuilding = buildingMock(pos = Point(3, 2), owner = Some(player2)),
      toBuilding = b,
      startTime = 0,
      duration = 7,
      count = 77
    )

    val config = gameConfigMock(
      buildings = buildingsConfigMock(
        tower3 = buildingConfigMock(
          shotPower = Some(0.14),
          regeneration = 0
        )
      ),
      shooting = shootingConfigMock(
        shootRadius = 1.1,
        speed = 0.25
      )
    )

    val gameState = gameStateMock(
      time = 0,
      buildings = List(b),
      units = List(u0, u1, u2),
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 5)
    val gameStateUpdate = diff(gameState, newGameState)

    val expectedBullets = createBullets(List(b), List(u0, u1, u2), time = 5, config)

    gameStateUpdate.newBullets shouldBe List(
      expectedBullets.head.dto(newGameState.time)
    )
  }

  def testItems(itemType: ItemType) =
    new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        itemType → gameItemStateMock(itemType, count = 1, lastUseTime = 0)
      )),
      PlayerId(1) → new ItemStates(Map(
        itemType → gameItemStateMock(itemType, count = 1, lastUseTime = 10)
      )),
      PlayerId(2) → new ItemStates(Map(
        itemType → gameItemStateMock(itemType, count = 0, lastUseTime = 0)
      ))
    ))

  def emptyChurchesProportion =
    new ChurchesProportion(Map(
      PlayerId(0) → 0.0,
      PlayerId(1) → 0.0,
      PlayerId(2) → 0.0
    ))
}