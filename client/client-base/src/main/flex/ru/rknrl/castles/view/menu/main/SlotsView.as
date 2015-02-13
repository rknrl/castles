//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.main {
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.events.SlotClickEvent;
import ru.rknrl.castles.model.menu.main.Slots;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;

public class SlotsView extends Sprite {

    private const idToSlot:Dictionary = new Dictionary();

    public function SlotsView(slots:Slots) {
        for each(var slotId:SlotId in SlotId.values) {
            const slotDto:SlotDTO = slots.getSlot(slotId);
            const slot:SlotView = new SlotView(slotId, slotDto);
            slot.addEventListener(MouseEvent.MOUSE_DOWN, onClick);
            const pos:Point = Layout.slotPos(slotId);
            slot.x = pos.x;
            slot.y = pos.y;
            idToSlot[slotId] = slot;
            addChild(slot);
        }
    }

    public function set slots(value:Slots):void {
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
