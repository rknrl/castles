package ru.rknrl.castles.view.game.area.units {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.UnitIdDTO;

public class UnitsView extends Sprite {
    private const units:Dictionary = new Dictionary();

    private function byId(id:UnitIdDTO):UnitView {
        const unit:UnitView = units[id.id];
        if (!unit) throw new Error("can't find unit " + id.id);
        return unit;
    }

    public function addUnit(id:UnitIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, ownerId:PlayerIdDTO, count:int, strengthened:Boolean, pos:Point):void {
        const unit:UnitView = new UnitView(buildingType, buildingLevel, ownerId, count, strengthened);
        if (units[id.id]) throw new Error("unit " + id.id + "already exists");
        units[id.id] = unit;
        addChild(unit);
        setUnitPos(id, pos);
    }

    public function setUnitPos(id:UnitIdDTO, pos:Point):void {
        byId(id).x = pos.x;
        byId(id).y = pos.y;
    }

    public function setUnitCount(id:UnitIdDTO, count:int):void {
        byId(id).count = count;
    }

    public function removeUnit(id:UnitIdDTO):void {
        removeChild(byId(id));
        delete units[id.id];
    }
}
}
