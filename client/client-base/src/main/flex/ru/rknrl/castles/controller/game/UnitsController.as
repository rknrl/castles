//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.display.Sprite;
import flash.utils.Dictionary;
import flash.utils.getTimer;

import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.game.area.units.BloodView;
import ru.rknrl.castles.view.game.area.units.UnitView;
import ru.rknrl.core.GameObject;
import ru.rknrl.core.GameObjectsController;
import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitId;
import ru.rknrl.dto.UnitUpdateDTO;

public class UnitsController extends GameObjectsController {
    private var bloodView:BloodView;

    public function UnitsController(layer:Sprite, bloodView:BloodView) {
        this.bloodView = bloodView;
        super(layer)
    }

    private const idToUnit:Dictionary = new Dictionary();

    public function getUnit(id:UnitId):Unit {
        return idToUnit[id.id];
    }

    public function addUnit(time:int, endPos:Point, dto:UnitDTO):void {
        const startPos:Point = new Point(dto.pos.x, dto.pos.y);
        const points:Points = Points.two(startPos, endPos);

        const unit:Unit = new Unit(dto.owner, points, getTimer(), dto.duration, dto.count);
        idToUnit[dto.id.id] = unit;
        const unitView:UnitView = new UnitView(dto.buildingType, BuildingLevel.LEVEL_1, dto.owner, dto.count, dto.strengthened);
        add(time, unit, unitView);
    }

    public function updateUnit(time:int, dto:UnitUpdateDTO):void {
        const unit:Unit = idToUnit[dto.id];
        if (unit) {
            if (unit.count > dto.count) addBlood(time, dto.id);
            unit.count = dto.count;

            const unitView:UnitView = objectToView[unit];
            unitView.count = dto.count;
        }
    }

    private static function keyByValue(value:*, map:Dictionary):* {
        for (var key:* in map) if (map[key] == value) return key;
        return null;
    }

    override public function remove(time:int, object:GameObject):void {
        super.remove(time, object);
        const id:int = keyByValue(object, idToUnit);
        delete idToUnit[id];
    }

    public function kill(time:int, id:UnitId):void {
        const unit:Unit = getUnit[id.id];
        if (unit) {
            addBlood(time, id);
            remove(time, unit);
        }
    }

    private function addBlood(time:int, id:UnitId):void {
        const unit:Unit = getUnit[id.id];
        bloodView.addBlood(unit.pos(time), Colors.playerColor(unit.owner));
    }
}
}
