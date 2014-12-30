package ru.rknrl.castles.view.game.area.units {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.Shadow;
import ru.rknrl.castles.view.game.GameColors;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.PlayerIdDTO;

public class UnitView extends Sprite {
    private static const textFieldBottom:int = -18;

    private var textField:TextField;

    public function UnitView(buildingType:BuildingType, buildingLevel:BuildingLevel, ownerId:PlayerIdDTO, count:int, stengthened:Boolean) {
        const shadow:Shadow = new Shadow();
        shadow.width = 16;
        shadow.height = 2;
        addChild(shadow);

        const unit:DisplayObject = Fla.unit(buildingType);
        unit.transform.colorTransform = GameColors.transformById(ownerId);
        addChild(unit);

        addChild(textField = createTextField(Fonts.unitNumber));
        textField.textColor = GameColors.colorById(ownerId);

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
