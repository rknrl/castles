//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import org.scalatest.{Matchers, WordSpec}

class AssertionTest extends WordSpec with Matchers {
  "check" in {
    Assertion.check(2 == 2)
    a[Exception] shouldBe thrownBy {
      Assertion.check(2 == 1)
    }
  }

  "check with message" in {
    Assertion.check(2 == 2, "my message")
    a[Exception] shouldBe thrownBy {
      Assertion.check(2 == 1, "my message")
    }
  }
}
