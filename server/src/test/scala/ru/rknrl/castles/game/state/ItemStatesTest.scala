//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import protos.ItemType._
import protos.PlayerId
import ru.rknrl.castles.kit.Mocks._

class ItemStatesTest extends WordSpec with Matchers {
  "getUpdateItemsStatesMessages" in {
    // У player0 различается count у фаербола и lastUseTime у вулкана
    // у player1 предметы не различаются

    val a = new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        FIREBALL → gameItemStateMock(FIREBALL, count = 1, lastUseTime = 0),
        VOLCANO → gameItemStateMock(VOLCANO, count = 1, lastUseTime = 0),
        TORNADO → gameItemStateMock(TORNADO, count = 1, lastUseTime = 0),
        STRENGTHENING → gameItemStateMock(STRENGTHENING, count = 1, lastUseTime = 0),
        ASSISTANCE → gameItemStateMock(ASSISTANCE, count = 1, lastUseTime = 0)
      )),
      PlayerId(1) → new ItemStates(Map(
        FIREBALL → gameItemStateMock(FIREBALL, count = 1, lastUseTime = 0),
        VOLCANO → gameItemStateMock(VOLCANO, count = 1, lastUseTime = 0),
        TORNADO → gameItemStateMock(TORNADO, count = 10, lastUseTime = 0),
        STRENGTHENING → gameItemStateMock(STRENGTHENING, count = 1, lastUseTime = 0),
        ASSISTANCE → gameItemStateMock(ASSISTANCE, count = 11, lastUseTime = 0)
      ))
    ))
    val b = new GameItems(Map(
      PlayerId(0) → new ItemStates(Map(
        FIREBALL → gameItemStateMock(FIREBALL, count = 0, lastUseTime = 0),
        VOLCANO → gameItemStateMock(VOLCANO, count = 1, lastUseTime = 10),
        TORNADO → gameItemStateMock(TORNADO, count = 1, lastUseTime = 0),
        STRENGTHENING → gameItemStateMock(STRENGTHENING, count = 1, lastUseTime = 0),
        ASSISTANCE → gameItemStateMock(ASSISTANCE, count = 1, lastUseTime = 0)
      )),
      PlayerId(1) → new ItemStates(Map(
        FIREBALL → gameItemStateMock(FIREBALL, count = 1, lastUseTime = 0),
        VOLCANO → gameItemStateMock(VOLCANO, count = 1, lastUseTime = 0),
        TORNADO → gameItemStateMock(TORNADO, count = 10, lastUseTime = 0),
        STRENGTHENING → gameItemStateMock(STRENGTHENING, count = 1, lastUseTime = 0),
        ASSISTANCE → gameItemStateMock(ASSISTANCE, count = 11, lastUseTime = 0)
      ))
    ))

    val config = gameConfigMock()

    val messages = GameStateDiff.getItemStatesUpdates(a, b, config, time = 10)

    messages shouldBe List(
      b.states(PlayerId(0)).dto(playerId = PlayerId(0), time = 10, config = config)
    )
  }
}
