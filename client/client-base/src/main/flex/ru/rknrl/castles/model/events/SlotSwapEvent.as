package ru.rknrl.castles.model.events {
import flash.events.Event;

import ru.rknrl.dto.SlotId;

public class SlotSwapEvent extends Event {
    public static const SLOT_SWAP:String = "slotSwap";

    private var _slotId1:SlotId;

    public function get slotId1():SlotId {
        return _slotId1;
    }

    private var _slotId2:SlotId;

    public function get slotId2():SlotId {
        return _slotId2;
    }

    public function SlotSwapEvent(slotId1:SlotId, slotId2:SlotId) {
        _slotId1 = slotId1;
        _slotId2 = slotId2;
        super(SLOT_SWAP, true);
    }
}
}
