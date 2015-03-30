//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

/** В отличие от стандартного assert кидает эксепшн, а не эррор */
object Assertion {
  def check(assertion: Boolean) {
    if (!assertion)
      throw new Exception("assertion failed")
  }

  def check(assertion: Boolean, message: => Any) {
    if (!assertion)
      throw new Exception("assertion failed: " + message)
  }
}
