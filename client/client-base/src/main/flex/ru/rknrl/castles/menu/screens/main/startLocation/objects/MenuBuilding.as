package ru.rknrl.castles.menu.screens.main.startLocation.objects {
import flash.display.Sprite;

import ru.rknrl.castles.game.view.BuildingBase;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Shadow;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
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

    private var bodyHolder:Sprite;
    private var shadow:Shadow;

    private var upCircle:UpButton;
    private var plusCircle:PlusButton;

    public function MenuBuilding(slotId:SlotId) {
        _slotId = slotId;

        addChild(bodyHolder = new Sprite());

        const halfCellSize:Number = CellSize.SIZE.id() / 2;

        upCircle = new UpButton(16, Colors.darkMagenta);
//        addChild(upCircle); // todo
        upCircle.x = halfCellSize - 4;
        upCircle.y = -halfCellSize + 4;
        upCircle.mouseChildren = upCircle.mouseEnabled = false;

        plusCircle = new PlusButton(16, Colors.darkMagenta);
//        addChild(plusCircle); // todo
        plusCircle.x = halfCellSize - 4;
        plusCircle.y = -halfCellSize + 4;
        plusCircle.mouseChildren = plusCircle.mouseEnabled = false;

        mouseChildren = false;
    }

    public function update(slot:SlotDTO):void {
        if (body) {
            bodyHolder.removeChild(body);
            body = null;
            removeChild(shadow);
        }

        _hasBuilding = slot.hasBuildingPrototype;
        if (_hasBuilding) {
            _buildingType = slot.buildingPrototype.type;
            _buildingLevel = slot.buildingPrototype.level;
            const scale:Number = Utils.getScaleByLevel(_buildingLevel);

            body = Utils.getBuildingBody(_buildingType);
            body.scaleX = body.scaleY = scale;
            body.transform.colorTransform = Colors.colorToTransform(Colors.magenta);
            bodyHolder.addChild(body);

            shadow = new Shadow();
            shadow.scaleX = shadow.scaleY = scale;
            shadow.y = Layout.shadowY * scale;
            addChild(shadow)
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

    public function set lock(value:Boolean):void {
        visible = !value;
    }
}
}
