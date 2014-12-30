package ru.rknrl.castles.view.menu.main.popups.icons {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.geom.ColorTransform;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class PopupBuildIcon extends Sprite {
    public function PopupBuildIcon(buildingType:BuildingType) {
        addChild(new Shadow());
        const building:DisplayObject = Fla.createBuilding(buildingType, BuildingLevel.LEVEL_3);
        building.transform.colorTransform = buildingTypeToColorTransform(buildingType);
        addChild(building);
    }

    private static function buildingTypeToColorTransform(buildingType:BuildingType):ColorTransform {
        switch (buildingType) {
            case BuildingType.HOUSE:
                return Colors.magentaTransform;
            case BuildingType.TOWER:
                return Colors.cyanTransform;
            case BuildingType.CHURCH:
                return Colors.redTransform;
        }
        throw new Error("unknown buildingType " + buildingType);
    }
}
}
