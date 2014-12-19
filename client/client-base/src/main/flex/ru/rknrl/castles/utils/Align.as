package ru.rknrl.castles.utils {
import flash.display.DisplayObject;
import flash.utils.Dictionary;

public class Align {
    public static function horizontal(displayObjects:Dictionary, itemWidth:Number, gap:Number):Number {
        var x:Number = 0;
        for each(var displayObject:DisplayObject in displayObjects) {
            displayObject.x = x + itemWidth / 2;
            x += itemWidth + gap;
        }
        return x;
    }

    public static function vertical(displayObjects:Dictionary, itemHeight:Number, gap:Number):Number {
        var y:Number = 0;
        for each(var displayObject:DisplayObject in displayObjects) {
            displayObject.y = y + itemHeight / 2;
            y += itemHeight + gap;
        }
        return y;
    }
}
}
