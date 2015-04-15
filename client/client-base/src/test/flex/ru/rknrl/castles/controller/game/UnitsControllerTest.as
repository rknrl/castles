//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.display.Sprite;

import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.view.game.area.units.BloodView;
import ru.rknrl.castles.view.game.area.units.UnitKill;
import ru.rknrl.castles.view.game.area.units.UnitView;
import ru.rknrl.core.points.Point;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitUpdateDTO;

public class UnitsControllerTest {
    [Test("add")]
    public function t1():void {
        const unitsLayer:Sprite = new Sprite();
        const bloodView:BloodView = new BloodView();
        const unitsController:UnitsController = new UnitsController(unitsLayer, bloodView);

        const unitDto:UnitDTO = new UnitDTO();
        unitDto.id = DtoMock.unitId(0);
        unitDto.buildingType = BuildingType.TOWER;
        unitDto.count = 33;
        unitDto.pos = DtoMock.point(1, 1);
        unitDto.duration = 10;
        unitDto.targetBuildingId = DtoMock.buildingId(3);
        unitDto.owner = DtoMock.playerId(1);
        unitDto.strengthened = true;

        const endPos:Point = new Point(2, 3);

        unitsController.addUnit(2, endPos, unitDto);

        assertEquals(1, unitsLayer.numChildren);
        const unitView:UnitView = UnitView(unitsLayer.getChildAt(0));
        const unit:Unit = unitsController.getUnit(DtoMock.unitId(0));
        assertEquals(33, unit.count);
        assertEquals(1, unit.owner.id);

        unitsController.update(12);
        assertEquals(0, unitsLayer.numChildren);
        assertEquals(null, unitsController.getUnit(DtoMock.unitId(0)));
    }

    [Test("add twice", expects="Error")]
    public function t2():void {
        const unitsLayer:Sprite = new Sprite();
        const bloodView:BloodView = new BloodView();
        const unitsController:UnitsController = new UnitsController(unitsLayer, bloodView);

        const unitDto:UnitDTO = new UnitDTO();
        unitDto.id = DtoMock.unitId(0);
        unitDto.buildingType = BuildingType.TOWER;
        unitDto.count = 33;
        unitDto.pos = DtoMock.point(1, 1);
        unitDto.duration = 10;
        unitDto.targetBuildingId = DtoMock.buildingId(3);
        unitDto.owner = DtoMock.playerId(1);
        unitDto.strengthened = true;

        const endPos:Point = new Point(2, 3);

        unitsController.addUnit(2, endPos, unitDto);
        unitsController.addUnit(2, endPos, unitDto);
    }

    [Test("update & blood")]
    public function t3():void {
        const unitsLayer:Sprite = new Sprite();
        const bloodView:BloodView = new BloodView();
        const unitsController:UnitsController = new UnitsController(unitsLayer, bloodView);

        const unitDto:UnitDTO = new UnitDTO();
        unitDto.id = DtoMock.unitId(0);
        unitDto.buildingType = BuildingType.TOWER;
        unitDto.count = 33;
        unitDto.pos = DtoMock.point(1, 1);
        unitDto.duration = 10;
        unitDto.targetBuildingId = DtoMock.buildingId(3);
        unitDto.owner = DtoMock.playerId(1);
        unitDto.strengthened = true;

        const endPos:Point = new Point(2, 3);

        unitsController.addUnit(2, endPos, unitDto);

        const updateDto:UnitUpdateDTO = new UnitUpdateDTO();
        updateDto.id = DtoMock.unitId(0);
        updateDto.count = 30;

        unitsController.updateUnit(4, updateDto);

        const unit:Unit = unitsController.getUnit(DtoMock.unitId(0));
        assertEquals(30, unit.count);
        assertEquals(1, unit.owner.id);

        assertEquals(1, bloodView.numChildren);
        const unitKill:UnitKill = UnitKill(bloodView.getChildAt(0));
        assertEquals(unit.pos(4).x, unitKill.x);
        assertEquals(unit.pos(4).y, unitKill.y);
    }

    [Test("update unknown unit")]
    public function t4():void {
        const unitsLayer:Sprite = new Sprite();
        const bloodView:BloodView = new BloodView();
        const unitsController:UnitsController = new UnitsController(unitsLayer, bloodView);

        const updateDto:UnitUpdateDTO = new UnitUpdateDTO();
        updateDto.id = DtoMock.unitId(0);
        updateDto.count = 30;

        unitsController.updateUnit(4, updateDto);
    }

    [Test("kill & blood")]
    public function t5():void {
        const unitsLayer:Sprite = new Sprite();
        const bloodView:BloodView = new BloodView();
        const unitsController:UnitsController = new UnitsController(unitsLayer, bloodView);

        const unitDto:UnitDTO = new UnitDTO();
        unitDto.id = DtoMock.unitId(0);
        unitDto.buildingType = BuildingType.TOWER;
        unitDto.count = 33;
        unitDto.pos = DtoMock.point(1, 1);
        unitDto.duration = 10;
        unitDto.targetBuildingId = DtoMock.buildingId(3);
        unitDto.owner = DtoMock.playerId(1);
        unitDto.strengthened = true;

        const endPos:Point = new Point(2, 3);

        unitsController.addUnit(2, endPos, unitDto);
        const unit:Unit = unitsController.getUnit(DtoMock.unitId(0));

        unitsController.kill(4, DtoMock.unitId(0));

        assertEquals(0, unitsLayer.numChildren);
        assertEquals(null, unitsController.getUnit(DtoMock.unitId(0)));

        assertEquals(1, bloodView.numChildren);
        const unitKill:UnitKill = UnitKill(bloodView.getChildAt(0));
        assertEquals(unit.pos(4).x, unitKill.x);
        assertEquals(unit.pos(4).y, unitKill.y);
    }

    [Test("kill unknown unit")]
    public function t6():void {
        const unitsLayer:Sprite = new Sprite();
        const bloodView:BloodView = new BloodView();
        const unitsController:UnitsController = new UnitsController(unitsLayer, bloodView);

        unitsController.kill(4, DtoMock.unitId(0));
    }

}
}
