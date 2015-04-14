//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingId;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class Building {
    private var _id:BuildingId;

    public function get id():BuildingId {
        return _id;
    }

    private var _buildingType:BuildingType;

    public function get buildingType():BuildingType {
        return _buildingType;
    }

    private var _buildingLevel:BuildingLevel;

    public function get buildingLevel():BuildingLevel {
        return _buildingLevel;
    }

    private var _pos:Point;

    public function get pos():Point {
        return _pos;
    }

    private var _owner:BuildingOwner;

    public function get owner():BuildingOwner {
        return _owner;
    }

    private var _population:int;

    public function get population():int {
        return _population;
    }

    private var _strengthened:Boolean;

    public function get strengthened():Boolean {
        return _strengthened;
    }

    public function update(owner:BuildingOwner, population:int, strengthened:Boolean):void {
        _owner = owner;
        _population = population;
        _strengthened = strengthened;
    }

    public function Building(id:BuildingId, buildingType:BuildingType, buildingLevel:BuildingLevel, pos:Point, owner:BuildingOwner, population:int, strengthened:Boolean) {
        _id = id;
        _buildingType = buildingType;
        _buildingLevel = buildingLevel;
        _pos = pos;
        _owner = owner;
        _population = population;
        _strengthened = strengthened;
    }

    public static function fromDto(b:BuildingDTO):Building {
        return new Building(
                b.id,
                b.building.buildingType,
                b.building.buildingLevel,
                Point.fromDto(b.pos),
                new BuildingOwner(b.hasOwner, b.owner),
                b.population,
                b.strengthened
        );
    }
}
}
