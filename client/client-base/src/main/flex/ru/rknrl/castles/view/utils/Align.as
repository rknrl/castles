package ru.rknrl.castles.view.utils {
import flash.display.DisplayObject;

public class Align {
    /**
     * @return total width
     */
    public static function horizontal(displayObjects:Vector.<DisplayObject>, itemWidth:Number, gap:Number):Number {
        var x:Number = 0;
        for each(var displayObject:DisplayObject in displayObjects) {
            displayObject.x = x + itemWidth / 2;
            x += itemWidth + gap;
        }
        return x - gap;
    }
}
}
