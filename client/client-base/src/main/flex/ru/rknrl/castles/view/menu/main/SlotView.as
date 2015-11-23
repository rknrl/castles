//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.main {
import protos.BuildingLevel;
import protos.Slot;
import protos.SlotId;

import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.model.menu.main.Slots;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.utils.Animated;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.castles.view.utils.Shadow;

public class SlotView extends Animated {
    private static const lockViewY:Number = -8;
    private static const mouseHolderSize:Number = 40;

    private var shadow:Shadow;
    private var buildingLayer:Sprite;
    private var lockView:LockView;

    public function SlotView(slotId:SlotId, dto:Slot) {
        _id = slotId;
        mouseChildren = false;

        const mouseHolder:Bitmap = new Bitmap(Colors.transparent);
        mouseHolder.width = mouseHolder.height = mouseHolderSize;
        mouseHolder.x = mouseHolder.y = -mouseHolderSize / 2;
        addChild(mouseHolder);

        addChild(shadow = new Shadow());
        addChild(buildingLayer = new Sprite());
        addChild(lockView = new LockView());
        lockView.y = lockViewY;

        this.dto = dto;
    }

    private var _id:SlotId;

    public function get id():SlotId {
        return _id;
    }

    private var building:DisplayObject;

    private var _dto:Slot;

    public function set dto(value:Slot):void {
        if (_dto && Slots.equals(_dto, value)) return;
        _dto = value;

        if (building) {
            buildingLayer.removeChild(building);
            building = null;
        }

        if (value.hasBuildingPrototype) {
            building = Fla.createBuilding(value.buildingPrototype.buildingType, value.buildingPrototype.buildingLevel);
            building.transform.colorTransform = Colors.transform(Colors.slot(id));
            buildingLayer.addChild(building);
        }
        shadow.scaleX = shadow.scaleY = value.hasBuildingPrototype ? Fla.buildingLevelToScale(value.buildingPrototype.buildingLevel) : Fla.buildingLevelToScale(BuildingLevel.LEVEL_1);

        bounce();
    }

    public function set lock(value:Boolean):void {
        lockView.visible = value;
        mouseEnabled = !value;
    }
}
}
