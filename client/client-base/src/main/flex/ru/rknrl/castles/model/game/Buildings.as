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
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.PlayerIdDTO;

public class Buildings {
    private static const mouseAreaRadius:Number = CellSize.SIZE.id() * 2 / 3;

    private var buildings:Vector.<Building>;

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

    public function selfInXy(selfId:PlayerIdDTO, pos:Point):Building {
        const building:Building = inXy(pos);
        return building && building.owner.equalsId(selfId) ? building : null;
    }

    // tutorial:

    private function byPlayerId(id:PlayerIdDTO):Vector.<Building> {
        const result:Vector.<Building> = new <Building>[];
        for each(var building:Building in buildings) {
            if (building.owner.equalsId(id)) result.push(building);
        }
        return result;
    }

    public function getEnemyBuildings(playerIds:Vector.<PlayerIdDTO>):Vector.<Vector.<Building>> {
        const result:Vector.<Vector.<Building>> = new <Vector.<Building>>[];
        for each(var id:PlayerIdDTO in playerIds) {
            result.push(byPlayerId(id));
        }
        return result;
    }

    public function getSelfBuildings(selfId:PlayerIdDTO):Vector.<Building> {
        return byPlayerId(selfId);
    }

    private static function ij(i:int, j:int):Point {
        return new Point((i + 0.5) * CellSize.SIZE.id(), (j + 0.5) * CellSize.SIZE.id())
    }

    public static const sourceBuilding1:Point = ij(2, 0);
    public static const targetBuilding1:Point = ij(4, 3);

    public static const sourceBuilding2_1:Point = ij(0, 0);
    public static const sourceBuilding2_2:Point = ij(4, 0);
    public static const targetBuilding2:Point = ij(2, 5);

    public function getSelfBuildingId(selfId:PlayerIdDTO):BuildingIdDTO {
        const selfBuildings:Vector.<Building> = getSelfBuildings(selfId);
        return selfBuildings[0].id;
    }

    public function getEnemyBuildingId(selfId:PlayerIdDTO):BuildingIdDTO {
        for each(var building:Building in buildings) {
            if (building.owner && !building.owner.equalsId(selfId)) return building.id;
        }
        throw new Error("no enemy buildings");
    }
}
}
