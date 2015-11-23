//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.events {
import flash.events.Event;

import protos.Slot;

public class SetSlotEvent extends Event {
    public static const SET_SLOT:String = "setSlot";
    private var _slotDto:Slot;

    public function get slotDto():Slot {
        return _slotDto;
    }

    public function SetSlotEvent(slotDto:Slot) {
        super(SET_SLOT, true);
        _slotDto = slotDto;
    }
}
}
