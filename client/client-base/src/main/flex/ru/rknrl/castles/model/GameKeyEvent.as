package ru.rknrl.castles.model {
import flash.events.Event;

public class GameKeyEvent extends Event {
    public static const KEY:String = "gameKey";

    private var _keyCode:int;

    public function get keyCode():int {
        return _keyCode;
    }

    public function GameKeyEvent(keyCode:int) {
        _keyCode = keyCode;
        super(KEY);
    }
}
}
