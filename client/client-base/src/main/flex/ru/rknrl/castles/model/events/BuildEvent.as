//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.events {
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
        _slotId = slotId;
        _buildingType = buildingType;
        super(BUILD, true);
    }
}
}
