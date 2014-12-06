package ru.rknrl.castles.menu.screens.main.startLocation.objects {
import ru.rknrl.castles.game.view.BuildingBase;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.SlotId;

public class DragBuilding extends BuildingBase {
    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    public function DragBuilding(slotId:SlotId, buildingType:BuildingType, buildingLevel:BuildingLevel) {
        _slotId = slotId;
        mouseEnabled = mouseChildren = false;

        addBody(buildingType, buildingLevel, Colors.colorToTransform(Colors.magenta));
    }
}
}
