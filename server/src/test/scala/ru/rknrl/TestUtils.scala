//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import org.scalatest.Matchers

object TestUtils extends Matchers {
  def checkEquals[T](a: () ⇒ T, b: () ⇒ T): Unit = {
    a() shouldEqual a()
    b() shouldEqual b()

    a() shouldNot equal(b())

    a() shouldNot equal(a().toString)
    b() shouldNot equal(b().toString)
  }

  def checkHashCode[T](a: () ⇒ T, b: () ⇒ T): Unit = {
    a() shouldEqual a()
    b() shouldEqual b()

    a() shouldNot equal(b())

    a() shouldNot equal(a().toString)
    b() shouldNot equal(b().toString)
  }
}
