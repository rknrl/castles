//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.account.AccountState
import ru.rknrl.castles.account.AccountState.{Skills, Slots}
import ru.rknrl.castles.account.state.AccountMock.{accountState, config}
import ru.rknrl.core.social.Product
import ru.rknrl.dto.BuildingLevel._
import ru.rknrl.dto.BuildingPrototype
import ru.rknrl.dto.BuildingType._
import ru.rknrl.dto.ItemType.{FIREBALL, TORNADO, VOLCANO}
import ru.rknrl.dto.SkillLevel._
import ru.rknrl.dto.SkillType._
import ru.rknrl.dto.SlotId._

class AccountStateTest extends WordSpec with Matchers {

  val GOLD = "gold"
  val RATING = "rating"
  val GAMES_COUNT = "gamesCount"

  def checkEquals(a: AccountState, b: AccountState, without: Set[Any]) = {
    for ((slotId, buildingPrototype) ← a.slots if !(without contains slotId))
      a.slots(slotId) shouldBe b.slots(slotId)

    for ((skillType, skillLevel) ← a.skills if !(without contains skillType))
      a.skills(skillType) shouldBe b.skills(skillType)

    for ((itemType, count) ← a.items if !(without contains itemType))
      a.items(itemType) shouldBe b.items(itemType)

    if (!(without contains GOLD)) a.gold shouldBe b.gold
    if (!(without contains RATING)) a.rating shouldBe b.rating
    if (!(without contains GAMES_COUNT)) a.gamesCount shouldBe b.gamesCount
  }

  "buyBuilding" should {
    "Если хватает денег и слот пустой" +
      "строим нужный домик" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountState(gold = 4)
      val state = oldState.buyBuilding(SLOT_1, TOWER, config())
      state.gold shouldBe 0
      state.slots(SLOT_1) shouldBe Some(BuildingPrototype(TOWER, LEVEL_1))
      checkEquals(oldState, state, Set(SLOT_1, GOLD))

    }

    "Если слот не пустой - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState(gold = 4).buyBuilding(SLOT_3, TOWER, config())
      }

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState(gold = 3).buyBuilding(SLOT_1, TOWER, config())
      }

    }
  }

  "upgradeBuilding" should {
    "Если хватает денег и слот не пустой и не последнего уровня" +
      "апгрейдим нужный домик" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountState(gold = 16)
      val state = oldState.upgradeBuilding(SLOT_3, config())
      state.gold shouldBe 0
      state.slots(SLOT_3) shouldBe Some(BuildingPrototype(HOUSE, LEVEL_2))
      checkEquals(oldState, state, Set(SLOT_3, GOLD))

    }

    "Если слот пустой - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState(gold = 16).upgradeBuilding(SLOT_1, config())
      }

    }

    "Если слот последнего уровня - кидаем эксепшн" in {

      val slots: Slots =
        Map(
          SLOT_1 → None,
          SLOT_2 → None,
          SLOT_3 → Some(BuildingPrototype(HOUSE, LEVEL_3)),
          SLOT_4 → Some(BuildingPrototype(TOWER, LEVEL_1)),
          SLOT_5 → Some(BuildingPrototype(CHURCH, LEVEL_1))
        )

      a[Exception] shouldBe thrownBy {
        accountState(gold = 16, slots = slots).upgradeBuilding(SLOT_3, config())
      }

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState(gold = 14).upgradeBuilding(SLOT_3, config())
      }

    }
  }

  "setBuilding" should {
    "Изменяем нужный домик и не меняем ничего другого" in {

      val slots: Slots =
        Map(
          SLOT_1 → None,
          SLOT_2 → None,
          SLOT_3 → Some(BuildingPrototype(HOUSE, LEVEL_3)),
          SLOT_4 → Some(BuildingPrototype(TOWER, LEVEL_1)),
          SLOT_5 → Some(BuildingPrototype(CHURCH, LEVEL_1))
        )

      val oldState = accountState(slots = slots, gold = 0)
      val state = oldState.setBuilding(SLOT_3, BuildingPrototype(TOWER, LEVEL_1))
      state.slots(SLOT_3) shouldBe Some(BuildingPrototype(TOWER, LEVEL_1))
      checkEquals(oldState, state, Set(SLOT_3))

    }
  }

  "removeBuilding" should {
    "Если слот не пустой - удаляем нужный домик и не меняем ничего другого" in {

      val oldState = accountState(gold = 0)
      val state = oldState.removeBuilding(SLOT_3)
      state.slots(SLOT_3) shouldBe empty
      checkEquals(oldState, state, Set(SLOT_3))

    }

    "Если слот пустой - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState(gold = 0).removeBuilding(SLOT_1)
      }

    }
  }

  "upgradeSkill" should {
    "Если хватает денег и скилл не последнего уровня" +
      "апгейдим нужный скилл" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountState(gold = 1)
      val state = oldState.upgradeSkill(ATTACK, config())
      state.skills(ATTACK) shouldBe SKILL_LEVEL_1
      state.gold shouldBe 0
      checkEquals(oldState, state, Set(ATTACK, GOLD))

    }

    "Если слот последнего уровня - кидаем эксепшн" in {

      val skills: Skills = Map(
        ATTACK → SKILL_LEVEL_3,
        DEFENCE → SKILL_LEVEL_1,
        SPEED → SKILL_LEVEL_1
      )

      a[Exception] shouldBe thrownBy {
        accountState(gold = 1, skills = skills).upgradeSkill(ATTACK, config())
      }

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState(gold = 0).upgradeSkill(ATTACK, config())
      }

    }
  }

  "setSkill" should {
    "Изменяем нужный скилл и не меняем ничего другого" in {

      val skills: Skills = Map(
        ATTACK → SKILL_LEVEL_3,
        DEFENCE → SKILL_LEVEL_1,
        SPEED → SKILL_LEVEL_1
      )

      val oldState = accountState(gold = 1, skills = skills)
      val state = oldState.setSkill(ATTACK, SKILL_LEVEL_2)
      state.skills(ATTACK) shouldBe SKILL_LEVEL_2
      checkEquals(oldState, state, Set(ATTACK))

    }
  }

  "buyItem" should {
    "Если хватает денег" +
      "увеличиваем нужный предмет на 1" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountState(gold = 1)
      val state = oldState.buyItem(FIREBALL, config())
      state.items(FIREBALL) shouldBe 5
      state.gold shouldBe 0
      checkEquals(oldState, state, Set(FIREBALL, GOLD))

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState(gold = 0).buyItem(FIREBALL, config())
      }

    }
  }

  "addItem" should {
    "Увеличиваем нужный предмет и не меняем ничего другого" in {

      val oldState = accountState(gold = 0)
      val state = oldState.addItem(VOLCANO, 3)
      state.items(VOLCANO) shouldBe 7
      checkEquals(oldState, state, Set(VOLCANO))

    }

    "Уменьшаем нужый предмет и не меняем ничего другого" in {

      val oldState = accountState(gold = 0)
      val state = oldState.addItem(VOLCANO, -3)
      state.items(VOLCANO) shouldBe 1
      checkEquals(oldState, state, Set(VOLCANO))

    }

    "Не можем уйти в минус" in {

      val oldState = accountState(gold = 0)
      val state = oldState.addItem(TORNADO, -999)
      state.items(TORNADO) shouldBe 0
      checkEquals(oldState, state, Set(TORNADO))

    }
  }

  "addGold" should {
    "Увеличиваем деньги на нужную сумму и не меняем ничего другого" in {

      val oldState = accountState(gold = 2)
      val state = oldState.addGold(3)
      state.gold shouldBe 5
      checkEquals(oldState, state, Set(GOLD))

    }

    "Уменьшаем деньги на нужную сумму и не меняем ничего другого" in {

      val oldState = accountState(gold = 9)
      val state = oldState.addGold(-4)
      state.gold shouldBe 5
      checkEquals(oldState, state, Set(GOLD))

    }

    "Не можем уйти в минус" in {

      val oldState = accountState(gold = 9)
      val state = oldState.addGold(-99)
      state.gold shouldBe 0
      checkEquals(oldState, state, Set(GOLD))

    }
  }

  "incGamesCount" should {
    "Увеличиваем gamesCount на 1 и не меняем ничего другого" in {

      val oldState = accountState(gamesCount = 0)
      val state = oldState.incGamesCount
      state.gamesCount shouldBe 1
      checkEquals(oldState, state, Set(GAMES_COUNT))

    }
  }

  "setNewRating" should {
    "Устанавливаем новый рейтинг и не меняем ничего другого" in {

      val oldState = accountState(rating = 1400)
      val state = oldState.setNewRating(1489)
      state.rating shouldBe 1489
      checkEquals(oldState, state, Set(RATING))

    }
  }

  "applyProduct" should {
    "если STARS увеличиваем деньги на нужную сумму и не меняем ничего другого" in {

      val oldState = accountState()
      val state = oldState.applyProduct(new Product(id = 1, title = "", description = "", photoUrl = ""), 1)
      state.gold shouldBe 11

      val oldState2 = accountState()
      val state2 = oldState2.applyProduct(new Product(id = 1, title = "", description = "", photoUrl = ""), 100)
      state2.gold shouldBe 110

      checkEquals(oldState, state, Set(GOLD))

    }

    "если не STARS - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        accountState().applyProduct(new Product(id = 3, title = "", description = "", photoUrl = ""), 1)
      }

    }
  }
}