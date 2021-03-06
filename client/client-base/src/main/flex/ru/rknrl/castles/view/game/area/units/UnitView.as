//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.units {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.Shadow;
import protos.BuildingLevel;
import protos.BuildingType;
import protos.PlayerId;
import ru.rknrl.display.createTextField;

public class UnitView extends Sprite {
    private static const textFieldBottom:int = -18;

    private var textField:TextField;

    public function UnitView(buildingType:BuildingType, buildingLevel:BuildingLevel, ownerId:PlayerId, count:int, stengthened:Boolean) {
        const shadow:Shadow = new Shadow();
        shadow.width = 16;
        shadow.height = 2;
        addChild(shadow);

        const unit:DisplayObject = Fla.unit(buildingType, buildingLevel);
        unit.transform.colorTransform = Colors.playerTransform(ownerId);
        addChild(unit);

        addChild(textField = createTextField(Fonts.unitNumber));
        textField.textColor = Colors.playerColor(ownerId);

        const scale:Number = Fla.buildingLevelToScale(buildingLevel);
        scaleX = scaleY = stengthened ? scale * Fla.strengtheningScale : scale;

        this.count = count;
    }

    public function set count(value:int):void {
        textField.text = value.toString();
        textField.x = -textField.width / 2;
        textField.y = textFieldBottom - textField.height;
    }
}
}
