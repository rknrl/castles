package ru.rknrl.castles.view.menu.main {
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.events.SlotClickEvent;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;

public class StartLocationView extends Sprite {
    private static const topGapX:Number = 21;
    private static const bottomGapX:Number = 42;
    private static const gapY:Number = 40;

    private const idToSlot:Dictionary = new Dictionary();

    public function StartLocationView(startLocation:StartLocation) {
        for each(var slotId:SlotId in SlotId.values) {
            const slotDto:SlotDTO = startLocation.getSlot(slotId);
            const slot:SlotView = new SlotView(slotId, slotDto);
            slot.addEventListener(MouseEvent.CLICK, onClick);
            slot.x = slotIdToPos(slotId).x;
            slot.y = slotIdToPos(slotId).y;
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

    private static function slotIdToPos(slotId:SlotId):Point {
        switch (slotId) {
            case SlotId.SLOT_1:
                return new Point(-topGapX, -gapY);
            case SlotId.SLOT_2:
                return new Point(topGapX, -gapY);
            case SlotId.SLOT_3:
                return new Point(-bottomGapX, 0);
            case SlotId.SLOT_4:
                return new Point(0, 0);
            case SlotId.SLOT_5:
                return new Point(bottomGapX, 0);

        }
        throw new Error("unknown slotId " + slotId);
    }

    public function set lock(value:Boolean):void {
        for each(var slot:SlotView in idToSlot) slot.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        const slot:SlotView = SlotView(event.target);
        dispatchEvent(new SlotClickEvent(slot.id));
    }
}
}
