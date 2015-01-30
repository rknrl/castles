package ru.rknrl.castles.events {
import flash.events.Event;

import ru.rknrl.dto.SlotDTO;

public class SetSlotEvent extends Event {
    public static const SET_SLOT:String = "setSlot";
    private var _slotDto:SlotDTO;

    public function get slotDto():SlotDTO {
        return _slotDto;
    }

    public function SetSlotEvent(slotDto:SlotDTO) {
        super(SET_SLOT, true);
        _slotDto = slotDto;
    }
}
}
