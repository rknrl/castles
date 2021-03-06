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
import flash.utils.setTimeout;

import protos.AcceptAdvert;
import protos.AccountStateEvent;
import protos.BuildingLevel;
import protos.PlaceEvent;
import protos.Product;
import protos.SkillLevel;
import protos.Slot;
import protos.StatAction;
import protos.TopEvent;

import ru.rknrl.asocial.ISocial;
import ru.rknrl.asocial.PaymentDialogData;
import ru.rknrl.asocial.PaymentDialogEvent;
import ru.rknrl.asocial.VideoAdvertEvent;
import ru.rknrl.castles.model.MutableTutorState;
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
import ru.rknrl.protobuf.Bool;

public class MenuController {
    private var view:MenuView;
    private var server:Server;
    private var model:MenuModel;
    private var social:ISocial;
    private var tutorState:MutableTutorState;
    private var gamesCount:int;

    private var tutor:MenuTutorController;

    /** Показывался ли уже туториал для этого экрана за текущий запуск приложения */
    private const tutorShows:Dictionary = initTutorShows();

    private static function initTutorShows():Dictionary {
        const result:Dictionary = new Dictionary();
        for each(var screenIndex:int in ScreenChangedEvent.ALL) {
            result[screenIndex] = false;
        }
        return result;
    }

    public function MenuController(view:MenuView, server:Server, model:MenuModel, social:ISocial, tutorState:MutableTutorState, gamesCount:int) {
        this.view = view;
        this.server = server;
        this.model = model;
        this.social = social;

        this.tutorState = tutorState;
        this.gamesCount = gamesCount;

        tutor = new MenuTutorController(view.tutor);

        server.addEventListener(AccountStateEvent.ACCOUNT_STATE, onAccountStateUpdated);
        server.addEventListener(TopEvent.TOP, onTopUpdated);
        server.addEventListener(PlaceEvent.PLACE, onPlaceUpdated);

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

        social.ui.addEventListener(VideoAdvertEvent.AD_COMPLETED, onAdvertComplete);
        social.ui.addEventListener(VideoAdvertEvent.AD_SKIPPED, onAdvertComplete);
        view.addEventListener(ViewEvents.SHOW_ADVERT, onShowAdvert);
    }

    private function onShowAdvert(event:Event):void {
        social.ui.showVideoAdvert();
        view.advertVisible = false;
    }

    private function onAdvertComplete(event:VideoAdvertEvent):void {
        setTimeout(function ():void {
            server.acceptAdvert(true)
        }, 2000); // таймаут, чтобы игрок увидел анимацию
    }

    private function onAccountStateUpdated(event:AccountStateEvent):void {
        const afterGame:Boolean = event.gamesCount >= model.gamesCount;
        model.mergeAccountStateDto(event.getAccountState());
        view.slots = model.slots;
        view.gold = model.gold;
        view.itemsCount = model.itemsCount;
        view.skillLevels = model.skillLevels;
        view.lock = false;

        if (afterGame && model.canShowAdvert) view.advertVisible = true;
    }

    private function onTopUpdated(e:TopEvent):void {
        model.mergeTopDto(e.getTop());
        view.top = model.top;
    }

    private function onPlaceUpdated(e:PlaceEvent):void {
        model.mergePlaceDto(e.getPlace());
        view.place = model.place;
    }

    private function onSlotClick(event:SlotClickEvent):void {
        const slot:Slot = model.slots.getSlot(event.slotId);
        if (slot.hasBuildingPrototype) {
            const canUpgrade:Boolean = slot.buildingPrototype.buildingLevel != BuildingLevel.LEVEL_3;
            const canRemove:Boolean = model.slots.buildingsCount > 1;
            if (canUpgrade) {
                const nextLevel:BuildingLevel = getNextLevel(slot.buildingPrototype.buildingLevel);
                const upgradePrice:int = model.buildingPrices.getPrice(nextLevel);
            }
            if (canUpgrade || canRemove) {
                if (!tutorState.slot) {
                    tutorState.slot = Bool.TRUE;
                    server.sendTutorState(tutorState.toDto());
                    server.stat(StatAction.TUTOR_SLOT_CLICK);
                }

                view.openUpgradePopup(event.slotId, slot.buildingPrototype.buildingType, canUpgrade, canRemove, upgradePrice);
            }

        } else {
            if (!tutorState.emptySlot) {
                tutorState.emptySlot = Bool.TRUE;
                server.sendTutorState(tutorState.toDto());
                server.stat(StatAction.TUTOR_EMPTY_SLOT_CLICK);
            }

            view.openBuildPopup(event.slotId, model.buildingPrices.buildPrice);
        }
    }

    private function onBuild(event:BuildEvent):void {
        if (model.gold < model.buildingPrices.buildPrice) {
            view.animatePrice();
        } else {
            server.buyBuilding(event.slotId, event.buildingType);

            view.closePopup();
            view.lock = true;
        }
    }

    private function onUpgradeBuilding(event:UpgradeBuildingEvent):void {
        const slot:Slot = model.slots.getSlot(event.slotId);
        if (!slot.hasBuildingPrototype) throw new Error();
        const nextLevel:BuildingLevel = getNextLevel(slot.buildingPrototype.buildingLevel);
        const price:int = model.buildingPrices.getPrice(nextLevel);

        if (model.gold < price) {
            view.animatePrice();
        } else {
            server.upgradeBuilding(event.slotId);

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
        server.removeBuilding(event.slotId);

        view.closePopup();
        view.lock = true;
    }

    private function onMagicItemClick(event:MagicItemClickEvent):void {
        if (model.gold < model.itemPrice) {
            view.animatePrice();
        } else {
            if (!tutorState.magicItem) {
                tutorState.magicItem = Bool.TRUE;
                server.sendTutorState(tutorState.toDto());
                server.stat(StatAction.TUTOR_ITEM_CLICK);
            }

            server.buyItem(event.itemType);

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
                    tutorState.skills = Bool.TRUE;
                    server.sendTutorState(tutorState.toDto());
                    server.stat(StatAction.TUTOR_SKILL_CLICK);
                }

                server.upgradeSkill(event.skillType);

                view.animateFlask(event.skillType);
                view.lock = true;
            }
        }
    }

    // payment

    private function onBuy(event:Event):void {
        const product:Product = model.products.product;
        social.ui.showPaymentDialog(new PaymentDialogData(product.id, product.title, product.description, product.price));
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

    public function cancelAdvert():void {
        if (view.advertVisible) {
            server.acceptAdvert(false);
            view.advertVisible = false;
        }
    }

    private function onScreenChanged(event:ScreenChangedEvent):void {
        const screenIndex:int = event.screenIndex;

        if (screenIndex != ScreenChangedEvent.SCREEN_MAIN) {
            cancelAdvert();
        }

        if (screenIndex > 0) {
            if (!tutorState.navigate) {
                tutorState.navigate = Bool.TRUE;
                server.sendTutorState(tutorState.toDto());
                server.stat(StatAction.TUTOR_NAVIGATE);
            }
        }

        if (gamesCount >= 3 && !tutorShows[screenIndex]) {
            switch (screenIndex) {
                case ScreenChangedEvent.SCREEN_MAIN:
                    if (!tutorState.slot && model.gold >= model.buildingPrices.getPrice(BuildingLevel.LEVEL_2)) {
                        tutorShows[screenIndex] = true;
                        view.tutor.play(tutor.playSlot(model.slots.getNotEmptySlot()));
                    } else if (!tutorState.emptySlot && model.slots.getEmptySlot() && model.gold >= model.buildingPrices.buildPrice) {
                        tutorShows[screenIndex] = true;
                        view.tutor.play(tutor.playSlot(model.slots.getEmptySlot()));
                    }
                    break;
                case ScreenChangedEvent.SCREEN_SHOP:
                    if (!tutorState.magicItem && gamesCount >= 4 && model.gold >= model.itemPrice) {
                        tutorShows[screenIndex] = true;
                        view.tutor.play(tutor.playMagicItem());
                    }
                    break;
                case ScreenChangedEvent.SCREEN_SKILLS:
                    if (!tutorState.skills && gamesCount >= 4 && model.gold >= model.upgradePrices.firstUpgradePrice) {
                        tutorShows[screenIndex] = true;
                        view.tutor.play(tutor.playFlask());
                    }
                    break;
            }
        } else {
            if (screenIndex == ScreenChangedEvent.SCREEN_MAIN && !tutorState.navigate) {
                tutorShows[screenIndex] = true;
                view.tutor.play(tutor.playNavigate());
            }
        }
    }
}
}
