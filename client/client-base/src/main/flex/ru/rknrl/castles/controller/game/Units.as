package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.units.UnitsView;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitIdDTO;
import ru.rknrl.dto.UnitUpdateDTO;

public class Units {
    private var view:UnitsView;

    public function Units(view:UnitsView) {
        this.view = view;
    }

    private const units:Vector.<Unit> = new <Unit>[];

    public function getUnit(id:UnitIdDTO):Unit {
        for each(var unit:Unit in units) {
            if (unit.id.id == id.id) return unit;
        }
        throw new Error("can't find unit " + id);
    }

    public function add(endPos:Point, dto:UnitDTO):void {
        const startPos:Point = new Point(dto.pos.x, dto.pos.y);

        units.push(new Unit(dto.id, startPos, endPos, getTimer(), dto.speed));

        view.addUnit(dto.id, dto.type, BuildingLevel.LEVEL_1, dto.owner, dto.count, dto.strengthened, startPos);
    }

    public function updateUnit(dto:UnitUpdateDTO):void {
        getUnit(dto.id).update(getTimer(), new Point(dto.pos.x, dto.pos.y), dto.speed);

        view.setUnitCount(dto.id, dto.count);
        view.setPos(dto.id.id, new Point(dto.pos.x, dto.pos.y));
    }

    public function remove(id:UnitIdDTO):void {
        const index:int = units.indexOf(getUnit(id));
        units.splice(index, 1);
        view.remove(id.id);
    }

    public function update(time:int):void {
        for each(var unit:Unit in units) {
            view.setPos(unit.id.id, unit.pos(time));
        }
    }
}
}
