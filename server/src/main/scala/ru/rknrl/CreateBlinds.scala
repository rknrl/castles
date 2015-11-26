//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl

import java.io.PrintWriter

import scala.io.Source

object CreateBlinds {
  val limitTemplate = Source.fromFile("/Users/tolyayanot/dev/cashpokerpro/poker/server/conf/poker-engine/limit.template").mkString
  val noLimitTemplate = Source.fromFile("/Users/tolyayanot/dev/cashpokerpro/poker/server/conf/poker-engine/no-limit.template").mkString
  val potLimitTemplate = Source.fromFile("/Users/tolyayanot/dev/cashpokerpro/poker/server/conf/poker-engine/pot-limit.template").mkString
  val outDir = "/Users/tolyayanot/dev/cashpokerpro/poker/server/conf/poker-engine/"

  val RUB = 100

  case class Blinds(small: Double, big: Double) {
    def smallStr = str(small)

    def bigStr = str(big)

    private def str(d: Double): String = {
      var s = "%.2f".format(d).replaceAll(",", ".")
      if (s == "0.00") return "0"
      if (s.endsWith(".00"))
        s = s.substring(0, s.length - 3)
      if (s.startsWith("0."))
        s = s.substring(1, s.length)
      s
    }
  }

  val blinds = List(
    Blinds(0.01, 0.02),
    Blinds(0.02, 0.04),
    Blinds(0.05, 0.10),
    Blinds(0.10, 0.25),
    Blinds(0.25, 0.50),
    Blinds(0.50, 1.00),
    Blinds(1, 2),
    Blinds(2, 4),
    Blinds(3, 6),
    Blinds(4, 8),
    Blinds(5, 10),
    Blinds(10, 15),
    Blinds(10, 20),
    Blinds(15, 30),
    Blinds(10, 20),
    Blinds(20, 40),
    Blinds(25, 50),
    Blinds(30, 60),
    Blinds(40, 80),
    Blinds(50, 100),
    Blinds(60, 120),
    Blinds(75, 150),
    Blinds(80, 160),
    Blinds(100, 200),
    Blinds(150, 300),
    Blinds(200, 400),
    Blinds(300, 600),
    Blinds(400, 800),
    Blinds(600, 1200)
  )

  def main(args: Array[String]) {
    for (blind ← blinds) {
      createNoLimit(blind)
      createPotLimit(blind)
      createLimit(blind)
    }
    println("done")
  }

  def createNoLimit(blind: Blinds): Unit = {
    val s = noLimitTemplate
      .replace("_NAME_", blind.smallStr + "-" + blind.bigStr)
      .replace("_DESC_", blind.smallStr + "/" + blind.bigStr)
      .replace("_UNIT_", (blind.small * RUB).toInt.toString)
      .replace("_BEST_BUY_IN_", (blind.small * RUB * 100).toInt.toString)
      .replace("_MAX_BUY_IN_", (blind.small * RUB * 200).toInt.toString)
      .replace("_BUY_IN_", (blind.small * RUB * 20).toInt.toString)
      .replace("_SMALL_", (blind.small * RUB).toInt.toString)
      .replace("_BIG_", (blind.big * RUB).toInt.toString)

    val name = blind.smallStr + "-" + blind.bigStr + "-no-limit"
    println('"' + name + '"' + ',')
    val out = new PrintWriter(outDir + "poker." + name + ".xml", "UTF-8")
    try {
      out.write(s)
    } finally {
      out.close()
    }
  }

  def createLimit(blind: Blinds): Unit = {
    val s = limitTemplate
      .replace("_NAME_", blind.smallStr + "-" + blind.bigStr)
      .replace("_DESC_", blind.smallStr + "/" + blind.bigStr)
      .replace("_UNIT_", (blind.small * RUB).toInt.toString)
      .replace("_BEST_BUY_IN_", (blind.small * RUB * 30).toInt.toString)
      .replace("_BUY_IN_", (blind.small * RUB * 5).toInt.toString)
      .replace("_SMALL_", (blind.small * RUB).toInt.toString)
      .replace("_BIGBET_", (blind.big * RUB * 2).toInt.toString)
      .replace("_BIG_", (blind.big * RUB).toInt.toString)

    val name = blind.smallStr + "-" + blind.bigStr + "-limit"
    println('"' + name + '"' + ',')
    val out = new PrintWriter(outDir + "poker." + name + ".xml", "UTF-8")
    try {
      out.write(s)
    } finally {
      out.close()
    }
  }

  def createPotLimit(blind: Blinds): Unit = {
    val s = potLimitTemplate
      .replace("_NAME_", blind.smallStr + "-" + blind.bigStr)
      .replace("_DESC_", blind.smallStr + "/" + blind.bigStr)
      .replace("_UNIT_", (blind.small * RUB).toInt.toString)
      .replace("_BEST_BUY_IN_", (blind.small * RUB * 100).toInt.toString)
      .replace("_MAX_BUY_IN_", (blind.small * RUB * 200).toInt.toString)
      .replace("_BUY_IN_", (blind.small * RUB * 20).toInt.toString)
      .replace("_SMALL_", (blind.small * RUB).toInt.toString)
      .replace("_BIG_", (blind.big * RUB).toInt.toString)

    val name = blind.smallStr + "-" + blind.bigStr + "-pot-limit"
    println('"' + name + '"' + ',')
    val out = new PrintWriter(outDir + "poker." + name + ".xml", "UTF-8")
    try {
      out.write(s)
    } finally {
      out.close()
    }
  }
}
