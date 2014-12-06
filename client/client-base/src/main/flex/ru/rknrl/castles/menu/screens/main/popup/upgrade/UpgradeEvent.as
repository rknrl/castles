package ru.rknrl.castles.menu.screens.main.popup.upgrade {
import flash.events.Event;

import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.SlotId;

public class UpgradeEvent extends Event {
    public static const UPGRADE:String = "Upgrade";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    private var _toLevel:BuildingLevel;


    public function get toLevel():BuildingLevel {
        return _toLevel;
    }

    public function UpgradeEvent(slotId:SlotId, toLevel:BuildingLevel) {
        _slotId = slotId;
        _toLevel = toLevel;
        super(UPGRADE);
    }
}
}
