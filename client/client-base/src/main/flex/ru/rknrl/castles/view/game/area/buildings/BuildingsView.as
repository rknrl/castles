package ru.rknrl.castles.view.game.area.buildings {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class BuildingsView extends Sprite {
    private static const buildingY:Number = 37 / 2 - 4;

    private const buildings:Dictionary = new Dictionary();

    public function byId(id:BuildingIdDTO):BuildingView {
        const building:BuildingView = buildings[id.id];
        if (!building) throw new Error("can't find building " + id.id);
        return building;
    }

    public function addBuilding(id:BuildingIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point):void {
        const building:BuildingView = new BuildingView(id, buildingType, buildingLevel, owner, count, strengthened, pos);
        building.x = pos.x;
        building.y = pos.y + buildingY;
        if (buildings[id.id]) throw new Error("building " + id.id + " already exists");
        buildings[id.id] = building;
        addChild(building);
    }

    public function setBuildingCount(id:BuildingIdDTO, count:int):void {
        byId(id).count = count;
    }

    public function setBuildingOwner(id:BuildingIdDTO, owner:BuildingOwner):void {
        byId(id).owner = owner;
    }

    public function setBuildingStrengthened(id:BuildingIdDTO, strengthened:Boolean):void {
        byId(id).strengthened = strengthened;
    }
}
}
