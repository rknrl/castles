//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller {
import flash.events.Event;
import flash.utils.Dictionary;

import ru.rknrl.asocial.PaymentDialogData;
import ru.rknrl.asocial.PaymentDialogEvent;
import ru.rknrl.asocial.ISocial;
import ru.rknrl.castles.model.events.BuildEvent;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.events.RemoveBuildingEvent;
import ru.rknrl.castles.model.events.ScreenChangedEvent;
import ru.rknrl.castles.model.events.SlotClickEvent;
import ru.rknrl.castles.model.events.UpgradeBuildingEvent;
import ru.rknrl.castles.model.events.UpgradeClickEvent;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.view.menu.MenuView;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuyBuildingDTO;
import ru.rknrl.dto.BuyItemDTO;
import ru.rknrl.dto.ProductDTO;
import ru.rknrl.dto.RemoveBuildingDTO;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.TutorStateDTO;
import ru.rknrl.dto.UpgradeBuildingDTO;
import ru.rknrl.dto.UpgradeSkillDTO;
import ru.rknrl.rmi.AccountStateUpdatedEvent;
import ru.rknrl.rmi.Server;

public class MenuController {
    private var view:MenuView;
    private var server:Server;
    private var model:MenuModel;
    private var social:ISocial;
    private var tutorState:TutorStateDTO;

    /** Показывался ли уже туториал для этого экрана за текущий запуск приложения */
    private const tutorShows:Dictionary = initTutorShows();

    private static function initTutorShows():Dictionary {
        const result:Dictionary = new Dictionary();
        for each(var screenIndex:int in ScreenChangedEvent.ALL) {
            result[screenIndex] = false;
        }
        return result;
    }

    public function MenuController(view:MenuView, server:Server, model:MenuModel, social:ISocial, tutorState:TutorStateDTO) {
        this.view = view;
        this.server = server;
        this.model = model;
        this.social = social;

        this.tutorState = tutorState;

        server.addEventListener(AccountStateUpdatedEvent.ACCOUNTSTATEUPDATED, onAccountStateUpdated);

        social.addEventListener(PaymentDialogEvent.PAYMENT_DIALOG_CLOSED, onPaymentDialogClosed);
        social.addEventListener(PaymentDialogEvent.PAYMENT_SUCCESS, onPaymentSuccess);
        social.addEventListener(PaymentDialogEvent.PAYMENT_FAIL, onPaymentFail);

        view.addEventListener(SlotClickEvent.SLOT_CLICK, onSlotClick);

        view.addEventListener(BuildEvent.BUILD, onBuild);
        view.addEventListener(UpgradeBuildingEvent.UPGRADE_BUILDING, onUpgradeBuilding);
        view.addEventListener(RemoveBuildingEvent.REMOVE_BUILDING, onRemoveBuilding);

        view.addEventListener(MagicItemClickEvent.MAGIC_ITEM_CLICK, onMagicItemClick);
        view.addEventListener(UpgradeClickEvent.UPGRADE_CLICK, onUpgradeClick);

        view.addEventListener(ViewEvents.BUY, onBuy);
        view.addEventListener(ScreenChangedEvent.SCREEN_CHANGED, onScreenChanged);
    }

    private function onAccountStateUpdated(event:AccountStateUpdatedEvent):void {
        model.mergeAccountStateDto(event.accountState);
        view.slots = model.slots;
        view.gold = model.gold;
        view.itemsCount = model.itemsCount;
        view.skillLevels = model.skillLevels;
        view.lock = false;
    }

    private function onSlotClick(event:SlotClickEvent):void {
        const slot:SlotDTO = model.slots.getSlot(event.slotId);
        if (slot.hasBuildingPrototype) {
            const canUpgrade:Boolean = slot.buildingPrototype.level != BuildingLevel.LEVEL_3;
            const canRemove:Boolean = model.slots.buildingsCount > 1;
            if (canUpgrade) {
                const nextLevel:BuildingLevel = getNextLevel(slot.buildingPrototype.level);
                const upgradePrice:int = model.buildingPrices.getPrice(nextLevel);
            }
            if (canUpgrade || canRemove) {
                if (!tutorState.slot) {
                    tutorState.slot = true;
                    server.updateTutorState(tutorState);
                }

                view.openUpgradePopup(event.slotId, slot.buildingPrototype.type, canUpgrade, canRemove, upgradePrice);
            }

        } else {
            if (!tutorState.emptySlot) {
                tutorState.emptySlot = true;
                server.updateTutorState(tutorState);
            }

            view.openBuildPopup(event.slotId, model.buildingPrices.buildPrice);
        }
    }

    private function onBuild(event:BuildEvent):void {
        if (model.gold < model.buildingPrices.buildPrice) {
            view.animatePrice();
        } else {
            const dto:BuyBuildingDTO = new BuyBuildingDTO();
            dto.id = event.slotId;
            dto.buildingType = event.buildingType;
            server.buyBuilding(dto);

            view.closePopup();
            view.lock = true;
        }
    }

    private function onUpgradeBuilding(event:UpgradeBuildingEvent):void {
        const slot:SlotDTO = model.slots.getSlot(event.slotId);
        if (!slot.hasBuildingPrototype) throw new Error();
        const nextLevel:BuildingLevel = getNextLevel(slot.buildingPrototype.level);
        const price:int = model.buildingPrices.getPrice(nextLevel);

        if (model.gold < price) {
            view.animatePrice();
        } else {
            const dto:UpgradeBuildingDTO = new UpgradeBuildingDTO();
            dto.id = event.slotId;
            server.upgradeBuilding(dto);

            view.closePopup();
            view.lock = true;
        }
    }

    private static function getNextLevel(buildingLevel:BuildingLevel):BuildingLevel {
        switch (buildingLevel) {
            case BuildingLevel.LEVEL_1:
                return BuildingLevel.LEVEL_2;
            case BuildingLevel.LEVEL_2:
                return BuildingLevel.LEVEL_3;
        }
        throw new Error(buildingLevel + " hasn't next level");
    }

    private function onRemoveBuilding(event:RemoveBuildingEvent):void {
        const dto:RemoveBuildingDTO = new RemoveBuildingDTO();
        dto.id = event.slotId;
        server.removeBuilding(dto);

        view.closePopup();
        view.lock = true;
    }

    private function onMagicItemClick(event:MagicItemClickEvent):void {
        if (model.gold < model.itemPrice) {
            view.animatePrice();
        } else {
            if (!tutorState.magicItem) {
                tutorState.magicItem = true;
                server.updateTutorState(tutorState);
            }

            const dto:BuyItemDTO = new BuyItemDTO();
            dto.type = event.itemType;
            server.buyItem(dto);

            view.animateMagicItem(event.itemType);
            view.lock = true;
        }
    }

    private function onUpgradeClick(event:UpgradeClickEvent):void {
        if (model.skillLevels.getLevel(event.skillType) != SkillLevel.SKILL_LEVEL_3) {
            if (model.gold < model.upgradePrices.getPrice(model.skillLevels.totalLevel + 1)) {
                view.animatePrice();
            } else {
                if (!tutorState.skills) {
                    tutorState.skills = true;
                    server.updateTutorState(tutorState);
                }

                const dto:UpgradeSkillDTO = new UpgradeSkillDTO();
                dto.type = event.skillType;
                server.upgradeSkill(dto);

                view.animateFlask(event.skillType);
                view.lock = true;
            }
        }
    }

    // payment

    private function onBuy(event:Event):void {
        const product:ProductDTO = model.products.product;
        social.showPaymentDialog(new PaymentDialogData(product.id, product.title, product.description, product.price));
        // todo temp view.lock = true;
    }

    private function onPaymentDialogClosed(event:PaymentDialogEvent):void {
        trace("payment closed");
        view.lock = false;
    }

    private function onPaymentSuccess(event:PaymentDialogEvent):void {
        trace("payment success");
        view.lock = false;
    }

    private function onPaymentFail(event:PaymentDialogEvent):void {
        trace("payment fail");
        view.lock = false;
    }

    private function onScreenChanged(event:ScreenChangedEvent):void {
        const screenIndex:int = event.screenIndex;
        if (screenIndex > 0) {
            if (!tutorState.navigate) {
                tutorState.navigate = true;
                server.updateTutorState(tutorState);
            }
        }

        if (tutorState.appRunCount >= 3 && !tutorShows[screenIndex] && !view.tutorPlaying) {
            switch (screenIndex) {
                case ScreenChangedEvent.SCREEN_MAIN:
                    if (!tutorState.navigate) {
                        tutorShows[screenIndex] = true;
                        view.playSwipeTutor();
                    } else if (!tutorState.slot && model.gold >= model.buildingPrices.getPrice(BuildingLevel.LEVEL_2)) {
                        tutorShows[screenIndex] = true;
                        view.playSlotTutor(model.slots.getNotEmptySlot());
                    } else if (!tutorState.emptySlot && model.slots.getEmptySlot() && model.gold >= model.buildingPrices.buildPrice) {
                        tutorShows[screenIndex] = true;
                        view.playSlotTutor(model.slots.getEmptySlot());
                    }
                    break;
                case ScreenChangedEvent.SCREEN_SHOP:
                    if (!tutorState.magicItem && tutorState.appRunCount >= 4 && model.gold >= model.itemPrice) {
                        tutorShows[screenIndex] = true;
                        view.playMagicItemTutor();
                    }
                    break;
                case ScreenChangedEvent.SCREEN_SKILLS:
                    if (!tutorState.skills && tutorState.appRunCount >= 4 && model.gold >= model.upgradePrices.firstUpgradePrice) {
                        tutorShows[screenIndex] = true;
                        view.playFlaskTutor();
                    }
                    break;
            }
        }
    }
}
}
