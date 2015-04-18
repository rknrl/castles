//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertNull;

import ru.rknrl.castles.kit.BuildingBuilder;
import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.core.kit.assertVectors;
import ru.rknrl.core.points.Point;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class BuildingsTest {
    private const b0:Building = new BuildingBuilder()
            .setId(DtoMock.buildingId(0))
            .setOwner(new BuildingOwner(true, DtoMock.playerId(0)))
            .setPos(new Point(2, 2))
            .setBuildingType(BuildingType.CHURCH)
            .build();

    private const b1:Building = new BuildingBuilder()
            .setId(DtoMock.buildingId(1))
            .setOwner(new BuildingOwner(true, DtoMock.playerId(1)))
            .setPos(new Point(3, 3))
            .setBuildingType(BuildingType.HOUSE)
            .build();

    private const b2:Building = new BuildingBuilder()
            .setId(DtoMock.buildingId(2))
            .setOwner(new BuildingOwner(true, DtoMock.playerId(1)))
            .setPos(new Point(64, 64))
            .setBuildingType(BuildingType.TOWER)
            .setBuildingLevel(BuildingLevel.LEVEL_1)
            .build();

    private const b3:Building = new BuildingBuilder()
            .setId(DtoMock.buildingId(3))
            .setOwner(new BuildingOwner(false))
            .setPos(new Point(4, 4))
            .setBuildingType(BuildingType.TOWER)
            .setBuildingLevel(BuildingLevel.LEVEL_2)
            .build();

    private const buildings:Buildings = new Buildings(new <Building>[b0, b1, b2, b3]);

    [Test("by id")]
    public function t0():void {
        assertEquals(b0, buildings.byId(DtoMock.buildingId(0)));
        assertEquals(b1, buildings.byId(DtoMock.buildingId(1)));
    }

    [Test("not found by id", expects="Error")]
    public function t1():void {
        const buildings:Buildings = new Buildings(new <Building>[]);
        buildings.byId(DtoMock.buildingId(0));
    }

    [Test("inRadius")]
    public function t2():void {
        const inRadius:Vector.<Building> = buildings.inRadius(new Point(0, 0), 10);
        assertVectors(new <*>[b0, b1, b3], Vector.<*>(inRadius));

        const inRadius2:Vector.<Building> = buildings.inRadius(new Point(0, 0), 1);
        assertEquals(0, inRadius2.length);
    }

    [Test("inXy")]
    public function t4():void {
        assertEquals(b0, buildings.inXy(new Point(0, 0)));
        assertNull(buildings.inXy(new Point(1000, 1000)));
    }

    [Test("selfInXy")]
    public function t6():void {
        assertEquals(b0, buildings.selfInXy(DtoMock.playerId(0), new Point(0, 0)));
        assertNull(buildings.selfInXy(DtoMock.playerId(1), new Point(0, 0)));
    }

    [Test("notPlayerId")]
    public function t7():void {
        assertVectors(new <*>[b0.id, b3.id], Vector.<*>(buildings.notPlayerId(DtoMock.playerId(1))));
        assertVectors(new <*>[b1.id, b2.id, b3.id], Vector.<*>(buildings.notPlayerId(DtoMock.playerId(0))));
        assertVectors(new <*>[b0.id, b1.id, b2.id, b3.id], Vector.<*>(buildings.notPlayerId(DtoMock.playerId(2))));
    }

    [Test("owned")]
    public function t8():void {
        assertVectors(new <*>[b0.id, b1.id, b2.id], Vector.<*>(buildings.owned()));
    }

    [Test("myTower")]
    public function t9():void {
        assertEquals(b2.id, buildings.myTower(DtoMock.playerId(1)));
        assertNull(buildings.myTower(DtoMock.playerId(0)));
    }

    [Test("bigTower")]
    public function t10():void {
        assertEquals(b3.id, buildings.bigTower(DtoMock.playerId(1)));
        assertEquals(b3.id, buildings.bigTower(DtoMock.playerId(0)));
    }
}
}
