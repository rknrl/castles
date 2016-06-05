//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.database

trait Calendar {
  def getCurrentWeek: Int
  def getCurrentMillis: Long
}

class RealCalendar extends Calendar {
  val week = 7 * 24 * 60 * 60 * 1000

  def getCurrentMillis: Long = System.currentTimeMillis

  def getCurrentWeek: Int = (System.currentTimeMillis / week).toInt
}

class FakeCalendar(week: Int, millis: Long = 3000) extends Calendar {
  def getCurrentMillis: Long = millis

  def getCurrentWeek: Int = week
}