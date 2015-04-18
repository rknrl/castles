//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.core.points.Point;
import ru.rknrl.dto.BuildingId;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.PlayerId;

public class Buildings {
    private static const mouseAreaRadius:Number = CellSize.SIZE.id() * 2 / 3;

    public var buildings:Vector.<Building>;

    public function Buildings(buildings:Vector.<Building>) {
        this.buildings = buildings;
    }

    public function byId(id:BuildingId):Building {
        for each(var building:Building in buildings) {
            if (building.id.id == id.id) return building;
        }
        throw new Error("can't find building " + id);
    }

    public function inRadius(pos:Point, radius:Number):Vector.<Building> {
        const result:Vector.<Building> = new <Building>[];
        for each(var building:Building in buildings) {
            if (building.pos.distance(pos) < radius) result.push(building);
        }
        return result;
    }

    public function inXy(pos:Point):Building {
        const nearest:Vector.<Building> = inRadius(pos, mouseAreaRadius);
        return nearest.length > 0 ? nearest[0] : null;
    }

    public function selfInXy(selfId:PlayerId, pos:Point):Building {
        const building:Building = inXy(pos);
        return building && building.owner.equalsId(selfId) ? building : null;
    }

    // tutorial:

    public function notPlayerId(id:PlayerId):Vector.<BuildingId> {
        const result:Vector.<BuildingId> = new <BuildingId>[];
        for each(var building:Building in buildings) {
            if (!building.owner.equalsId(id)) result.push(building.id);
        }
        return result;
    }

    public function owned():Vector.<BuildingId> {
        const result:Vector.<BuildingId> = new <BuildingId>[];
        for each(var building:Building in buildings) {
            if (building.owner.hasOwner) result.push(building.id);
        }
        return result;
    }

    public function myTower(ownerId:PlayerId):BuildingId {
        for each(var building:Building in buildings) {
            if (building.owner.equalsId(ownerId) && building.buildingType == BuildingType.TOWER)
                return building.id;
        }
        return null;
    }

    public function bigTower(ownerId:PlayerId):BuildingId {
        for each(var building:Building in buildings) {
            if (!building.owner.equalsId(ownerId) && building.buildingType == BuildingType.TOWER && building.buildingLevel == BuildingLevel.LEVEL_2)
                return building.id;
        }
        return null;
    }
}
}
