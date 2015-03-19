//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.Warning;
import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.units.BloodView;
import ru.rknrl.castles.view.game.area.units.UnitsView;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitIdDTO;
import ru.rknrl.dto.UnitUpdateDTO;

public class Units {
    private var view:UnitsView;
    private var bloodView:BloodView;

    public function Units(view:UnitsView, bloodView:BloodView) {
        this.view = view;
        this.bloodView = bloodView;
    }

    private const units:Vector.<Unit> = new <Unit>[];

    public function getUnit(id:UnitIdDTO):Unit {
        for each(var unit:Unit in units) {
            if (unit.id.id == id.id) return unit;
        }
        throw new Warning("can't find unit " + id);
    }

    public function add(endPos:Point, dto:UnitDTO):void {
        const startPos:Point = new Point(dto.pos.x, dto.pos.y);

        units.push(new Unit(dto.id, startPos, endPos, getTimer(), dto.speed, dto.count));

        view.addUnit(dto.id, dto.type, BuildingLevel.LEVEL_1, dto.owner, dto.count, dto.strengthened, startPos);
    }

    public function updateUnit(dto:UnitUpdateDTO):void {
        const unit:Unit = getUnit(dto.id);
        const newPos:Point = new Point(dto.pos.x, dto.pos.y);
        unit.update(getTimer(), newPos, dto.speed, dto.count);
        view.setUnitCount(dto.id, dto.count);
        view.setPos(dto.id.id, newPos);
    }

    public function remove(id:UnitIdDTO):void {
        const unit:Unit = getUnit(id);
        const index:int = units.indexOf(unit);
        units.splice(index, 1);
        view.remove(id.id);
        bloodView.addBlood(unit.pos(getTimer())); // todo Не добавлять кровь при обычном входе в здание
    }

    public function update(time:int):void {
        for each(var unit:Unit in units) {
            view.setPos(unit.id.id, unit.pos(time));
        }
    }
}
}
