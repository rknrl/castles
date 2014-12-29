package ru.rknrl.castles.view.menu.main {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.LockView;
import ru.rknrl.castles.view.Shadow;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;

public class SlotView extends Sprite {
    private var shadow:Shadow;
    private var buildingLayer:Sprite;
    private var lockView:LockView;

    public function SlotView(slotId:SlotId, dto:SlotDTO) {
        _id = slotId;
        mouseChildren = false;
        addChild(shadow = new Shadow());
        addChild(buildingLayer = new Sprite());
        addChild(lockView = new LockView());

        this.dto = dto;
    }

    private var _id:SlotId;

    public function get id():SlotId {
        return _id;
    }

    private var building:DisplayObject;

    public function set dto(value:SlotDTO):void {
        if (building) {
            buildingLayer.removeChild(building);
            building = null;
        }

        if (value.hasBuildingPrototype) {
            building = Fla.createBuilding(value.buildingPrototype.type, value.buildingPrototype.level);
            building.transform.colorTransform = Colors.slotIdToColorTransforms(id);
            buildingLayer.addChild(building);
        }
        shadow.scaleX = shadow.scaleY = value.hasBuildingPrototype ? Fla.buildingLevelToScale(value.buildingPrototype.level) : Fla.buildingLevelToScale(BuildingLevel.LEVEL_1);
    }

    public function set lock(value:Boolean):void {
        lockView.visible = value;
        mouseEnabled = !value;
    }
}
}
