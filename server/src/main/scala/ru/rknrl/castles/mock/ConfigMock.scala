package ru.rknrl.castles.mock

import ru.rknrl.base.database.DbConfiguration
import ru.rknrl.castles.Config
import ru.rknrl.core.social.SocialConfigs

object ConfigMock {
  def config = new Config(
    "127.0.0.1",
    123,
    124,
    8080,
    new DbConfiguration("username", "host", 213, "pass", "database"),
    List.empty,
    new SocialConfigs(None, None, None),
    AccountConfigMock.config,
    GameConfigMock.gameConfig()
  )
}
