package ru.rknrl.castles.game.view {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.geom.ColorTransform;
import flash.geom.Point;

import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.funnyUi.Animated;

public class Unit extends Sprite {
    public static const unitCounterY:Number = -30;

    private var _id:int;

    public function get id():int {
        return _id;
    }

    private var _startX:Number;

    public function get startX():Number {
        return _startX;
    }

    private var _startY:Number;

    public function get startY():Number {
        return _startY;
    }

    private var _endX:Number;

    public function get endX():Number {
        return _endX;
    }

    private var _endY:Number;

    public function get endY():Number {
        return _endY;
    }

    private var toLeft:Boolean;

    private var _startTime:int;

    public function get startTime():int {
        return _startTime;
    }

    private var _speed:Number;

    private var _buildingType:BuildingType;

    public function getPos(time:int):Point {
        const dx:Number = _endX - _startX;
        const dy:Number = _endY - _startY;
        const distance:Number = Math.sqrt(dx * dx + dy * dy);
        const duration:int = (distance / _speed);
        const deltaTime:int = time - _startTime;
        var progress:Number = deltaTime / duration;
        if (progress > 1) progress = 1;
        return new Point(_startX + dx * progress, _startY + dy * progress);
    }

    private var angle:Number;

    public function Unit(id:int, startX:Number, startY:Number, endX:Number, endY:Number, startTime:int, buildingType:BuildingType, count:int, colorTransform:ColorTransform, speed:Number, strengthened:Boolean) {
        _id = id;
        _startX = startX;
        _startY = startY;
        _endX = endX;
        _endY = endY;
        _startTime = startTime;

        angle = Math.atan2(endY - startY, startX - endX);

        toLeft = endX < startX;
        _buildingType = buildingType;
        _speed = speed;

        body = Utils.getUnitBody(_buildingType);
        body.transform.colorTransform = colorTransform;
        body.scaleX = body.scaleY = strengthened ? 3 : 2.5;
        addChild(body);

        addChild(textFieldHolder = createTextHolder());

        textFieldHolder.addChild(textField = createTextField(Layout.unitCounterTextFormat));

        this.count = count;
    }

    private var body:DisplayObject;

    public function update(time:int):void {
        body.rotation = Math.sin(time / 100) * 10;
    }

    private var textFieldHolder:Animated;
    private var textField:Label;

    private static function createTextHolder():Animated {
        const textFieldHolder:Animated = new Animated();
        textFieldHolder.y = unitCounterY;
        return textFieldHolder;
    }

    private function set count(value:int):void {
        textField.text = value.toString();
        textField.x = -textField.width / 2;
        textFieldHolder.playBounce();
    }
}
}
