package ru.rknrl.castles.menu.screens.main.startLocation.events {
import flash.events.Event;

import ru.rknrl.dto.SlotId;

public class OpenBuildPopupEvent extends Event {
    public static const OPEN_BUILD_POPUP:String = "openBuildPopup";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    public function OpenBuildPopupEvent(slotId:SlotId) {
        _slotId = slotId;
        super(OPEN_BUILD_POPUP);
    }
}
}
