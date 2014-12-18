package ru.rknrl.castles.menu.screens.top {
import flash.display.BitmapData;

public class TopAvatarData {
    private var _bitmapData:BitmapData;
    private var _text:String;

    public function TopAvatarData(bitmapData:BitmapData, text:String) {
        _bitmapData = bitmapData;
        _text = text;
    }

    public function get bitmapData():BitmapData {
        return _bitmapData;
    }

    public function get text():String {
        return _text;
    }
}
}
