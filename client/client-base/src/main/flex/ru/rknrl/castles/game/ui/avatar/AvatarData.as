package ru.rknrl.castles.game.ui.avatar {
import flash.display.BitmapData;

public class AvatarData {
    private var _bitmapData:BitmapData;
    private var _text:String;
    private var _color:uint;

    public function AvatarData(bitmapData:BitmapData, text:String, color:uint) {
        _bitmapData = bitmapData;
        _text = text;
        _color = color;
    }

    public function get bitmapData():BitmapData {
        return _bitmapData;
    }

    public function get text():String {
        return _text;
    }

    public function get color():uint {
        return _color;
    }
}
}
