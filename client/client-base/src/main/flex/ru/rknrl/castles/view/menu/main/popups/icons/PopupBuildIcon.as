//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.main.popups.icons {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.utils.Shadow;
import protos.BuildingLevel;
import protos.BuildingType;

public class PopupBuildIcon extends Sprite {
    public function PopupBuildIcon(buildingType:BuildingType) {
        addChild(new Shadow());
        const building:DisplayObject = Fla.createBuilding(buildingType, BuildingLevel.LEVEL_3);
        building.transform.colorTransform = Colors.transform(buildingTypeToColor(buildingType));
        addChild(building);
    }

    private static function buildingTypeToColor(buildingType:BuildingType):uint {
        switch (buildingType) {
            case BuildingType.HOUSE:
                return Colors.magenta;
            case BuildingType.TOWER:
                return Colors.cyan;
            case BuildingType.CHURCH:
                return Colors.red;
        }
        throw new Error("unknown buildingType " + buildingType);
    }
}
}
