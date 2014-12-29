package ru.rknrl.castles.view.game.area.units {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.UnitIdDTO;

public class UnitsView extends Sprite {
    private const units:Dictionary = new Dictionary();

    public function addUnit(id:UnitIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, ownerId:PlayerIdDTO, count:int, strengthened:Boolean, pos:Point):void {
        const unit:UnitView = new UnitView(buildingType, buildingLevel, ownerId, count, strengthened, pos);
        addChild(unit);
        units[id.id] = unit;
    }

    public function setUnitPos(id:UnitIdDTO, pos:Point):void {
        const unit:UnitView = units[id.id];
        unit.pos = pos;
    }

    public function setUnitCount(id:UnitIdDTO, count:int):void {
        const unit:UnitView = units[id.id];
        unit.count = count;
    }

    public function removeUnit(id:UnitIdDTO):void {
        const unit:UnitView = units[id.id];
        removeChild(unit);
        delete  units[id.id];
    }
}
}
