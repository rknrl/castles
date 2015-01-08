package ru.rknrl.castles.view.game.area.buildings {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class BuildingView extends Sprite {
    private static const textFieldBottom:int = -2;

    private var building:DisplayObject;
    private var textField:TextField;
    private var scale:Number;

    public function BuildingView(id:BuildingIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point) {
        _id = id;
        _pos = pos;
        addChild(new Shadow());
        addChild(building = Fla.createBuilding(buildingType, BuildingLevel.LEVEL_3));
        addChild(textField = createTextField(Fonts.buildingNumber));

        scale = Fla.buildingLevelToScale(buildingLevel);

        this.owner = owner;
        this.count = count;
        this.strengthened = strengthened;
    }

    private var _id:BuildingIdDTO;

    public function get id():BuildingIdDTO {
        return _id;
    }

    private var _pos:Point;

    public function get pos():Point {
        return _pos;
    }

    private var _owner:BuildingOwner;

    public function get owner():BuildingOwner {
        return _owner;
    }

    public function set owner(value:BuildingOwner):void {
        _owner = value;
        building.transform.colorTransform = Colors.buildingTransform(owner);
    }

    public function set count(value:int):void {
        textField.text = value.toString();
        textField.x = -textField.width / 2;
        textField.y = textFieldBottom - textField.height;
    }

    public function set strengthened(value:Boolean):void {
        scaleX = scaleY = scale * (value ? Fla.strengtheningScale : 1);
    }
}
}
