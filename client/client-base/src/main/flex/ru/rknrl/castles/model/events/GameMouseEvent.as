//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.events {
import flash.events.Event;

import ru.rknrl.castles.model.points.Point;

public class GameMouseEvent extends Event {
    public static const MOUSE_DOWN:String = "gameMouseDown";
    public static const ENTER_FRAME:String = "gameEnterFrame";
    public static const MOUSE_UP:String = "gameMouseUp";

    private var _mousePos:Point;

    public function get mousePos():Point {
        return _mousePos;
    }

    public function GameMouseEvent(type:String, mousePos:Point) {
        _mousePos = mousePos;
        super(type);
    }
}
}
