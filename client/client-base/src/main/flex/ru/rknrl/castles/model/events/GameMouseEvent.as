package ru.rknrl.castles.model.events {
import flash.events.Event;

import ru.rknrl.castles.utils.points.Point;

public class GameMouseEvent extends Event {
    public static const MOUSE_DOWN:String = "gameMouseDown";
    public static const MOUSE_MOVE:String = "gameMouseMove";
    public static const MOUSE_UP:String = "gameMouseUp";

    private var _mousePos:Point;

    public function get mousePos():Point {
        return _mousePos;
    }

    public function GameMouseEvent(type:String, mousePos:Point) {
        super(type);
        _mousePos = mousePos;
    }
}
}
