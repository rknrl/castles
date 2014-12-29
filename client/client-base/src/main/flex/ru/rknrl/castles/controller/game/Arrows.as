package ru.rknrl.castles.controller.game {

import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.dto.BuildingIdDTO;

public class Arrows {
    private var view:ArrowsView;

    public function Arrows(view:ArrowsView) {
        this.view = view;
    }

    private var _drawing:Boolean;

    public function get drawing():Boolean {
        return _drawing;
    }

    private const _fromBuildingIds:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];

    public function get fromBuildingIds():Vector.<BuildingIdDTO> {
        return _fromBuildingIds;
    }

    private function exists(buildingId:BuildingIdDTO):Boolean {
        for each(var id:BuildingIdDTO in _fromBuildingIds) {
            if (buildingId.id == id.id) return true;
        }
        return false;
    }

    public function startDraw(fromBuilding:Building):void {
        _drawing = true;
        addArrow(fromBuilding);
    }

    public function addArrow(fromBuilding:Building):void {
        if (exists(fromBuilding.id)) return;

        _fromBuildingIds.push(fromBuilding.id);

        view.addArrow(fromBuilding.pos);
    }

    public function mouseMove(mousePos:Point):void {
        view.orientArrows(mousePos);
    }

    public function endDraw():void {
        _drawing = false;
        _fromBuildingIds.length = 0;
        view.removeArrows();
    }
}
}
