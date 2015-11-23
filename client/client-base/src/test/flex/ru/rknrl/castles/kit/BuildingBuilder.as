//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.kit {

import protos.BuildingId;
import protos.BuildingLevel;
import protos.BuildingType;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.core.points.Point;

public class BuildingBuilder {
    private var id:BuildingId = DtoMock.buildingId(1);
    private var buildingType:BuildingType = BuildingType.HOUSE;
    private var buildingLevel:BuildingLevel = BuildingLevel.LEVEL_1;
    private var pos:Point = new Point(0, 0);
    private var owner:BuildingOwner = new BuildingOwner(false);
    private var population:int = 10;
    private var strengthened:Boolean = false;

    public function setId(value:BuildingId):BuildingBuilder {
        id = value;
        return this;
    }

    public function setBuildingType(value:BuildingType):BuildingBuilder {
        buildingType = value;
        return this;
    }

    public function setBuildingLevel(value:BuildingLevel):BuildingBuilder {
        buildingLevel = value;
        return this;
    }

    public function setPos(value:Point):BuildingBuilder {
        pos = value;
        return this;
    }

    public function setOwner(value:BuildingOwner):BuildingBuilder {
        owner = value;
        return this;
    }

    public function setPopulation(value:int):BuildingBuilder {
        population = value;
        return this;
    }

    public function setStrengthened(value:Boolean):BuildingBuilder {
        strengthened = value;
        return this;
    }

    public function build():Building {
        return new Building(
                id,
                buildingType,
                buildingLevel,
                pos,
                owner,
                population,
                strengthened
        )
    }
}
}
