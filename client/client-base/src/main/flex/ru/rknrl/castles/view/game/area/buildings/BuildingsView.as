//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.buildings {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.core.points.Point;
import ru.rknrl.dto.BuildingId;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class BuildingsView extends Sprite {
    private static const buildingY:Number = 37 / 2 - 4;

    private const buildings:Dictionary = new Dictionary();

    public function byId(id:BuildingId):BuildingView {
        const building:BuildingView = buildings[id.id];
        if (!building) throw new Error("can't find building " + id.id);
        return building;
    }

    public function addBuilding(id:BuildingId, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point):void {
        const building:BuildingView = new BuildingView(id, buildingType, buildingLevel, owner, count, strengthened, pos);
        building.x = pos.x;
        building.y = pos.y + buildingY;
        if (buildings[id.id]) throw new Error("building " + id.id + " already exists");
        buildings[id.id] = building;
        addChild(building);
    }

    public function setBuildingCount(id:BuildingId, count:int):void {
        byId(id).count = count;
    }

    public function setBuildingsDust(id:BuildingId, visible:Boolean):void {
        byId(id).dustVisible = visible;
    }

    public function setBuildingOwner(id:BuildingId, owner:BuildingOwner):void {
        byId(id).owner = owner;
    }

    public function setBuildingStrengthened(id:BuildingId, strengthened:Boolean):void {
        byId(id).strengthened = strengthened;
    }

    public function tutorBlur(buildingIds:Vector.<BuildingId>):void {
        for each(var id:BuildingId in buildingIds)
            byId(id).tutorBlur = true;
    }

    public function tutorUnblur():void {
        for each(var building:BuildingView in buildings)
            building.tutorBlur = false;
    }
}
}
