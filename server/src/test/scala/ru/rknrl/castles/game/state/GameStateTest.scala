//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.state.Assistance.castToUnit
import ru.rknrl.castles.game.state.Bullets._
import ru.rknrl.castles.game.state.Fireballs.castToFireball
import ru.rknrl.castles.game.state.GameItems._
import ru.rknrl.castles.game.state.Moving.moveActionsToExitUnits
import ru.rknrl.castles.game.state.Strengthening.castToStrengthening
import ru.rknrl.castles.game.state.Tornadoes.castToTornado
import ru.rknrl.castles.game.state.Volcanoes.castToVolcano
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.rmi.B2C._
import ru.rknrl.core.points.{Point, Points}
import ru.rknrl.dto.BuildingLevel._
import ru.rknrl.dto.BuildingType._
import ru.rknrl.dto.ItemType._
import ru.rknrl.dto._

class GameStateTest extends WordSpec with Matchers {

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

  "no changes" in {
    val gameState = gameStateMock(time = 1)
    val newGameState = updateGameState(gameState, newTime = 1)
    checkGameState(newGameState, gameState)
  }

  "time" in {
    val gameState = gameStateMock(time = 1)
    val newGameState = updateGameState(gameState, newTime = 2)
    newGameState.time shouldBe 2
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
    val newGameState = updateGameState(gameState, newTime = 3)

    val newA = a.regenerate(2, config)
    val newB = b.regenerate(2, config)

    checkBuildings(
      newGameState.buildings,
      List(
        newA,
        newB
      )
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

    val exitUnits = moveActionsToExitUnits(moveActions, buildings, new UnitIdIterator, time = 3).toSeq

    checkUnits(
      newGameState.units,
      exitUnits
    )

    checkBuildings(
      newGameState.buildings,
      gameState.buildings.map(_.applyExitUnits(exitUnits))
    )
  }

  "enterUnits" in {
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
      time = 0,
      buildings = buildings,
      config = config,
      units = List(
        unitMock(
          id = UnitId(0),
          fromBuilding = a,
          toBuilding = b,
          count = 10,
          startTime = 1,
          duration = 10
        ),
        unitMock(
          id = UnitId(1),
          fromBuilding = c,
          toBuilding = a,
          count = 10,
          startTime = 2,
          duration = 11
        ),
        unitMock(
          id = UnitId(2),
          fromBuilding = c,
          toBuilding = b,
          count = 10,
          startTime = 0,
          duration = 20
        )
      )
    )

    val newGameState = updateGameState(gameState, newTime = 99)
    newGameState.units.size shouldBe 0
    checkBuildings(
      newGameState.buildings,
      gameState.buildings.map(_.applyEnterUnits(gameState.units, gameState.config))
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

    newGameState.items.states(PlayerId(0)).items(ASSISTANCE).count shouldBe 0
    newGameState.items.states(PlayerId(1)).items(ASSISTANCE).count shouldBe 1
    newGameState.items.states(PlayerId(2)).items(ASSISTANCE).count shouldBe 2

    val churchesProportion = new ChurchesProportion(Map(
      PlayerId(0) → 0.0,
      PlayerId(1) → 1.0
    ))

    val expectedIterator = new UnitIdIterator

    checkUnits(
      newGameState.units,
      List(
        castToUnit(
          player0 → BuildingId(0),
          gameState.buildings,
          gameState.config,
          churchesProportion,
          expectedIterator,
          assistancePositions,
          time = 1
        ),
        castToUnit(
          player1 → BuildingId(1),
          gameState.buildings,
          gameState.config,
          churchesProportion,
          expectedIterator,
          assistancePositions,
          time = 1
        )
      )
    )
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

    newGameState.items.states(PlayerId(0)).items(STRENGTHENING).count shouldBe 0
    newGameState.items.states(PlayerId(1)).items(STRENGTHENING).count shouldBe 1
    newGameState.items.states(PlayerId(2)).items(STRENGTHENING).count shouldBe 2

    val churchesProportion = new ChurchesProportion(Map(
      PlayerId(0) → 0,
      PlayerId(1) → 0,
      PlayerId(2) → 0
    ))

    checkBuildings(
      newGameState.buildings,
      List(
        b0.copy(newStrengthening = Some(castToStrengthening(
          cast1,
          time = 2,
          churchesProportion = churchesProportion,
          config = gameState.config
        ))),
        b1,
        b2.copy(newStrengthening = Some(castToStrengthening(
          cast2,
          time = 2,
          churchesProportion = churchesProportion,
          config = gameState.config
        )))
      )
    )
    checkBuildings(
      updateGameState(newGameState, newTime = 99999).buildings,
      List(
        b0.copy(newStrengthening = None),
        b1,
        b2.copy(newStrengthening = None)
      )
    )
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

    newGameState.items.states(PlayerId(0)).items(FIREBALL).count shouldBe 0
    newGameState.items.states(PlayerId(1)).items(FIREBALL).count shouldBe 1
    newGameState.items.states(PlayerId(2)).items(FIREBALL).count shouldBe 2

    val fireball1 = castToFireball(cast1, newGameState.time, churchesProportion, gameState.config)
    val fireball2 = castToFireball(cast2, newGameState.time, churchesProportion, gameState.config)

    newGameState.fireballs shouldBe List(
      fireball1,
      fireball2
    )

    updateGameState(newGameState, newTime = 99999).fireballs.size shouldBe 0
  }

  "fireball damage buildings & units" in {
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
      pos = Point(2, 2),
      count = 90,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player0)
    )
    val b1 = buildingMock(
      id = BuildingId(1),
      pos = Point(4, 2),
      count = 99,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player1)
    )
    val b2 = buildingMock(
      id = BuildingId(2),
      pos = Point(2, 4),
      count = 89,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player2)
    )
    val buildings = List(b0, b1, b2)

    val u0 = unitMock(
      id = UnitId(0),
      fromBuilding = b0,
      toBuilding = b1,
      startTime = 1,
      duration = 10,
      count = 77
    )
    val u1 = unitMock(
      id = UnitId(1),
      fromBuilding = b1,
      toBuilding = b2,
      startTime = 1,
      duration = 10,
      count = 55
    )
    val u2 = unitMock(
      id = UnitId(2),
      fromBuilding = b2,
      toBuilding = b0,
      startTime = 1,
      duration = 10,
      count = 66
    )
    val units = List(u0, u1, u2)

    val damagerConfig = damagerConfigMock(
      powerVsUnit = 40,
      powerVsBuilding = 20,
      radius = 1
    )

    val fireball1 = fireballMock(pos = Point(2, 2), startTime = 1, duration = 1, damagerConfig = damagerConfig)
    val fireball2 = fireballMock(pos = Point(4, 2), startTime = 1, duration = 1, damagerConfig = damagerConfig)

    val gameState = gameStateMock(
      time = 1,
      players = players,
      buildings = buildings,
      units = units,
      fireballs = List(fireball1, fireball2),
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 2)

    checkBuildings(
      newGameState.buildings,
      List(
        b0.applyDamagers(List(fireball1), time = 2),
        b1.applyDamagers(List(fireball2), time = 2),
        b2
      )
    )

    checkUnits(
      newGameState.units,
      List(
        u0.applyDamagers(List(fireball1), time = 2),
        u1.applyDamagers(List(fireball2), time = 2),
        u2
      )
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

    newGameState.items.states(PlayerId(0)).items(VOLCANO).count shouldBe 0
    newGameState.items.states(PlayerId(1)).items(VOLCANO).count shouldBe 1
    newGameState.items.states(PlayerId(2)).items(VOLCANO).count shouldBe 2

    val volcano1 = castToVolcano(cast1, newGameState.time, churchesProportion, gameState.config)
    val volcano2 = castToVolcano(cast2, newGameState.time, churchesProportion, gameState.config)

    newGameState.volcanoes shouldBe List(
      volcano1,
      volcano2
    )

    updateGameState(newGameState, newTime = 99999).fireballs.size shouldBe 0
  }

  "volcano damage buildings & units" in {
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
      pos = Point(2, 2),
      count = 90,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player0)
    )
    val b1 = buildingMock(
      id = BuildingId(1),
      pos = Point(4, 2),
      count = 99,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player1)
    )
    val b2 = buildingMock(
      id = BuildingId(2),
      pos = Point(2, 4),
      count = 89,
      buildingPrototype = BuildingPrototype(HOUSE, LEVEL_1),
      owner = Some(player2)
    )
    val buildings = List(b0, b1, b2)

    val u0 = unitMock(
      id = UnitId(0),
      fromBuilding = b0,
      toBuilding = b1,
      startTime = 1,
      duration = 10,
      count = 77
    )
    val u1 = unitMock(
      id = UnitId(1),
      fromBuilding = b1,
      toBuilding = b2,
      startTime = 1,
      duration = 10,
      count = 55
    )
    val u2 = unitMock(
      id = UnitId(2),
      fromBuilding = b2,
      toBuilding = b0,
      startTime = 1,
      duration = 10,
      count = 66
    )
    val units = List(u0, u1, u2)

    val damagerConfig = damagerConfigMock(
      powerVsUnit = 20,
      powerVsBuilding = 40,
      radius = 1
    )

    val volcano1 = volcanoMock(pos = Point(2, 2), startTime = 1, duration = 1, damagerConfig = damagerConfig)
    val volcano2 = volcanoMock(pos = Point(4, 2), startTime = 1, duration = 1, damagerConfig = damagerConfig)

    val gameState = gameStateMock(
      time = 1,
      players = players,
      buildings = buildings,
      units = units,
      volcanoes = List(volcano1, volcano2),
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 2)

    checkBuildings(
      newGameState.buildings,
      List(
        b0.applyDamagers(List(volcano1), time = 2),
        b1.applyDamagers(List(volcano2), time = 2),
        b2
      )
    )

    checkUnits(
      newGameState.units,
      List(
        u0.applyDamagers(List(volcano1), time = 2),
        u1.applyDamagers(List(volcano2), time = 2),
        u2
      )
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

    newGameState.items.states(PlayerId(0)).items(TORNADO).count shouldBe 0
    newGameState.items.states(PlayerId(1)).items(TORNADO).count shouldBe 1
    newGameState.items.states(PlayerId(2)).items(TORNADO).count shouldBe 2

    val tornado1 = castToTornado(cast1, newGameState.time, churchesProportion, gameState.config)
    val tornado2 = castToTornado(cast2, newGameState.time, churchesProportion, gameState.config)

    newGameState.tornadoes shouldBe List(
      tornado1,
      tornado2
    )

    updateGameState(newGameState, newTime = 99999).fireballs.size shouldBe 0
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

    checkBuildings(
      newGameState.buildings,
      List(
        b0.applyDamagers(List(tornado1), time = 6),
        b1.applyDamagers(List(tornado2), time = 6),
        b2
      )
    )

    val newU0 = u0.applyDamagers(List(tornado1), time = 6)
    newU0.count should be < u0.count

    val newU1 = u1.applyDamagers(List(tornado2), time = 6)
    newU1.count should be < u1.count

    checkUnits(
      newGameState.units,
      List(
        newU0,
        newU1,
        u2
      )
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

    checkUnits(
      newGameState.units,
      List(
        u2
      )
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


    val expectedBullets = createBullets(List(b), List(u0, u1, u2), time = 5, config)

    newGameState.bullets shouldBe expectedBullets

    checkBuildings(
      newGameState.buildings,
      List(b.applyShots(time = 5, expectedBullets))
    )

    updateGameState(newGameState, newTime = 9).bullets shouldBe empty

    checkUnits(
      updateGameState(newGameState, newTime = 9).units,
      List(u0.applyBullets(expectedBullets), u1)
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

  "not valid fireballs casts" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val config = gameConfigMock(
      constants = constantsConfigMock(itemCooldown = 100)
    )

    val items = testItems(FIREBALL)

    val cast0 = PlayerId(0) → PointDTO(0, 0)
    val cast1 = PlayerId(1) → PointDTO(0, 0)
    val cast2 = PlayerId(2) → PointDTO(0, 0)

    val gameState = gameStateMock(
      time = 0,
      items = items,
      players = players,
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 100, fireballCasts = Map(cast0, cast1, cast2))
    newGameState.fireballs shouldBe List(
      castToFireball(cast0, time = 100, emptyChurchesProportion, config)
    )
  }

  "not valid volcano casts" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val config = gameConfigMock(
      constants = constantsConfigMock(itemCooldown = 100)
    )

    val items = testItems(VOLCANO)

    val cast0 = PlayerId(0) → PointDTO(0, 0)
    val cast1 = PlayerId(1) → PointDTO(0, 0)
    val cast2 = PlayerId(2) → PointDTO(0, 0)

    val gameState = gameStateMock(
      time = 0,
      items = items,
      players = players,
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 100, volcanoCasts = Map(cast0, cast1, cast2))
    newGameState.volcanoes shouldBe List(
      castToVolcano(cast0, time = 100, emptyChurchesProportion, config)
    )
  }

  "not valid tornado casts" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val config = gameConfigMock(
      constants = constantsConfigMock(itemCooldown = 100)
    )

    val items = testItems(TORNADO)

    val cast0 = PlayerId(0) → CastTornadoDTO(List(PointDTO(0, 0), PointDTO(1, 1)))
    val cast1 = PlayerId(1) → CastTornadoDTO(List(PointDTO(0, 0), PointDTO(1, 1)))
    val cast2 = PlayerId(2) → CastTornadoDTO(List(PointDTO(0, 0), PointDTO(1, 1)))

    val gameState = gameStateMock(
      time = 0,
      items = items,
      players = players,
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 100, tornadoCasts = Map(cast0, cast1, cast2))
    newGameState.tornadoes shouldBe List(
      castToTornado(cast0, time = 100, emptyChurchesProportion, config)
    )
  }

  "not valid assistance casts" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val config = gameConfigMock(
      constants = constantsConfigMock(itemCooldown = 100)
    )

    val b0 = buildingMock(id = BuildingId(0), owner = Some(player0))
    val b1 = buildingMock(id = BuildingId(1), owner = Some(player1))
    val b2 = buildingMock(id = BuildingId(2), owner = Some(player2))
    val buildings = List(b0, b1, b2)

    val items = testItems(ASSISTANCE)

    val cast0 = PlayerId(0) → BuildingId(0)
    val cast1 = PlayerId(1) → BuildingId(1)
    val cast2 = PlayerId(2) → BuildingId(2)

    val assistancePositions = Map(
      PlayerId(0) → Point(1, 2),
      PlayerId(1) → Point(50, 10),
      PlayerId(2) → Point(70, 70)
    )

    val gameState = gameStateMock(
      time = 0,
      items = items,
      players = players,
      buildings = buildings,
      config = config,
      assistancePositions = assistancePositions
    )

    val newGameState = updateGameState(gameState, newTime = 100, assistanceCasts = Map(cast0, cast1, cast2))
    checkUnits(
      newGameState.units,
      List(castToUnit(
        player0 → BuildingId(0),
        time = 100,
        buildings = buildings,
        config = config,
        churchesProportion = emptyChurchesProportion,
        unitIdIterator = new UnitIdIterator,
        assistancePositions = assistancePositions
      ))
    )
  }

  "not valid strengthening casts" in {
    val player0 = playerMock(PlayerId(0))
    val player1 = playerMock(PlayerId(1))
    val player2 = playerMock(PlayerId(2))

    val players = Map(
      PlayerId(0) → player0,
      PlayerId(1) → player1,
      PlayerId(2) → player2
    )

    val config = gameConfigMock(
      constants = constantsConfigMock(itemCooldown = 100),
      buildings = buildingsConfigMock(
        house1 = buildingConfigMock(
          regeneration = 0
        )
      )
    )

    val b0 = buildingMock(id = BuildingId(0), owner = Some(player0))
    val b1 = buildingMock(id = BuildingId(1), owner = Some(player1))
    val b2 = buildingMock(id = BuildingId(2), owner = Some(player2))
    val buildings = List(b0, b1, b2)

    val items = testItems(STRENGTHENING)

    val cast0 = PlayerId(0) → BuildingId(0)
    val cast1 = PlayerId(1) → BuildingId(1)
    val cast2 = PlayerId(2) → BuildingId(2)

    val gameState = gameStateMock(
      time = 0,
      items = items,
      players = players,
      buildings = buildings,
      config = config
    )

    val newGameState = updateGameState(gameState, newTime = 100, strengtheningCasts = Map(cast0, cast1, cast2))

    val expectedStrengthenings = List(
      castToStrengthening(cast0, time = 100, emptyChurchesProportion, config)
    )

    checkBuildings(
      newGameState.buildings,
      gameState.buildings.map(_.applyStrengthening(expectedStrengthenings))
    )
  }

}