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

    private function getBuildingsById(id:PlayerIdDTO):Vector.<Building> {
        const result:Vector.<Building> = new <Building>[];
        for each(var building:Building in buildings) {
            if (building.owner.equalsId(id)) result.push(building);
        }
        return result;
    }

    public function getEnemyBuildings(ids:Vector.<PlayerIdDTO>):Vector.<Vector.<Building>> {
        const result: Vector.<Vector.<Building>> = new <Vector.<Building>>[];
        for each(var id: PlayerIdDTO in ids) {
            result.push(getBuildingsById(id));
        }
        return result;
    }

    public function getSelfBuildings(selfId:PlayerIdDTO):Vector.<Building> {
        return getBuildingsById(selfId);
    }

    public function getSelfBuildingPos(selfId:PlayerIdDTO):Point {
        const selfBuildings:Vector.<Building> = getSelfBuildings(selfId);
        if (selfBuildings.length < 1) throw new Error("getSelfBuildingPos: buildings.length=" + selfBuildings.length);
        return selfBuildings[0].pos;
    }

    public function getSelfBuildingsPos(selfId:PlayerIdDTO):Vector.<Point> {
        const selfBuildings:Vector.<Building> = getSelfBuildings(selfId);
        if (selfBuildings.length < 3) return null;
        return new <Point>[
            selfBuildings[1].pos,
            selfBuildings[2].pos
        ];
    }

    public function getUnstrengthenedSelfBuildingPos(selfId:PlayerIdDTO):Point {
        for each(var building:Building in buildings) {
            if (building.owner.equalsId(selfId) && !building.strengthened) return building.pos;
        }
        return null;
    }

    public function getEnemyBuildingPos(selfId:PlayerIdDTO):Point {
        for each(var building:Building in buildings) {
            if (building.owner && !building.owner.equalsId(selfId)) return building.pos;
        }
        throw new Error("no enemy buildings");
    }
}
}
