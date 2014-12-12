package ru.rknrl.castles.config

import ru.rknrl.castles.account.AccountConfig
import ru.rknrl.castles.game._
import ru.rknrl.core.social.SocialConfigs
import ru.rknrl.dto.CommonDTO.{BuildingLevel, BuildingType}


object Config {
  type BuildingsConfig = Map[BuildingType, BuildingConfig]
  type BuildingLevelToFactor = Map[BuildingLevel, Double]
}

class Config(val host: String,
             val gamePort: Int,
             val policyPort: Int,
             val social: SocialConfigs,
             val account: AccountConfig,
             val game: GameConfig) {
  val regeneration = 0.0002
  val speed = 0.00005
}
