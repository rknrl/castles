//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.main {
import ru.rknrl.dto.BuildingPrototype;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;

public class Slots {
    private var dto:Vector.<SlotDTO>;

    public function Slots(dto:Vector.<SlotDTO>) {
        this.dto = dto;

        var buildingsCount:int = 0;
        for each(var slot:SlotDTO in dto) {
            if (slot.hasBuildingPrototype) buildingsCount++;
        }
        _buildingsCount = buildingsCount;
    }

    private var _buildingsCount:int;

    public function get buildingsCount():int {
        return _buildingsCount;
    }

    public function getSlot(slotId:SlotId):SlotDTO {
        for each(var slot:SlotDTO in dto) {
            if (slot.id == slotId) return slot;
        }
        throw new Error("can't find slot by id " + slotId);
    }

    public function getEmptySlot():SlotId {
        for each(var slot:SlotDTO in dto) {
            if (!slot.hasBuildingPrototype) return slot.id
        }
        return null;
    }

    public function getNotEmptySlot():SlotId {
        for each(var slot:SlotDTO in dto) {
            if (slot.hasBuildingPrototype) return slot.id
        }
        throw new Error("no buildings in start location");
    }

    public static function equals(a:SlotDTO, b:SlotDTO):Boolean {
        if (a.hasBuildingPrototype != b.hasBuildingPrototype) return false;
        return !a.hasBuildingPrototype && !b.hasBuildingPrototype || equalsPrototype(a.buildingPrototype, b.buildingPrototype);
    }

    public static function equalsPrototype(a:BuildingPrototype, b:BuildingPrototype):Boolean {
        return a.buildingType == b.buildingType && a.buildingLevel == b.buildingLevel;
    }
}
}
