package ru.rknrl.castles.menu.screens.main.popup.build {
import flash.display.DisplayObject;
import flash.events.MouseEvent;

import ru.rknrl.castles.menu.screens.main.popup.popup.Popup;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.SlotId;

public class BuildPopup extends Popup {
    private var slotId:SlotId;

    private var items:Vector.<BuildItem> = new <BuildItem>[];

    private var popup:Popup;

    public function BuildPopup(slotId:SlotId, price:int, layout:Layout, locale:CastlesLocale) {
        this.slotId = slotId;

        for each(var buildingType:BuildingType in BuildingType.values) {
            const item:BuildItem = new BuildItem(buildingType, price, Colors.randomColor(), layout, locale);
            item.addEventListener(MouseEvent.CLICK, onItemClick);
            items.push(item);
        }

        addChild(popup = layout.createPopup(locale.build, Vector.<DisplayObject>(items)));
    }

    public function updateLayout(layout:Layout):void {
        layout.updatePopup(popup);
        for each(var item:BuildItem in items) {
            item.updateLayout(layout);
        }
    }

    override public function set transition(value:Number):void {
        popup.transition = value;
    }

    public function animate():void {
        // todo item.animate();
    }

    private function onItemClick(event:MouseEvent):void {
        const item:BuildItem = BuildItem(event.target);
        dispatchEvent(new BuildEvent(slotId, item.buildingType));
    }
}
}

import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.menu.screens.main.popup.popup.item.PopupItem;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuildingType;

class BuildItem extends Sprite {
    private var _buildingType:BuildingType;

    public function get buildingType():BuildingType {
        return _buildingType;
    }

    private var popupItem:PopupItem;

    public function BuildItem(buildingType:BuildingType, price:int, color:uint, layout:Layout, locale:CastlesLocale) {
        _buildingType = buildingType;
        mouseChildren = false;

        const icon:DisplayObject = Utils.getBuildingBody(buildingType);
        icon.transform.colorTransform = Colors.colorToTransform(color);
        const name:String = locale.getBuildingName(buildingType);
        const info:String = locale.getBuildingInfo(buildingType);
        addChild(popupItem = layout.createPopupItem(icon, name, info, price, color));
    }

    public function updateLayout(layout:Layout):void {
        layout.updatePopupItem(popupItem);
    }

    public function animate():void {
        popupItem.animate();
    }
}