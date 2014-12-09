package ru.rknrl.castles.menu.screens.main.popup.upgrade {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;

import ru.rknrl.castles.menu.screens.main.popup.popup.Popup;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.SlotId;

public class UpgradePopup extends Popup {
    private var slotId:SlotId;
    private var buildingLevel:BuildingLevel;

    private var upgradeItem:UpgradeItem;
    private var removeItem:RemoveItem;

    private var popup:Popup;

    public function UpgradePopup(slotId:SlotId, buildingType:BuildingType, buildingLevel:BuildingLevel, buildingsCount:int, upgradePrice:int, layout:Layout, locale:CastlesLocale) {
        this.slotId = slotId;
        this.buildingLevel = buildingLevel;

        const items:Vector.<DisplayObject> = new <DisplayObject>[];
        if (buildingLevel != BuildingLevel.LEVEL_3) {
            upgradeItem = new UpgradeItem(upgradePrice, Colors.randomColor(), layout, locale);
            upgradeItem.addEventListener(MouseEvent.CLICK, onUpgradeClick);
            items.push(upgradeItem);
        }
        if (buildingsCount > 1) {
            removeItem = new RemoveItem(Colors.randomColor(), layout, locale);
            removeItem.addEventListener(MouseEvent.CLICK, onRemoveClick);
            items.push(removeItem);
        }

        popup = layout.createPopup(locale.getBuildingName(buildingType), items);
        addChild(popup);
    }

    public function updateLayout(layout:Layout):void {
        layout.updatePopup(popup);
        if (upgradeItem) upgradeItem.updateLayout(layout);
        if (removeItem) removeItem.updateLayout(layout);
    }

    override public function set transition(value:Number):void {
        popup.transition = value;
    }

    public function animate():void {
        upgradeItem.animate();
    }

    private function onRemoveClick(event:MouseEvent):void {
        dispatchEvent(new RemoveEvent(slotId));
    }

    private function onUpgradeClick(event:MouseEvent):void {
        dispatchEvent(new UpgradeEvent(slotId, Utils.nextBuildingLevel(buildingLevel)));
    }
}
}

import flash.display.Sprite;

import ru.rknrl.castles.menu.screens.main.popup.popup.item.PopupItem;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.funnyUi.buttons.round.RemoveButton;
import ru.rknrl.funnyUi.buttons.round.UpButton;

class UpgradeItem extends Sprite {
    private var popupItem:PopupItem;

    public function UpgradeItem(price:int, color:uint, layout:Layout, locale:CastlesLocale) {
        mouseChildren = false;

        const icon:UpButton = new UpButton(layout.popupIconSize / 2, color);
        addChild(popupItem = layout.createPopupItem(icon, locale.upgrade, locale.upgradeInfo, price, color));
    }

    public function updateLayout(layout:Layout):void {
        layout.updatePopupItem(popupItem);
    }

    public function animate():void {
        popupItem.animate();
    }
}

class RemoveItem extends Sprite {
    private var popupItem:PopupItem;

    public function RemoveItem(color:uint, layout:Layout, locale:CastlesLocale) {
        mouseChildren = false;

        const icon:RemoveButton = new RemoveButton(layout.popupIconSize / 2, color);
        addChild(popupItem = layout.createPopupItem(icon, locale.remove, "", 0, color));
    }

    public function updateLayout(layout:Layout):void {
        layout.updatePopupItem(popupItem);
    }
}