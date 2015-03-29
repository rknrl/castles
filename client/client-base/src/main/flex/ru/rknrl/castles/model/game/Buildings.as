//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.PlayerIdDTO;

public class Buildings {
    private static const mouseAreaRadius:Number = CellSize.SIZE.id() * 2 / 3;

    public var buildings:Vector.<Building>;

    public function Buildings(buildings:Vector.<Building>) {
        this.buildings = buildings;
    }

    public function byId(id:BuildingIdDTO):Building {
        for each(var building:Building in buildings) {
            if (building.id.id == id.id) return building;
        }
        throw new Error("can't find building " + id);
    }

    public function inXy(pos:Point):Building {
        for each(var building:Building in buildings) {
            if (building.pos.distance(pos) < mouseAreaRadius) return building;
        }
        return null;
    }

    public function inRadius(pos:Point, radius:Number):Vector.<Building> {
        const result:Vector.<Building> = new <Building>[];
        for each(var building:Building in buildings) {
            if (building.pos.distance(pos) < radius) result.push(building);
        }
        return result;
    }

    public function selfInXy(selfId:PlayerIdDTO, pos:Point):Building {
        const building:Building = inXy(pos);
        return building && building.owner.equalsId(selfId) ? building : null;
    }

    // tutorial:

    private function byPlayerId(id:PlayerIdDTO):Vector.<BuildingIdDTO> {
        const result:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];
        for each(var building:Building in buildings) {
            if (building.owner.equalsId(id)) result.push(building.id);
        }
        return result;
    }

    public function notPlayerId(id:PlayerIdDTO):Vector.<BuildingIdDTO> {
        const result:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];
        for each(var building:Building in buildings) {
            if (!building.owner.equalsId(id)) result.push(building.id);
        }
        return result;
    }

    public function notOwned():Vector.<BuildingIdDTO> {
        const result:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];
        for each(var building:Building in buildings) {
            if (!building.owner.hasOwner) result.push(building.id);
        }
        return result;
    }

    public function owned():Vector.<BuildingIdDTO> {
        const result:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];
        for each(var building:Building in buildings) {
            if (building.owner.hasOwner) result.push(building.id);
        }
        return result;
    }

    public function getBuildingIds(selfId:PlayerIdDTO):Vector.<BuildingIdDTO> {
        return byPlayerId(selfId);
    }

    public function getBuildingId(selfId:PlayerIdDTO):BuildingIdDTO {
        return getBuildingIds(selfId)[0];
    }

    public function getEnemyBuildingId(selfId:PlayerIdDTO):BuildingIdDTO {
        return notPlayerId(selfId)[0];
    }

    public function myTower(ownerId:PlayerIdDTO):BuildingIdDTO {
        for each(var building:Building in buildings) {
            if (building.owner.equalsId(ownerId) && building.buildingType == BuildingType.TOWER)
                return building.id;
        }
        return null;
    }

    public function bigTower(ownerId:PlayerIdDTO):BuildingIdDTO {
        for each(var building:Building in buildings) {
            if (!building.owner.equalsId(ownerId) && building.buildingType == BuildingType.TOWER && building.buildingLevel == BuildingLevel.LEVEL_2)
                return building.id;
        }
        return null;
    }
}
}
