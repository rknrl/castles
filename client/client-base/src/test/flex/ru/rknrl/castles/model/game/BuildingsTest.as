package ru.rknrl.castles.model.game {
import org.flexunit.asserts.assertNull;
import org.flexunit.asserts.assertTrue;

import ru.rknrl.castles.controller.mock.DtoMock;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.PlayerIdDTO;

public class BuildingsTest {
    private static const selfId:PlayerIdDTO = DtoMock.playerIdDto(2);
    private static const b0:Building = new Building(DtoMock.buildingIdDto(0), new Point(0, 0), new BuildingOwner(false), false);
    private static const b1:Building = new Building(DtoMock.buildingIdDto(1), new Point(100, 0), new BuildingOwner(true, selfId), false);
    private static const b2:Building = new Building(DtoMock.buildingIdDto(2), new Point(0, 100), new BuildingOwner(true, DtoMock.playerIdDto(0)), true);
    private static const buildings:Buildings = new Buildings(new <Building>[b0, b1, b2]);

    [Test(description="Поиск по id возвращает верный домик")]
    public function t1():void {
        assertTrue(buildings.byId(DtoMock.buildingIdDto(1)) == b1)
    }

    [Test(expects="Error", description="Поиск по id кидает эксепшн если домика нет")]
    public function t2():void {
        buildings.byId(DtoMock.buildingIdDto(3));
    }

    [Test(description="Поиск по xy возвращает верный домик")]
    public function t3():void {
        assertTrue(buildings.inXy(new Point(0, 0)) == b0);
        assertTrue(buildings.inXy(new Point(90, 0)) == b1);
        assertTrue(buildings.inXy(new Point(10, 90)) == b2);
    }

    [Test(description="Поиск по xy возвращает null если домика нет")]
    public function t4():void {
        assertNull(buildings.inXy(new Point(200, 200)));
    }

    [Test(description="selfInXy возвращает верный домик")]
    public function t5():void {
        assertTrue(buildings.selfInXy(selfId, new Point(90, -10)) == b1);
    }

    [Test(description="selfInXy возвращает null если домика нет")]
    public function t6():void {
        assertNull(buildings.selfInXy(selfId, new Point(0, 100)));
    }
}
}
