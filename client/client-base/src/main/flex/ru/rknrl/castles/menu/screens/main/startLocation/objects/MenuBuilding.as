package ru.rknrl.castles.menu.screens.main.startLocation.objects {
import flash.display.Sprite;

import ru.rknrl.castles.game.view.BuildingBase;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.funnyUi.buttons.round.PlusButton;
import ru.rknrl.funnyUi.buttons.round.UpButton;

public class MenuBuilding extends BuildingBase {
    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    private var buildingScale:Number;

    private var bodyHolder:Sprite;
    private var upCircle:UpButton;
    private var plusCircle:PlusButton;

    public function MenuBuilding(slotId:SlotId, buildingScale:Number) {
        _slotId = slotId;
        this.buildingScale = buildingScale;

        addChild(bodyHolder = new Sprite());

        const halfCellSize:Number = CellSize.SIZE.id() * buildingScale / 2;

        addChild(upCircle = new UpButton(16, Colors.darkMagenta));
        upCircle.x = halfCellSize - 4;
        upCircle.y = -halfCellSize + 4;
        upCircle.mouseChildren = upCircle.mouseEnabled = false;

        addChild(plusCircle = new PlusButton(16, Colors.darkMagenta));
        plusCircle.x = halfCellSize - 4;
        plusCircle.y = -halfCellSize + 4;
        plusCircle.mouseChildren = plusCircle.mouseEnabled = false;

        mouseChildren = false;
    }

    public function differentWith(slot:SlotDTO):Boolean {
        if (slot.hasBuildingPrototype) {
            if (hasBuilding) {
                return buildingType != slot.buildingPrototype.type || buildingLevel != slot.buildingPrototype.level;
            } else {
                return true;
            }
        }
        return hasBuilding;
    }

    public function update(slot:SlotDTO):void {
        if (body) {
            bodyHolder.removeChild(body);
            body = null;
        }

        _hasBuilding = slot.hasBuildingPrototype;
        if (_hasBuilding) {
            _buildingType = slot.buildingPrototype.type;
            _buildingLevel = slot.buildingPrototype.level;

            body = Utils.getBuildingBody(_buildingType);
            body.scaleX = body.scaleY = Utils.getScaleByLevel(_buildingLevel) * buildingScale;
            body.transform.colorTransform = Colors.colorToTransform(Colors.magenta);
            bodyHolder.addChild(body);
        }

        upCircle.visible = _hasBuilding && _buildingLevel != BuildingLevel.LEVEL_3;
        plusCircle.visible = !_hasBuilding;
    }

    public function set color(value:uint):void {
        if (_hasBuilding) {
            body.transform.colorTransform = Colors.colorToTransform(value);
        }
    }

    private var _hasBuilding:Boolean;

    public function get hasBuilding():Boolean {
        return _hasBuilding;
    }

    private var _buildingType:BuildingType;

    public function get buildingType():BuildingType {
        if (!_hasBuilding) throw new Error();
        return _buildingType;
    }

    private var _buildingLevel:BuildingLevel;

    public function get buildingLevel():BuildingLevel {
        if (!_hasBuilding) throw new Error();
        return _buildingLevel;
    }

    public function lock():void {
        visible = false;
    }

    public function unlock():void {
        visible = true;
    }
}
}
