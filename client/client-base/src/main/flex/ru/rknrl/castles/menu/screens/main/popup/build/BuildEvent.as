package ru.rknrl.castles.menu.screens.main.popup.build {
import flash.events.Event;

import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.SlotId;

public class BuildEvent extends Event {
    public static const BUILD:String = "build";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    private var _buildingType:BuildingType;

    public function get buildingType():BuildingType {
        return _buildingType;
    }

    public function BuildEvent(slotId:SlotId, buildingType:BuildingType) {
        super(BUILD);
        _slotId = slotId;
        _buildingType = buildingType;
    }
}
}