//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.units {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.MovableView;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.PlayerId;
import ru.rknrl.dto.UnitId;

public class UnitsView extends MovableView {
    public function UnitsView() {
        super("unit");
    }

    public function addUnit(id:UnitId, buildingType:BuildingType, buildingLevel:BuildingLevel, ownerId:PlayerId, count:int, strengthened:Boolean, pos:Point):void {
        add(id.id, pos, new UnitView(buildingType, buildingLevel, ownerId, count, strengthened));
    }

    public function setUnitCount(id:UnitId, count:int):void {
        UnitView(byId(id.id)).count = count;
    }
}
}
