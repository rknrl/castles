package ru.rknrl.castles.view.game.area.buildings {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;

public class BuildingsView extends Sprite {
    private static const buildingY:Number = CellSize.SIZE.id() / 2 - 4;

    private const buildings:Dictionary = new Dictionary();

    public function addBuilding(id:BuildingIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point):void {
        const building:BuildingView = new BuildingView(id, buildingType, buildingLevel, owner, count, strengthened);
        building.x = pos.x;
        building.y = pos.y + buildingY;
        addChild(building);
        buildings[id.id] = building;
    }

    public function setBuildingCount(id:BuildingIdDTO, count:int):void {
        const building:BuildingView = buildings[id.id];
        building.count = count;
    }

    public function setBuildingOnwer(id:BuildingIdDTO, owner:BuildingOwner):void {
        const building:BuildingView = buildings[id.id];
        building.owner = owner;
    }

    public function setBuildingStrengthened(id:BuildingIdDTO, strengthened:Boolean):void {
        const building:BuildingView = buildings[id.id];
        building.strengthened = strengthened;
    }
}
}
