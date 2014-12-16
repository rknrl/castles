package ru.rknrl.castles.config

import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.account.AccountConfig.{BuildingPrices, SkillUpgradePrices}
import ru.rknrl.castles.config.Config.{BuildingLevelToFactor, BuildingsConfig}
import ru.rknrl.castles.game._
import ru.rknrl.core.social.JsonUtils._
import ru.rknrl.core.social.Products.Products
import ru.rknrl.core.social.SocialConfigs
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}
import spray.json._

object ConfigJsonProtocol extends DefaultJsonProtocol {

  implicit object BuildingPricesJsonFormat extends RootJsonReader[BuildingPrices] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        Map(
          BuildingLevel.LEVEL_1 → getInt("level1"),
          BuildingLevel.LEVEL_2 → getInt("level2"),
          BuildingLevel.LEVEL_3 → getInt("level3")
        )
      case _ ⇒ deserializationError("BuildingPrices isn't object, but " + value)
    }
  }

  implicit object SkillUpgradePricesJsonFormat extends RootJsonReader[SkillUpgradePrices] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        var prices = Map[Int, Int]()
        for (i ← 1 to 9) prices = prices + (i → getInt(i.toString))
        prices
      case _ ⇒ deserializationError("SkillUpgradePrices isn't object, but " + value)
    }
  }

  implicit object AccountConfigJsonFormat extends RootJsonReader[AccountConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new AccountConfig(
          initGold = getInt("initGold"),
          initItemCount = getInt("initItemCount"),
          buildingPrices = map("buildingPrices").convertTo[BuildingPrices],
          skillUpgradePrices = map("skillUpgradePrices").convertTo[SkillUpgradePrices],
          itemPrice = getInt("itemPrice")
        )
      case _ ⇒ deserializationError("Account isn't object, but " + value)
    }
  }

  implicit object StatJsonFormat extends RootJsonReader[Stat] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new Stat(
          attack = getDouble("attack"),
          defence = getDouble("defence"),
          speed = getDouble("speed")
        )
      case _ ⇒ deserializationError("Stat isn't object, but" + value)
    }
  }

  implicit object BuildingConfigJsonFormat extends RootJsonReader[BuildingConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new BuildingConfig(
          regeneration = getDouble("regeneration"),
          startPopulation = getInt("startPopulation"),
          stat = map("stat").convertTo[Stat]
        )
      case _ ⇒ deserializationError("BuildingConfig isn't object, but " + value)
    }
  }

  implicit object BuildingLevelToFactorJsonFormat extends RootJsonReader[BuildingLevelToFactor] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        Map(
          BuildingLevel.LEVEL_1 → getDouble("level1"),
          BuildingLevel.LEVEL_2 → getDouble("level2"),
          BuildingLevel.LEVEL_3 → getDouble("level3")
        )
      case _ ⇒ deserializationError("BuildingLevelToFactor isn't object, but " + value)
    }
  }

  implicit object ConstantsJsonFormat extends RootJsonReader[Constants] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new Constants(
          unitToExitFactor = getDouble("unitToExitFactor"),
          itemCooldown = getInt("itemCooldown")
        )
      case _ ⇒ deserializationError("Constants isn't object, but " + value)
    }
  }

  implicit object FireballJsonFormat extends RootJsonReader[FireballConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new FireballConfig(
          damage = getDouble("damage"),
          flyDuration = getInt("flyDuration")
        )
      case _ ⇒ deserializationError("Fireball isn't object, but " + value)
    }
  }

  implicit object VolcanoJsonFormat extends RootJsonReader[VolcanoConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new VolcanoConfig(
          damage = getDouble("damage"),
          duration = getInt("duration")
        )
      case _ ⇒ deserializationError("Volcano isn't object, but " + value)
    }
  }

  implicit object TornadoJsonFormat extends RootJsonReader[TornadoConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new TornadoConfig(
          damage = getDouble("damage"),
          duration = getInt("duration"),
          speed = getDouble("speed")
        )
      case _ ⇒ deserializationError("Tornado isn't object, but " + value)
    }
  }

  implicit object StrengtheningJsonFormat extends RootJsonReader[StrengtheningConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new StrengtheningConfig(
          factor = getDouble("factor"),
          duration = getInt("duration")
        )
      case _ ⇒ deserializationError("Strengthening isn't object, but " + value)
    }
  }

  implicit object ShootingJsonFormat extends RootJsonReader[ShootingConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new ShootingConfig(
          damage = getDouble("damage"),
          speed = getDouble("speed"),
          shootInterval = getInt("shootInterval"),
          shootRadius = getDouble("shootRadius")
        )
      case _ ⇒ deserializationError("Shooting isn't object, but " + value)
    }
  }

  implicit object AssistanceJsonFormat extends RootJsonReader[AssistanceConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map
        new AssistanceConfig(
          count = getInt("count")
        )
      case _ ⇒ deserializationError("Assistance isn't object, but " + value)
    }
  }

  implicit object BuildingsConfigJsonFormat extends RootJsonReader[BuildingsConfig] {
    override def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        Map(
          BuildingType.TOWER → map("tower").convertTo[BuildingConfig],
          BuildingType.HOUSE → map("house").convertTo[BuildingConfig],
          BuildingType.CHURCH → map("church").convertTo[BuildingConfig]
        )
      case _ ⇒ deserializationError("BuildingsConfig isn't object, but " + value)
    }
  }

  implicit object GameConfigJsonFormat extends RootJsonReader[GameConfig] {
    def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        new GameConfig(
          constants = map("constants").convertTo[Constants],
          buildingsConfig = map("buildings").convertTo[BuildingsConfig],
          levelToFactor = map("levelToFactor").convertTo[BuildingLevelToFactor],
          fireball = map("fireball").convertTo[FireballConfig],
          volcano = map("volcano").convertTo[VolcanoConfig],
          tornado = map("tornado").convertTo[TornadoConfig],
          strengthening = map("strengthening").convertTo[StrengtheningConfig],
          shooting = map("shooting").convertTo[ShootingConfig],
          assistance = map("assistance").convertTo[AssistanceConfig]
        )

      case _ ⇒ deserializationError("GameConfig isn't object, but " + value)
    }
  }

  import ru.rknrl.core.social.ProductJsonProtocol._
  import ru.rknrl.core.social.SocialConfigJsonProtocol._

  implicit object ConfigJsonFormat extends RootJsonReader[Config] {
    def read(value: JsValue) = value match {
      case JsObject(map) ⇒
        implicit val m = map

        new Config(
          host = getNotEmptyString("host"),
          gamePort = getInt("gamePort"),
          policyPort = getInt("policyPort"),
          products = map("products").convertTo[Products],
          social = map("social").convertTo[SocialConfigs],
          account = map("account").convertTo[AccountConfig],
          game = map("game").convertTo[GameConfig]
        )

      case _ ⇒ deserializationError("Config isn't object, but " + value)
    }
  }

}
