//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.buildings {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.Animated;
import ru.rknrl.castles.view.utils.AnimatedTextField;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class BuildingView extends Sprite {
    private static const textFieldHeight:int = 24;
    private static const textFieldBottom:int = -2;
    private static const textFieldCenterY:int = textFieldBottom - textFieldHeight / 2;

    private var buildingHolder:Animated;
    private var building:DisplayObject;
    private var textField:AnimatedTextField;
    private var scale:Number;

    public function BuildingView(id:BuildingIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point) {
        _id = id;
        _pos = pos;

        mouseChildren = false;

        addChild(buildingHolder = new Animated());
        buildingHolder.addChild(new Shadow());
        buildingHolder.addChild(building = Fla.createBuilding(buildingType, BuildingLevel.LEVEL_3));

        buildingHolder.addChild(textField = new AnimatedTextField(Fonts.buildingNumber));
        textField.y = textFieldCenterY;

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

    public function set owner(value:BuildingOwner):void {
        building.transform.colorTransform = Colors.buildingTransform(value);
        if (_owner && !_owner.equals(value)) buildingHolder.bounce();
        _owner = value;
    }

    public function set count(value:int):void {
        const newText:String = value.toString();
        if (textField.text != newText) {
            textField.text = newText;
            textField.bounce();
        }
    }

    public function set strengthened(value:Boolean):void {
        scaleX = scaleY = scale * (value ? Fla.strengtheningScale : 1);
    }
}
}
