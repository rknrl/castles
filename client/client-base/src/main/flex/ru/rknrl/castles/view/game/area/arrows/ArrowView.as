package ru.rknrl.castles.view.game.area.arrows {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.castles.utils.points.Point;

public class ArrowView extends Sprite {
    public static const arrowHeadHeight:Number = 32;

    private var arrowBitmapData:BitmapData = new BitmapData(1, 1, false, 0xffff00);

    private static const bodyHeight:int = 1;
    private var headHeight:int;

    private var body:Bitmap;
    private var head:Shape;

    public function ArrowView(startPos:Point) {
        x = startPos.x;
        y = startPos.y;

        headHeight = arrowHeadHeight;

        body = new Bitmap(arrowBitmapData);
        body.width = headHeight;
        body.x = -headHeight / 2;
        body.height = bodyHeight;
        addChild(body);

        head = new Shape();
        head.graphics.beginFill(0xffff00);
        head.graphics.moveTo(-headHeight, 0);
        head.graphics.lineTo(0, headHeight);
        head.graphics.lineTo(headHeight, 0);
        head.graphics.endFill();
        addChild(head);
    }

    public function orient(endPos:Point):void {
        const dx:Number = endPos.x - x;
        const dy:Number = endPos.y - y;
        const distance:Number = Math.sqrt(dx * dx + dy * dy);

        body.visible = distance > headHeight;
        body.scaleY = (distance - headHeight) / bodyHeight;

        head.y = distance - headHeight;

        const angle:Number = Math.atan2(-dx, dy);
        rotation = angle * 180 / Math.PI;
    }
}
}
