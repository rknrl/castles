package ru.rknrl.castles.controllerMock {
import flash.events.Event;
import flash.utils.setTimeout;

import ru.rknrl.castles.model.events.BuildEvent;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.events.RemoveBuildingEvent;
import ru.rknrl.castles.model.events.SlotClickEvent;
import ru.rknrl.castles.model.events.SlotSwapEvent;
import ru.rknrl.castles.model.events.UpgradeBuildingEvent;
import ru.rknrl.castles.model.events.UpgradeClickEvent;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.menu.bank.Products;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.castles.model.menu.shop.ItemsCount;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.model.menu.top.Top;
import ru.rknrl.castles.view.View;
import ru.rknrl.dto.AuthenticationSuccessDTO;
import ru.rknrl.dto.ItemDTO;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SlotId;

public class ControllerMock {
    private var view:View;

    public function ControllerMock(view:View) {
        this.view = view;

        view.addEventListener(SlotClickEvent.SLOT_CLICK, onSlotClick);
        view.addEventListener(SlotSwapEvent.SLOT_SWAP, onSlotSwap);

        view.addEventListener(BuildEvent.BUILD, onBuild);
        view.addEventListener(UpgradeBuildingEvent.UPGRADE_BUILDING, onUpgradeBuilding);
        view.addEventListener(RemoveBuildingEvent.REMOVE_BUILDING, onRemoveBuilding);

        view.addEventListener(MagicItemClickEvent.MAGIC_ITEM_CLICK, onMagicItemClick);
        view.addEventListener(UpgradeClickEvent.UPGRADE_CLICK, onUpgradeClick);

        view.addEventListener(ViewEvents.PLAY, onPlay);
        view.addEventListener(ViewEvents.BUY, onBuy);

        view.addLoadingScreen();
        setTimeout(addMenu, 2000)
    }

    private var dto:AuthenticationSuccessDTO;

    private function addMenu():void {
        dto = DtoMock.authenticationSuccess();

        const startLocation:StartLocation = new StartLocation(dto.accountState.startLocation);

        const gold:int = dto.accountState.gold;

        const top:Top = new Top(dto.config.top);

        const itemsCount:ItemsCount = new ItemsCount(dto.accountState.items);

        const itemPrice:int = dto.config.itemPrice;

        const skillLevels:SkillLevels = new SkillLevels(dto.accountState.skills);

        const upgradePrices:SkillUpgradePrices = new SkillUpgradePrices(dto.config.skillUpgradePrices);

        const products:Products = new Products(dto.products);

        view.addMenu(startLocation, gold, top, itemsCount, itemPrice, skillLevels, upgradePrices, products)
    }

    private function onPlay(event:Event):void {
        view.addSearchOpponentScreen();

        setTimeout(function () {
            view.addGame();
            view.addEventListener(GameViewEvents.SURRENDER, onSurrender);
        }, 2000)
    }

    private function onBuy(event:Event):void {
        dto.accountState.gold += 20;
        view.gold = dto.accountState.gold;
        lock();
    }

    private function onUpgradeClick(event:UpgradeClickEvent):void {
        const skillLevel:SkillLevelDTO = DtoMock.findSkillLevel(dto.accountState.skills, event.skillType);
        skillLevel.level = SkillLevel.SKILL_LEVEL_3;
        view.skillLevels = new SkillLevels(dto.accountState.skills);
        lock();
    }

    private function onMagicItemClick(event:MagicItemClickEvent):void {
        const item:ItemDTO = DtoMock.findItem(dto.accountState.items, event.itemType);
        item.count++;
        view.itemsCount = new ItemsCount(dto.accountState.items);
        lock();
    }

    private function onSlotClick(event:SlotClickEvent):void {
        if (event.slotId == SlotId.SLOT_1) {
            view.openBuildPopup(event.slotId, 4);
        } else {
            view.openUpgradePopup(event.slotId, true, true, 4);
        }
    }

    private function onBuild(event:BuildEvent):void {
        view.startLocation = new StartLocation(dto.accountState.startLocation);
        lock();
    }

    private function onUpgradeBuilding(event:UpgradeBuildingEvent):void {
        view.startLocation = new StartLocation(dto.accountState.startLocation);
        lock();
    }

    private function onRemoveBuilding(event:RemoveBuildingEvent):void {
        view.startLocation = new StartLocation(dto.accountState.startLocation);
        lock();
    }

    private function onSlotSwap(event:SlotSwapEvent):void {
        view.startLocation = new StartLocation(dto.accountState.startLocation);
        lock();
    }

    private function onSurrender(event:Event):void {
        view.removeEventListener(GameViewEvents.SURRENDER, onSurrender);
        view.removeGame();
    }

    private function lock():void {
        view.lock = true;
        setTimeout(function () {
            view.lock = false
        }, 1000);
    }
}
}
