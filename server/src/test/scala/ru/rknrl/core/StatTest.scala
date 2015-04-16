//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core

import org.scalatest.{Matchers, WordSpec}

class StatTest extends WordSpec with Matchers {
  "non positive" in {
    a[Exception] shouldBe thrownBy {
      Stat(0, 1, 1)
    }
    a[Exception] shouldBe thrownBy {
      Stat(-1, 1, 1)
    }
    a[Exception] shouldBe thrownBy {
      Stat(1, 0, 1)
    }
    a[Exception] shouldBe thrownBy {
      Stat(1, -1, 1)
    }
    a[Exception] shouldBe thrownBy {
      Stat(1, 1, 0)
    }
    a[Exception] shouldBe thrownBy {
      Stat(1, 1, -1)
    }
  }

  "*" in {
    val newStat = Stat(2, 4, 2.2) * Stat(1.1, 1.2, 1.4)
    newStat.attack shouldBe 2.2
    newStat.defence shouldBe 4.8
    newStat.speed shouldBe 3.08
  }
}
