package ru.rknrl.castles.model.menu.main {
import ru.rknrl.dto.BuildingPrototypeDTO;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.SlotsDTO;

public class Slots {
    private var dto:SlotsDTO;

    public function Slots(dto:SlotsDTO) {
        this.dto = dto;

        var buildingsCount:int = 0;
        for each(var slot:SlotDTO in dto.slots) {
            if (slot.hasBuildingPrototype) buildingsCount++;
        }
        _buildingsCount = buildingsCount;
    }

    private var _buildingsCount:int;

    public function get buildingsCount():int {
        return _buildingsCount;
    }

    public function getSlot(slotId:SlotId):SlotDTO {
        for each(var slot:SlotDTO in dto.slots) {
            if (slot.id == slotId) return slot;
        }
        throw new Error("can't find slot by id " + slotId);
    }

    public static function equals(a:SlotDTO, b:SlotDTO):Boolean {
        if (a.hasBuildingPrototype != b.hasBuildingPrototype) return false;
        return !a.hasBuildingPrototype && !b.hasBuildingPrototype || equalsPrototype(a.buildingPrototype, b.buildingPrototype);
    }

    private static function equalsPrototype(a:BuildingPrototypeDTO, b:BuildingPrototypeDTO):Boolean {
        return a.type == b.type && a.level == b.level;
    }

    public function getEmptySlot():SlotId {
        for each(var slot:SlotDTO in dto.slots) {
            if (!slot.hasBuildingPrototype) return slot.id
        }
        return null;
    }

    public function getNotEmptySlot():SlotId {
        for each(var slot:SlotDTO in dto.slots) {
            if (slot.hasBuildingPrototype) return slot.id
        }
        throw new Error("no buildings in start location");
    }
}
}