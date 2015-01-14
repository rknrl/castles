package ru.rknrl.castles.view.menu.main {
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.events.SlotClickEvent;
import ru.rknrl.castles.model.getSlotPos;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;

public class StartLocationView extends Sprite {
    private static const gapX:Number = 20;
    private static const gapY:Number = 40;

    private const idToSlot:Dictionary = new Dictionary();

    public function StartLocationView(startLocation:StartLocation) {
        for each(var slotId:SlotId in SlotId.values) {
            const slotDto:SlotDTO = startLocation.getSlot(slotId);
            const slot:SlotView = new SlotView(slotId, slotDto);
            slot.addEventListener(MouseEvent.MOUSE_DOWN, onClick);

            const pos:Point = getSlotPos(slotId);
            slot.x = pos.x * gapX;
            slot.y = pos.y * gapY;
            idToSlot[slotId] = slot;
            addChild(slot);
        }
    }

    public function set startLocation(value:StartLocation):void {
        for each(var slotId:SlotId in SlotId.values) {
            const slotDto:SlotDTO = value.getSlot(slotId);
            const slot:SlotView = idToSlot[slotId];
            slot.dto = slotDto;
        }
    }

    public function set lock(value:Boolean):void {
        for each(var slot:SlotView in idToSlot) slot.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        event.stopImmediatePropagation();
        const slot:SlotView = SlotView(event.target);
        dispatchEvent(new SlotClickEvent(slot.id));
    }
}
}
