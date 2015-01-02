package ru.rknrl.castles.model.events {
import flash.events.Event;

import ru.rknrl.dto.SlotId;

public class UpgradeBuildingEvent extends Event {
    public static const UPGRADE_BUILDING:String = "upgradeBuilding";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    public function UpgradeBuildingEvent(slotId:SlotId) {
        _slotId = slotId;
        super(UPGRADE_BUILDING, true);
    }
}
}