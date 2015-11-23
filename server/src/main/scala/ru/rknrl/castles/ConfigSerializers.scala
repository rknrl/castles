//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles

import net.liftweb.json.CustomSerializer
import net.liftweb.json.JsonAST._
import protos.BuildingLevel
import ru.rknrl.castles.account.{BuildingPrices, SkillUpgradePrices}

class BuildingPricesSerializer extends CustomSerializer[BuildingPrices](format ⇒ ( {
  case JObject(
  JField("level1", JInt(level1)) ::
    JField("level2", JInt(level2)) ::
    JField("level3", JInt(level3)) ::
    Nil) ⇒
    new BuildingPrices(Map(
      BuildingLevel.LEVEL_1 → level1.toInt,
      BuildingLevel.LEVEL_2 → level2.toInt,
      BuildingLevel.LEVEL_3 → level3.toInt
    ))
}, {
  case x: BuildingPrices ⇒ ???
}
  )
)

class SkillUpgradePricesSerializer extends CustomSerializer[SkillUpgradePrices](format ⇒ ( {
  case JObject(
  JField("1", JInt(l1)) ::
    JField("2", JInt(l2)) ::
    JField("3", JInt(l3)) ::
    JField("4", JInt(l4)) ::
    JField("5", JInt(l5)) ::
    JField("6", JInt(l6)) ::
    JField("7", JInt(l7)) ::
    JField("8", JInt(l8)) ::
    JField("9", JInt(l9)) ::
    Nil) ⇒
    new SkillUpgradePrices(Map(
      1 → l1.toInt,
      2 → l2.toInt,
      3 → l3.toInt,
      4 → l4.toInt,
      5 → l5.toInt,
      6 → l6.toInt,
      7 → l7.toInt,
      8 → l8.toInt,
      9 → l9.toInt
    ))
}, {
  case x: SkillUpgradePrices ⇒ ???
}
  )
)
