package ru.rknrl.castles.game.view {
import flash.geom.ColorTransform;
import flash.text.TextField;

import ru.rknrl.castles.game.*;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.funnyUi.Animated;
import ru.rknrl.utils.centerize;
import ru.rknrl.utils.createTextField;

public class Building extends BuildingBase {
    private var _id:int;

    public function get id():int {
        return _id;
    }

    private var _owner:BuildingOwner = new BuildingOwner(false);

    public function get owner():BuildingOwner {
        return _owner;
    }

    private var _population:int;
    private var buildingLevel:BuildingLevel;
    private var textFieldHolder:Animated;
    private var textField:TextField;

    public function Building(id:int, x:Number, y:Number, buildingType:BuildingType, buildingLevel:BuildingLevel, population:int, colorTransform:ColorTransform, owner:BuildingOwner, strengthened:Boolean) {
        _id = id;
        this.x = x;
        this.y = y;
        this.buildingLevel = buildingLevel;
        addBody(buildingType, buildingLevel, colorTransform);

        addChild(textFieldHolder = new Animated());
        textFieldHolder.addChild(textField = createTextField(Layout.buildingCounterTextFormat));

        update(population, colorTransform, owner, strengthened);
    }

    public function update(population:int, colorTransform:ColorTransform, owner:BuildingOwner, strengthened:Boolean):void {
        if (!_owner.equals(owner)) {
            _owner = owner;
            playBounce();
        }

        body.transform.colorTransform = colorTransform;

        body.scaleX = body.scaleY = strengthened ? Utils.getScaleByLevel(buildingLevel) * Utils.strengtheningScale : Utils.getScaleByLevel(buildingLevel);

        if (_population != population) {
            _population = population;
            textField.text = population.toString();
            centerize(textField);
            textFieldHolder.playBounce();
        }
    }
}
}
