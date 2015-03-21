//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {

import flash.utils.Dictionary;

import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.arrows.ArrowView;
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

    private var fromBuildingIdToArrow:Dictionary = new Dictionary();

    public function getFromBuildingsIds():Vector.<BuildingIdDTO> {
        const result:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];
        for (var id:BuildingIdDTO in fromBuildingIdToArrow)
            result.push(id);
        return result;
    }

    public function startDraw(fromBuilding:Building):void {
        _drawing = true;
        addArrow(fromBuilding);
    }

    private function getArrow(fromBuildingId:BuildingIdDTO):ArrowView {
        for (var id:BuildingIdDTO in fromBuildingIdToArrow)
            if (id.id == fromBuildingId.id) return fromBuildingIdToArrow[id];
        return null;
    }

    public function hasArrow(fromBuildingId:BuildingIdDTO):Boolean {
        return getArrow(fromBuildingId);
    }

    public function addArrow(fromBuilding:Building):void {
        if (!hasArrow(fromBuilding.id))
            fromBuildingIdToArrow[fromBuilding.id] = view.addArrow(fromBuilding.pos);
    }

    public function removeArrow(fromBuildingId:BuildingIdDTO):void {
        view.removeArrow(getArrow(fromBuildingId));
    }

    public function mouseMove(mousePos:Point):void {
        view.orientArrows(mousePos);
    }

    public function endDraw():void {
        _drawing = false;
        fromBuildingIdToArrow = new Dictionary();
        view.removeArrows();
    }
}
}
