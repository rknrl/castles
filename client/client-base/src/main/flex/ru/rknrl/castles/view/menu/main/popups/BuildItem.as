//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.main.popups {
import flash.display.DisplayObject;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.popups.popup.PopupItem;
import protos.BuildingType;

public class BuildItem extends PopupItem {
    private var _buildingType:BuildingType;

    public function get buildingType():BuildingType {
        return _buildingType;
    }

    public function BuildItem(buildingType:BuildingType, layout:Layout, icon:DisplayObject, text:String, price:int) {
        super(layout, icon, text, price);
        _buildingType = buildingType;
    }
}
}
