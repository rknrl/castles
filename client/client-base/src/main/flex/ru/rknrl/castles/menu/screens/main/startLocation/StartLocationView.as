package ru.rknrl.castles.menu.screens.main.startLocation {
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.utils.Dictionary;

import ru.rknrl.castles.menu.screens.main.startLocation.events.OpenBuildPopupEvent;
import ru.rknrl.castles.menu.screens.main.startLocation.events.OpenUpgradePopupEvent;
import ru.rknrl.castles.menu.screens.main.startLocation.events.SwapEvent;
import ru.rknrl.castles.menu.screens.main.startLocation.objects.DragBuilding;
import ru.rknrl.castles.menu.screens.main.startLocation.objects.MenuBuilding;
import ru.rknrl.castles.menu.screens.main.startLocation.objects.MenuGround;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.StartLocationDTO;

public class StartLocationView extends Sprite {
    private var groundLayer:Sprite;
    private var buildingLayer:Sprite;

    private var grounds:Vector.<MenuGround> = new <MenuGround>[];

    public function StartLocationView(startLocation:StartLocationDTO, layout:Layout) {
        addChild(groundLayer = new Sprite());

        addChild(buildingLayer = new Sprite());

        const cellSize:int = CellSize.SIZE.id() * layout.menuBuildingScale;

        for each(var id:SlotId in SlotId.values) {
            const pos:Point = Utils.slotsPositions[id];
            const x:int = pos.x * cellSize;
            const y:int = pos.y * cellSize + cellSize / 2;

            const ground:MenuGround = new MenuGround(id, cellSize, cellSize);
            ground.addEventListener(MouseEvent.MOUSE_DOWN, onGroundMouseDown);
            grounds.push(ground);
            groundLayer.addChild(ground);
            idToGround[id] = ground;
            ground.x = x;
            ground.y = y;

            const building:MenuBuilding = new MenuBuilding(id, layout.menuBuildingScale);
            building.addEventListener(MouseEvent.MOUSE_DOWN, onBuildingMouseDown);
            buildingLayer.addChild(building);

            idToBuilding[id] = building;
            building.x = x;
            building.y = y;

            idToPosition[id] = new Point(x, y);
        }

        this.startLocation = startLocation;
    }

    private const idToBuilding:Dictionary = new Dictionary();
    private const idToGround:Dictionary = new Dictionary();
    private const idToPosition:Dictionary = new Dictionary();

    private var buildingsCount:int;

    public function set startLocation(value:StartLocationDTO):void {
        buildingsCount = 0;

        for each(var slot:SlotDTO in value.slots) {
            const id:SlotId = slot.id;
            const building:MenuBuilding = idToBuilding[id];
            if (!building) throw new Error("no building for id " + id);

            if (building.differentWith(slot)) unlockSlot(id);

            building.update(slot);
            if (slot.hasBuildingPrototype) buildingsCount++;
        }
    }

    public function set color(value:uint):void {
        for each(var building:MenuBuilding in idToBuilding) {
            building.color = value;
        }
    }

    // mouse down

    private function onGroundMouseDown(event:MouseEvent):void {
        event.stopImmediatePropagation();

        const ground:MenuGround = MenuGround(event.target);

        const building:MenuBuilding = idToBuilding[ground.slotId];
        if (building.hasBuilding) {
            startBuilding(ground.slotId);
        } else {
            startGround(ground.slotId);
        }
    }

    private function onBuildingMouseDown(event:MouseEvent):void {
        event.stopImmediatePropagation();

        const building:MenuBuilding = MenuBuilding(event.target);
        startBuilding(building.slotId);
    }

    // mouse

    private var startMouseX:int;
    private var startMouseY:int;

    private function isClick():Boolean {
        return Math.abs(mouseX - startMouseX) < 32 && Math.abs(mouseY - startMouseY) < 32;
    }

    // mouse & ground

    private var downSlotId:SlotId;

    private function startGround(slotId:SlotId):void {
        downSlotId = slotId;
        startMouseX = mouseX;
        startMouseY = mouseY;
        stage.addEventListener(MouseEvent.MOUSE_UP, onGroundMouseUp);
    }

    private function onGroundMouseUp(event:MouseEvent):void {
        stage.removeEventListener(MouseEvent.MOUSE_UP, onGroundMouseUp);

        if (isClick()) {
            dispatchEvent(new OpenBuildPopupEvent(downSlotId));
        }
    }

    // mouse & building

    private var dragBuilding:DragBuilding;
    private var startBuildingX:int;
    private var startBuildingY:int;

    private function startBuilding(slotId:SlotId):void {
        const building:MenuBuilding = idToBuilding[slotId];
        dragBuilding = new DragBuilding(building.slotId, building.buildingType, building.buildingLevel);
        addChild(dragBuilding);

        startBuildingX = building.x;
        startBuildingY = building.y;
        dragBuilding.x = building.x;
        dragBuilding.y = building.y;
        startMouseX = mouseX;
        startMouseY = mouseY;
        stage.addEventListener(MouseEvent.MOUSE_MOVE, onBuildingMouseMove);
        stage.addEventListener(MouseEvent.MOUSE_UP, onBuildingMouseUp);
    }

    private function onBuildingMouseMove(event:MouseEvent):void {
        dragBuilding.x = mouseX - (startMouseX - startBuildingX);
        dragBuilding.y = mouseY - (startMouseY - startBuildingY);
    }

    private function onBuildingMouseUp(event:MouseEvent):void {
        if (isClick()) {
            const building:MenuBuilding = idToBuilding[dragBuilding.slotId];
            dispatchEvent(new OpenUpgradePopupEvent(building.slotId, building.buildingType, building.buildingLevel, buildingsCount));
        } else {
            const slotId:SlotId = getNearestSlotId(mouseX, mouseY);
            if (slotId && dragBuilding.slotId != slotId) {
                dispatchEvent(new SwapEvent(dragBuilding.slotId, slotId))
            }
        }
        removeChild(dragBuilding);
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onBuildingMouseMove);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onBuildingMouseUp);
    }

    public function removeListeners():void {
        //todo stage == null
        stage.removeEventListener(MouseEvent.MOUSE_UP, onGroundMouseUp);
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onBuildingMouseMove);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onBuildingMouseUp);
    }

    private function getNearestSlotId(x:int, y:int):SlotId {
        for (var i:* in idToPosition) {
            const id:SlotId = i;
            const point:Point = idToPosition[id];
            if (distance(x, y, point) < 32) return id;
        }
        return null;
    }

    private static function distance(x:int, y:int, pos:Point):int {
        const dx:int = pos.x - x;
        const dy:int = pos.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public function lockSlot(slotId:SlotId):void {
        MenuGround(idToGround[slotId]).lock();
        MenuBuilding(idToBuilding[slotId]).lock();
    }

    public function unlockSlot(slotId:SlotId):void {
        MenuGround(idToGround[slotId]).unlock();
        MenuBuilding(idToBuilding[slotId]).unlock();
    }
}
}

