//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.game.area.units.BloodView;
import ru.rknrl.castles.view.game.area.units.UnitsView;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitId;
import ru.rknrl.dto.UnitUpdateDTO;

public class Units {
    private var view:UnitsView;
    private var bloodView:BloodView;

    public function Units(view:UnitsView, bloodView:BloodView) {
        this.view = view;
        this.bloodView = bloodView;
    }

    private const units:Vector.<Unit> = new <Unit>[];

    public function getUnit(id:UnitId):Unit {
        for each(var unit:Unit in units) {
            if (unit.id.id == id.id) return unit;
        }
        return null;
    }

    public function add(endPos:Point, dto:UnitDTO):void {
        const startPos:Point = new Point(dto.pos.x, dto.pos.y);

        units.push(new Unit(dto.id, dto.owner, startPos, endPos, getTimer(), dto.duration, dto.count));

        view.addUnit(dto.id, dto.buildingType, BuildingLevel.LEVEL_1, dto.owner, dto.count, dto.strengthened, startPos);
    }

    public function updateUnit(dto:UnitUpdateDTO):void {
        const unit:Unit = getUnit(dto.id);
        if (unit) {
            if (unit.count > dto.count) addBlood(unit.id);
            unit.setCount(dto.count);
            view.setUnitCount(dto.id, dto.count);
        }
    }

    public function kill(id:UnitId):void {
        const unit:Unit = getUnit(id);
        if (unit) {
            addBlood(id);
            remove(id);
        }
    }

    private function addBlood(id:UnitId):void {
        const unit:Unit = getUnit(id);
        bloodView.addBlood(unit.pos(getTimer()), Colors.playerColor(unit.owner));
    }

    private function remove(id:UnitId):void {
        const unit:Unit = getUnit(id);
        const index:int = units.indexOf(unit);
        units.splice(index, 1);
        view.remove(id.id);
    }

    public function update(time:int):void {
        const toRemove:Vector.<Unit> = new <Unit>[];
        for each(var unit:Unit in units) {
            view.setPos(unit.id.id, unit.pos(time));
            if (unit.needRemove(time)) toRemove.push(unit);
        }

        for each(unit in toRemove) remove(unit.id);
    }
}
}
