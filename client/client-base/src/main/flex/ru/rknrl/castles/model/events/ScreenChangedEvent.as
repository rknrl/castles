package ru.rknrl.castles.model.events {
import flash.events.Event;

public class ScreenChangedEvent extends Event {
    public static const SCREEN_CHANGED:String = "screenChanged";

    // todo: индексы никак не синхронизированы с скрин навигатором
    public static const ALL:Vector.<int> = new <int>[SCREEN_MAIN, SCREEN_TOP, SCREEN_SHOP, SCREEN_SKILLS, SCREEN_BANK];
    public static const SCREEN_MAIN:int = 0;
    public static const SCREEN_TOP:int = 1;
    public static const SCREEN_SHOP:int = 2;
    public static const SCREEN_SKILLS:int = 3;
    public static const SCREEN_BANK:int = 4;

    private var _screenIndex:int;

    public function get screenIndex():int {
        return _screenIndex;
    }

    public function ScreenChangedEvent(screenIndex:int) {
        _screenIndex = screenIndex;
        super(SCREEN_CHANGED, true)
    }
}
}
