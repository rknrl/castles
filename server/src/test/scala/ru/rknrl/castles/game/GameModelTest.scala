package ru.rknrl.castles.game

import org.scalatest.{FlatSpec, Matchers}
import ru.rknrl.castles.AccountId
import ru.rknrl.castles.account.objects.items.ItemsTest
import ru.rknrl.castles.account.objects.skills.SkillsTest
import ru.rknrl.castles.account.objects.startLocation.StartLocationTest
import ru.rknrl.castles.game.objects.buildings.BuildingId
import ru.rknrl.castles.game.objects.players.{Player, PlayerId}
import ru.rknrl.castles.game.objects.units.UnitId
import ru.rknrl.dto.AuthDTO.AccountType

class GameModelTest extends FlatSpec with Matchers {

  // PlayerId

  "PlayerId.dto" should "be correct" in {
    val dto = new PlayerId(124).dto
    dto.getId should be(124)
  }

  "PlayerId.equals" should "be false with other types" in {
    (new PlayerId(124) == 124) should be(false)
  }

  "PlayerId.equals" should "be true with same playerId" in {
    (new PlayerId(124) == new PlayerId(124)) should be(true)
  }

  "PlayerId.equals" should "be true with different playerId" in {
    (new PlayerId(124) == new PlayerId(12)) should be(false)
  }

  "PlayerId" should "have correct hash" in {
    Map[PlayerId, String]()
      .updated(new PlayerId(124), "a")
      .updated(new PlayerId(124), "b")
      .apply(new PlayerId(124)) should be("b")

    Map[PlayerId, String]()
      .updated(new PlayerId(0), "a")
      .updated(new PlayerId(1), "b")
      .apply(new PlayerId(0)) should be("a")
  }

  // Player

  "Player.dto" should "be correct" in {
    val player = new Player(
      new PlayerId(7),
      new AccountId(AccountType.VKONTAKTE, "accountId"),
      StartLocationTest.startLocation1,
      SkillsTest.skills,
      ItemsTest.items,
      isBot = false
    )

    player.dto.getId.getId should be(7)
  }

  // BuildingId

  "BuildingId.dto" should "be correct" in {
    val dto = new BuildingId(124).dto
    dto.getId should be(124)
  }

  "BuildingId.equals" should "be false with other types" in {
    (new BuildingId(124) == 124) should be(false)
  }

  "BuildingId.equals" should "be true with same BuildingId" in {
    (new BuildingId(124) == new BuildingId(124)) should be(true)
  }

  "BuildingId.equals" should "be true with different BuildingId" in {
    (new BuildingId(124) == new BuildingId(12)) should be(false)
  }

  "BuildingId" should "have correct hash" in {
    Map[BuildingId, String]()
      .updated(new BuildingId(124), "a")
      .updated(new BuildingId(124), "b")
      .apply(new BuildingId(124)) should be("b")

    Map[BuildingId, String]()
      .updated(new BuildingId(0), "a")
      .updated(new BuildingId(1), "b")
      .apply(new BuildingId(0)) should be("a")
  }

  // UnitId

  "UnitId.dto" should "be correct" in {
    val dto = new UnitId(124).dto
    dto.getId should be(124)
  }

  "UnitId.equals" should "be false with other types" in {
    (new UnitId(124) == 124) should be(false)
  }

  "UnitId.equals" should "be true with same UnitId" in {
    (new UnitId(124) == new UnitId(124)) should be(true)
  }

  "UnitId.equals" should "be true with different UnitId" in {
    (new UnitId(124) == new UnitId(12)) should be(false)
  }

  "UnitId" should "have correct hash" in {
    Map[UnitId, String]()
      .updated(new UnitId(124), "a")
      .updated(new UnitId(124), "b")
      .apply(new UnitId(124)) should be("b")

    Map[UnitId, String]()
      .updated(new UnitId(0), "a")
      .updated(new UnitId(1), "b")
      .apply(new UnitId(0)) should be("a")
  }
}
