//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.game.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.game.Game.PersonalMessage
import ru.rknrl.castles.game.state.GameItems.getUpdateItemsStatesMessages
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.castles.rmi.B2C.UpdateItemStates
import ru.rknrl.dto.ItemType._
import ru.rknrl.dto.PlayerId

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

    val personalMessages = getUpdateItemsStatesMessages(a, b, config, time = 10)

    personalMessages shouldBe List(
      PersonalMessage(PlayerId(0), UpdateItemStates(b.states(PlayerId(0)).dto(time = 10, config = config)))
    )

  }
}