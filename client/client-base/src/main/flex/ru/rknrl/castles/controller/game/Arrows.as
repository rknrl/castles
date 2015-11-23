//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {

import protos.BuildingId;

import flash.utils.Dictionary;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.view.game.area.arrows.ArrowView;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.core.points.Point;

public class Arrows {
    private var layer:ArrowsView;

    public function Arrows(layer:ArrowsView) {
        this.layer = layer;
    }

    private var _drawing:Boolean;

    public function get drawing():Boolean {
        return _drawing;
    }

    private var fromBuildingIdToView:Dictionary = new Dictionary();

    public function getFromBuildingsIds():Vector.<BuildingId> {
        const result:Vector.<BuildingId> = new <BuildingId>[];
        for (var key:* in fromBuildingIdToView) {
            const id:int = key;
            result.push(DtoMock.buildingId(id));
        }
        return result;
    }

    public function startDraw(fromBuildingId:int, fromPos:Point):void {
        _drawing = true;
        addArrow(fromBuildingId, fromPos);
    }

    public function hasArrow(fromBuildingId:int):Boolean {
        return fromBuildingIdToView[fromBuildingId];
    }

    public function addArrow(fromBuildingId:int, fromPos:Point):void {
        if (!fromBuildingIdToView[fromBuildingId]) {
            fromBuildingIdToView[fromBuildingId] = layer.addArrow(fromPos);
        }
    }

    public function removeArrow(fromBuildingId:int):void {
        const view:ArrowView = fromBuildingIdToView[fromBuildingId];
        layer.removeArrow(view);
        delete fromBuildingIdToView[fromBuildingId];
    }

    public function mouseMove(mousePos:Point):void {
        layer.orientArrows(mousePos);
    }

    public function endDraw():void {
        _drawing = false;
        fromBuildingIdToView = new Dictionary();
        layer.removeArrows();
    }
}
}
