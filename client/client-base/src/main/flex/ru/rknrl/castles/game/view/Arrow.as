package ru.rknrl.castles.game.view {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Shape;
import flash.display.Sprite;
import flash.geom.Point;

public class Arrow extends Sprite {
    public static const arrowHeadHeight:Number = 32;

    private var arrowBitmapData:BitmapData = new BitmapData(1, 1, false, 0xffff00);

    private static const bodyHeight:int = 1;
    private var headHeight:int;

    private var body:Bitmap;
    private var head:Shape;

    private var _fromBuildingId:int;

    public function get fromBuildingId():int {
        return _fromBuildingId;
    }

    public function Arrow(fromBuildingId:int, startX:Number, startY:Number) {
        _fromBuildingId = fromBuildingId;
        x = startX;
        y = startY;
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

    public function orient(mousePos:Point):void {
        const dx:Number = mousePos.x - x;
        const dy:Number = mousePos.y - y;
        const distance:Number = Math.sqrt(dx * dx + dy * dy);

        body.visible = distance > headHeight;
        body.scaleY = (distance - headHeight) / bodyHeight;

        head.y = distance - headHeight;

        const angle:Number = Math.atan2(-dx, dy);
        rotation = angle * 180 / Math.PI;
    }
}
}
