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
import ru.rknrl.dto.PlayerId

class GameStateIsPlayerLoseTest extends WordSpec with Matchers {

  "false" in {
    val b0 = buildingMock(owner = Some(playerMock(PlayerId(0))))
    val b1 = buildingMock(owner = None)
    val b2 = buildingMock(owner = Some(playerMock(PlayerId(1))))

    val gameState = gameStateMock(buildings = List(b0, b1, b2))
    gameState.isPlayerLose(PlayerId(0)) shouldBe false
  }

  "true" in {
    val b0 = buildingMock(owner = Some(playerMock(PlayerId(1))))
    val b1 = buildingMock(owner = None)
    val b2 = buildingMock(owner = Some(playerMock(PlayerId(1))))

    val gameState = gameStateMock(buildings = List(b0, b1, b2))
    gameState.isPlayerLose(PlayerId(0)) shouldBe true
  }

}
