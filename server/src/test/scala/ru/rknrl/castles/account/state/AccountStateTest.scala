//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.account.state

import org.scalatest.{Matchers, WordSpec}
import ru.rknrl.castles.account.AccountState._
import ru.rknrl.castles.database.DatabaseTransaction.FakeCalendar
import ru.rknrl.castles.kit.Mocks._
import ru.rknrl.core.social.Product
import ru.rknrl.dto.BuildingLevel._
import ru.rknrl.dto.BuildingType._
import ru.rknrl.dto.ItemType._
import ru.rknrl.dto.SkillLevel._
import ru.rknrl.dto.SkillType._
import ru.rknrl.dto.SlotId._
import ru.rknrl.dto.{AccountStateDTO, BuildingPrototype, SkillLevelDTO, SlotDTO}

class AccountStateTest extends WordSpec with Matchers {

  val GOLD = "gold"
  val GAMES_COUNT = "gamesCount"

  def checkEquals(a: AccountStateDTO, b: AccountStateDTO, without: Set[Any]) = {
    for (slot ← a.slots if !(without contains slot.id))
      slot shouldBe b.slots.find(_.id == slot.id).get

    for (skill ← a.skills if !(without contains skill.skillType))
      skill shouldBe b.skills.find(_.skillType == skill.skillType).get

    for (item ← a.items if !(without contains item.itemType))
      item shouldBe b.items.find(_.itemType == item.itemType).get

    if (!(without contains GOLD)) a.gold shouldBe b.gold
    if (!(without contains GAMES_COUNT)) a.gamesCount shouldBe b.gamesCount
  }

  "buyBuilding" should {
    "Если хватает денег и слот пустой" +
      "строим нужный домик" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 4)
      val state = buyBuilding(Some(oldState), SLOT_1, TOWER, accountConfigMock())
      state.gold shouldBe 0
      state.slots.find(_.id == SLOT_1).get.buildingPrototype shouldBe Some(BuildingPrototype(TOWER, LEVEL_1))
      checkEquals(oldState, state, Set(SLOT_1, GOLD))

    }

    "Если слот не пустой - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        buyBuilding(Some(accountStateMock(gold = 4)), SLOT_3, TOWER, accountConfigMock())
      }

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        buyBuilding(Some(accountStateMock(gold = 3)), SLOT_1, TOWER, accountConfigMock())
      }

    }
  }

  "upgradeBuilding" should {
    "Если хватает денег и слот не пустой и не последнего уровня" +
      "апгрейдим нужный домик" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 16)
      val state = upgradeBuilding(Some(oldState), SLOT_3, accountConfigMock())
      state.gold shouldBe 0
      state.slots.find(_.id == SLOT_3).get.buildingPrototype shouldBe Some(BuildingPrototype(HOUSE, LEVEL_2))
      checkEquals(oldState, state, Set(SLOT_3, GOLD))

    }

    "Если слот пустой - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        upgradeBuilding(Some(accountStateMock(gold = 16)), SLOT_1, accountConfigMock())
      }

    }

    "Если слот последнего уровня - кидаем эксепшн" in {

      val slots =
        List(
          SlotDTO(SLOT_1, None),
          SlotDTO(SLOT_2, None),
          SlotDTO(SLOT_3, Some(BuildingPrototype(HOUSE, LEVEL_3))),
          SlotDTO(SLOT_4, Some(BuildingPrototype(TOWER, LEVEL_1))),
          SlotDTO(SLOT_5, Some(BuildingPrototype(CHURCH, LEVEL_1)))
        )

      a[Exception] shouldBe thrownBy {
        upgradeBuilding(Some(accountStateMock(gold = 16, slots = slots)), SLOT_3, accountConfigMock())
      }

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        upgradeBuilding(Some(accountStateMock(gold = 14)), SLOT_3, accountConfigMock())
      }

    }
  }

  "removeBuilding" should {
    "Если слот не пустой - удаляем нужный домик и не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 0)
      val state = removeBuilding(Some(oldState), SLOT_3, accountConfigMock())
      state.slots.find(_.id == SLOT_3).get.buildingPrototype shouldBe empty
      checkEquals(oldState, state, Set(SLOT_3))

    }

    "Если слот пустой - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        removeBuilding(Some(accountStateMock(gold = 0)), SLOT_1, accountConfigMock())
      }

    }
  }

  "upgradeSkill" should {
    "Если хватает денег и скилл не последнего уровня" +
      "апгейдим нужный скилл" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 1)
      val state = upgradeSkill(Some(oldState), ATTACK, accountConfigMock())
      state.skills.find(_.skillType == ATTACK).get.level shouldBe SKILL_LEVEL_1
      state.gold shouldBe 0
      checkEquals(oldState, state, Set(ATTACK, GOLD))

    }

    "Если слот последнего уровня - кидаем эксепшн" in {

      val skills = List(
        SkillLevelDTO(ATTACK, SKILL_LEVEL_3),
        SkillLevelDTO(DEFENCE, SKILL_LEVEL_1),
        SkillLevelDTO(SPEED, SKILL_LEVEL_1)
      )

      a[Exception] shouldBe thrownBy {
        upgradeSkill(Some(accountStateMock(gold = 1, skills = skills)), ATTACK, accountConfigMock())
      }

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        upgradeSkill(Some(accountStateMock(gold = 0)), ATTACK, accountConfigMock())
      }

    }
  }

  "buyItem" should {
    "Если хватает денег" +
      "увеличиваем нужный предмет на 1" +
      "уменьшаем деньги на нужную сумму" +
      "не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 1)
      val state = buyItem(Some(oldState), FIREBALL, accountConfigMock())
      state.items.find(_.itemType == FIREBALL).get.count shouldBe 5
      state.gold shouldBe 0
      checkEquals(oldState, state, Set(FIREBALL, GOLD))

    }

    "Если не хватает денег - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        buyItem(Some(accountStateMock(gold = 0)), FIREBALL, accountConfigMock())
      }

    }
  }

  "addItem" should {
    "Увеличиваем нужный предмет и не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 0)
      val state = addItem(oldState, VOLCANO, 3)
      state.items.find(_.itemType == VOLCANO).get.count shouldBe 7
      checkEquals(oldState, state, Set(VOLCANO))

    }

    "Уменьшаем нужый предмет и не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 0)
      val state = addItem(oldState, VOLCANO, -3)
      state.items.find(_.itemType == VOLCANO).get.count shouldBe 1
      checkEquals(oldState, state, Set(VOLCANO))

    }

    "Не можем уйти в минус" in {

      val oldState = accountStateMock(gold = 0)
      val state = addItem(oldState, TORNADO, -999)
      state.items.find(_.itemType == TORNADO).get.count shouldBe 0
      checkEquals(oldState, state, Set(TORNADO))

    }
  }

  "addGold" should {
    "Увеличиваем деньги на нужную сумму и не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 2)
      val state = addGold(oldState, 3)
      state.gold shouldBe 5
      checkEquals(oldState, state, Set(GOLD))

    }

    "Уменьшаем деньги на нужную сумму и не меняем ничего другого" in {

      val oldState = accountStateMock(gold = 9)
      val state = addGold(oldState, -4)
      state.gold shouldBe 5
      checkEquals(oldState, state, Set(GOLD))

    }

    "Не можем уйти в минус" in {

      val oldState = accountStateMock(gold = 9)
      val state = addGold(oldState, -99)
      state.gold shouldBe 0
      checkEquals(oldState, state, Set(GOLD))

    }
  }

  "incGamesCount" should {
    "Увеличиваем gamesCount на 1 и не меняем ничего другого" in {

      val oldState = accountStateMock(gamesCount = 0)
      val state = incGamesCount(oldState)
      state.gamesCount shouldBe 1
      checkEquals(oldState, state, Set(GAMES_COUNT))

    }
  }

  "applyProduct" should {
    "если STARS увеличиваем деньги на нужную сумму и не меняем ничего другого" in {

      val oldState = accountStateMock()
      val state = applyProduct(oldState, new Product(id = 1, title = "", description = "", photoUrl = ""), 1)
      state.gold shouldBe 11

      val oldState2 = accountStateMock()
      val state2 = applyProduct(oldState2, new Product(id = 1, title = "", description = "", photoUrl = ""), 100)
      state2.gold shouldBe 110

      checkEquals(oldState, state, Set(GOLD))

    }

    "если не STARS - кидаем эксепшн" in {

      a[Exception] shouldBe thrownBy {
        applyProduct(accountStateMock(), new Product(id = 3, title = "", description = "", photoUrl = ""), 1)
      }

    }
  }

  "applyUsedItems" in {
    val oldState = accountStateMock()
    val state = applyUsedItems(oldState, Map(
      FIREBALL → 1,
      TORNADO → 2,
      STRENGTHENING → 3
    ))

    state.items.find(_.itemType == FIREBALL).get.count shouldBe 3
    state.items.find(_.itemType == STRENGTHENING).get.count shouldBe 1
    state.items.find(_.itemType == VOLCANO).get.count shouldBe 4
    state.items.find(_.itemType == TORNADO).get.count shouldBe 2
    state.items.find(_.itemType == ASSISTANCE).get.count shouldBe 4

    checkEquals(oldState, state, Set(FIREBALL, TORNADO, STRENGTHENING))
  }

  "acceptPresent" should {
    "Если прошло достаточное время то " +
      "начисляем деньги и " +
      "изменяем lastPresentTime" in {
      val state = accountStateMock(
        gold = 11,
        lastPresentTime = Some(1000)
      )
      val config = accountConfigMock(
        presentGold = 20,
        presentInterval = 2000
      )
      val newState = acceptPresent(Some(state), config, new FakeCalendar(week = 3, millis = 4000))
      newState shouldBe state.copy(gold = 31, lastPresentTime = Some(4000))
    }

    "Если времени НЕ достаточно то ничего не делаем" in {
      val state = accountStateMock(
        lastPresentTime = Some(3000)
      )
      val config = accountConfigMock(
        presentInterval = 2000
      )
      val newState = acceptPresent(Some(state), config, new FakeCalendar(week = 3, millis = 4000))
      newState shouldBe state
    }
  }

  "acceptAdvert" should {
    "Если не приняли " +
      "изменяем lastGamesCountAdvert" in {
      val state = accountStateMock(
        gold = 11,
        gamesCount = 5,
        lastGamesCountAdvert = Some(3)
      )
      val config = accountConfigMock(
        advertGold = 20,
        advertGamesInterval = 2
      )
      val newState = acceptAdvert(Some(state), false, config)
      newState shouldBe state.copy(lastGamesCountAdvert = Some(5))
    }

    "Если приняли и прошло достаточное кол-во игр " +
      "начисляем деньги и " +
      "изменяем lastGamesCountAdvert" in {
      val state = accountStateMock(
        gold = 11,
        gamesCount = 5,
        lastGamesCountAdvert = Some(3)
      )
      val config = accountConfigMock(
        advertGold = 20,
        advertGamesInterval = 2
      )
      val newState = acceptAdvert(Some(state), true, config)
      newState shouldBe state.copy(gold = 31, lastGamesCountAdvert = Some(5))
    }

    "Если прошло кол-во игр НЕ достаточно то ничего не делаем" in {
      val state = accountStateMock(
        gamesCount = 5,
        lastGamesCountAdvert = Some(4)
      )
      val config = accountConfigMock(
        advertGamesInterval = 2
      )
      val newState = acceptAdvert(Some(state), true, config)
      newState shouldBe state
    }
  }

  "acceptWeekTop" should {
    "Если номер недели больше предыдущего " +
      "то изменяем его" in {
      val state = accountStateMock(
        weekNumberAccepted = Some(1)
      )
      val config = accountConfigMock()
      val newState = acceptWeekTop(Some(state), config, 2)
      newState shouldBe state.copy(weekNumberAccepted = Some(2))
    }

    "Если номер неделе НЕ больше предудущего - ничего не делаем" in {
      val state = accountStateMock(
        weekNumberAccepted = Some(10)
      )
      val config = accountConfigMock()
      val newState = acceptWeekTop(Some(state), config, 9)
      newState shouldBe state
    }
  }
}
