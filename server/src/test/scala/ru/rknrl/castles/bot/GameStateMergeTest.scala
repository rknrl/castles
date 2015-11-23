//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.bot

import org.scalatest.{Matchers, WordSpec}
import protos.ItemType.{FIREBALL, VOLCANO}
import protos._
import ru.rknrl.castles.bot.GameStateMerge.merge
import ru.rknrl.castles.game.state.{GameItems, ItemStates}
import ru.rknrl.castles.kit.Mocks._

class GameStateMergeTest extends WordSpec with Matchers {
  val players = Map(
    PlayerId(0) → playerMock(PlayerId(0)),
    PlayerId(1) → playerMock(PlayerId(1))
  )

  val items = new GameItems(Map(
    PlayerId(0) → new ItemStates(Map(
      FIREBALL → gameItemStateMock(FIREBALL, count = 1, lastUseTime = 10),
      VOLCANO → gameItemStateMock(VOLCANO, count = 2, lastUseTime = 30)
    )),
    PlayerId(1) → new ItemStates(Map.empty)
  ))

  val gameState = gameStateMock(
    time = 100,
    players = players,
    items = items,
    buildings = List(
      buildingMock(
        id = BuildingId(0),
        owner = None,
        count = 10,
        strengthening = None
      ),
      buildingMock(
        id = BuildingId(1),
        owner = Some(playerMock(PlayerId(1))),
        count = 20,
        strengthening = Some(strengtheningMock())
      )
    ),
    config = gameConfigMock(
      constants = constantsConfigMock(
        itemCooldown = 1000
      )
    )
  ).dto(PlayerId(0), List.empty)

  "empty" in {
    merge(gameState, GameStateUpdate()) shouldBe gameState
  }

  "buildingUpdated" in {
    val merged = merge(gameState, GameStateUpdate(
      buildingUpdates = List(
        BuildingUpdate(
          id = BuildingId(0),
          population = 5,
          owner = Some(PlayerId(0)),
          strengthened = true
        ),
        BuildingUpdate(
          id = BuildingId(1),
          population = 30,
          owner = Some(PlayerId(1)),
          strengthened = false
        )
      )
    ))

    merged.buildings shouldBe List(
      buildingMock(
        id = BuildingId(0),
        owner = Some(playerMock(PlayerId(0))),
        count = 5,
        strengthening = Some(strengtheningMock())
      ).dto,
      buildingMock(
        id = BuildingId(1),
        owner = Some(playerMock(PlayerId(1))),
        count = 30,
        strengthening = None
      ).dto
    )
  }

  "itemStatesUpdates" in {
    val merged = merge(gameState, GameStateUpdate(
      itemStatesUpdates = List(
        ItemStatesDTO(
          playerId = PlayerId(0),
          items = List(
            ItemStateDTO(
              itemType = FIREBALL,
              count = 0,
              millisFromStart = 200,
              cooldownDuration = 1000
            )
          )
        ),
        ItemStatesDTO(
          playerId = PlayerId(1), // <- Этот игноририруется потому что для другого игрока
          items = List(
            ItemStateDTO(
              itemType = VOLCANO,
              count = 1,
              millisFromStart = 150,
              cooldownDuration = 1000
            )
          )
        )
      )
    ))

    merged.itemStates.items shouldBe List(
      ItemStateDTO(
        itemType = FIREBALL,
        count = 0,
        millisFromStart = 200,
        cooldownDuration = 1000
      ),
      ItemStateDTO(
        itemType = VOLCANO,
        count = 2,
        millisFromStart = 70,
        cooldownDuration = 1000
      )
    )
  }
}
