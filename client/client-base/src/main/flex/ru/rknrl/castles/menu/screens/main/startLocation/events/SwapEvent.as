package ru.rknrl.castles.menu.screens.main.startLocation.events {
import flash.events.Event;

import ru.rknrl.dto.SlotId;

public class SwapEvent extends Event {
    public static const SWAP_SLOTS:String = "swapSlots";

    private var _slotId1:SlotId;

    public function get slotId1():SlotId {
        return _slotId1;
    }

    private var _slotId2:SlotId;

    public function get slotId2():SlotId {
        return _slotId2;
    }

    public function SwapEvent(slotId1:SlotId, slotId2:SlotId) {
        _slotId1 = slotId1;
        _slotId2 = slotId2;
        super(SWAP_SLOTS);
    }
}
}
