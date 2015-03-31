//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{FreeSpec, Matchers}
import ru.rknrl.TestUtils
import ru.rknrl.dto.CommonDTO.ItemType

class SkillsTest extends FreeSpec with Matchers {

  "не все скилы" in {
    TestUtils.equals(new Item(ItemType.FIREBALL, 10), new Item(ItemType.FIREBALL, 11))
  }

  "set" in {

  }

  "upgrade" in {

  }

  "totalLevel" in {

  }

  "isLastTotalLevel" in {

  }

  "nextTotalLevel" in {

  }

  "stat" in {

  }

  "dto" in {

  }

  "parse from dto" in {

  }
}
