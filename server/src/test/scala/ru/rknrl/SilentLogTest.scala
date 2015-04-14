//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import org.scalatest.{Matchers, WordSpec}

class SilentLogTest extends WordSpec with Matchers {
  "result" in {
    val log = new SilentLog
    log.info("a")
    log.info("b")
    log.info("c")
    log.result shouldBe "a\nb\nc\n"
  }
}
