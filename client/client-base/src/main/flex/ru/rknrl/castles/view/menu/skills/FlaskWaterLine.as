package ru.rknrl.castles.view.menu.skills {
import flash.display.Bitmap;
import flash.display.Sprite;

import ru.rknrl.castles.view.Colors;

public class FlaskWaterLine extends Sprite {
    private static const maxHeight:int = 4;
    private var bitmap:Bitmap;

    public function FlaskWaterLine(width:int) {
        addChild(bitmap = new Bitmap(Colors.flaskFillBitmapData));
        bitmap.width = width;
        bitmap.x = -width / 2;
    }

    public function onEnterFrame(fraction:Number):void {
        const unit:Number = (fraction + 1) / 2; // from 0 to 1
        const height:Number = unit * maxHeight;
        bitmap.height = height;
        bitmap.y = -height;
    }
}
}
