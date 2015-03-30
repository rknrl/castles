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
  def checkEquals[T](factories: Seq[() ⇒ T]): Unit = {
    for (a ← factories;
         b ← factories
         if a != b) {

      a() shouldEqual a()
      b() shouldEqual b()

      a() shouldNot equal(b())
      b() shouldNot equal(a())

      a() shouldNot equal(a().toString)
      b() shouldNot equal(b().toString)
    }
  }

  def checkHashCode[T](factories: Seq[() ⇒ T]): Unit = {
    for (a ← factories;
         b ← factories
         if a != b) {
      a().hashCode shouldEqual a().hashCode
      b().hashCode shouldEqual b().hashCode

      a().hashCode shouldNot equal(b().hashCode)
      b().hashCode shouldNot equal(a().hashCode)
    }
  }
}
