package ru.rknrl.castles.game.view {
import flash.display.Sprite;
import flash.geom.ColorTransform;

import ru.rknrl.castles.utils.Utils;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.funnyUi.Animated;

public class BuildingBase extends Animated {
    protected var body:Sprite;

    protected function addBody(buildingType:BuildingType, buildingLevel:BuildingLevel, color:ColorTransform, scale:Number = 1):void {
        body = Utils.getBuildingBody(buildingType);
        body.scaleX = body.scaleY = Utils.getScaleByLevel(buildingLevel) * scale;
        body.transform.colorTransform = color;
        addChild(body);
    }
}
}
