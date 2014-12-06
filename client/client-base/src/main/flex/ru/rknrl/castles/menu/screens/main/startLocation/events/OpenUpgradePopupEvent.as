package ru.rknrl.castles.menu.screens.main.startLocation.events {
import flash.events.Event;

import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.SlotId;

public class OpenUpgradePopupEvent extends Event {
    public static const OPEN_UPGRADE_POPUP:String = "openUpgradePopup";

    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    private var _buildingType:BuildingType;

    public function get buildingType():BuildingType {
        return _buildingType;
    }

    private var _buildingLevel:BuildingLevel;

    public function get buildingLevel():BuildingLevel {
        return _buildingLevel;
    }

    private var _buildingsCount:int;

    public function get buildingsCount():int {
        return _buildingsCount;
    }

    public function OpenUpgradePopupEvent(slotId:SlotId, buildingType:BuildingType, buildingLevel:BuildingLevel, buildingsCount:int) {
        _slotId = slotId;
        _buildingType = buildingType;
        _buildingLevel = buildingLevel;
        _buildingsCount = buildingsCount;
        super(OPEN_UPGRADE_POPUP);
    }
}
}
