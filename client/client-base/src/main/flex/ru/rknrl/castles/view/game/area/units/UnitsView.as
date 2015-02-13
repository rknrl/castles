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
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.UnitIdDTO;

public class UnitsView extends MovableView {
    public function UnitsView() {
        super("unit");
    }

    public function addUnit(id:UnitIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, ownerId:PlayerIdDTO, count:int, strengthened:Boolean, pos:Point):void {
        add(id.id, pos, new UnitView(buildingType, buildingLevel, ownerId, count, strengthened));
    }

    public function setUnitCount(id:UnitIdDTO, count:int):void {
        UnitView(byId(id.id)).count = count;
    }
}
}
