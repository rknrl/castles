package ru.rknrl.castles.menu.screens.main.popup.upgrade {
import flash.events.Event;

import ru.rknrl.dto.SlotId;

public class RemoveEvent extends Event {
    public static const REMOVE:String = "Remove";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    public function RemoveEvent(slotId:SlotId) {
        _slotId = slotId;
        super(REMOVE);
    }
}
}
