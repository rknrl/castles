package ru.rknrl.castles.menu.slider {

import flash.display.Sprite;

import ru.rknrl.utils.drawCircle;

public class MenuPoint extends Sprite {
    private var _id:String;

    public function get id():String {
        return _id;
    }

    public function MenuPoint(id:String, radius:int) {
        _id = id;
        _radius = radius;
        selected = false;
    }

    private var _radius:int;

    public function set radius(value:int):void {
        _radius = value;
        redrawCircle();
    }

    private var _selected:Boolean;

    public function set selected(value:Boolean):void {
        _selected = value;
        redrawCircle();
    }

    private function redrawCircle():void {
        graphics.clear();
        drawCircle(graphics, _radius, _selected ? 0xff0000 : 0xaaaaaa)
    }
}
}
