package ru.rknrl.castles

import ru.rknrl.castles.account.AccountConfigMock
import ru.rknrl.castles.database.DbConfiguration
import ru.rknrl.castles.game.GameConfigMock
import ru.rknrl.core.social.SocialConfigs

object ConfigMock {
  def config = new Config(
    "host",
    123,
    124,
    new DbConfiguration("username", "host", 213, "pass", "database"),
    List.empty,
    new SocialConfigs(None, None, None),
    AccountConfigMock.config,
    GameConfigMock.gameConfig()
  )
}
