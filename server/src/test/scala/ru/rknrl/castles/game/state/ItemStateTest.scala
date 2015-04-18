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
import ru.rknrl.dto.ItemType.FIREBALL

class ItemStateTest extends WordSpec with Matchers {

  "use" in {
    val state = ItemState(
      itemType = FIREBALL,
      count = 10,
      lastUseTime = 0,
      useCount = 0
    )
    state.use(time = 100) shouldBe ItemState(
      itemType = FIREBALL,
      count = 9,
      lastUseTime = 100,
      useCount = 1
    )
  }

  "use empty" in {
    a[Exception] shouldBe thrownBy {
      ItemState(
        itemType = FIREBALL,
        count = 0,
        lastUseTime = 0,
        useCount = 0
      ).use(time = 100)
    }
  }

  "dto" in {
    val state = ItemState(
      itemType = FIREBALL,
      count = 7,
      lastUseTime = 110,
      useCount = 1
    )
    val dto = state.dto(
      time = 200,
      config = gameConfigMock(
        constants = constantsConfigMock(itemCooldown = 100)
      )
    )

    dto.itemType shouldBe FIREBALL
    dto.count shouldBe 7
    dto.cooldownDuration shouldBe 100
    dto.millisFromStart shouldBe 90
  }

}
