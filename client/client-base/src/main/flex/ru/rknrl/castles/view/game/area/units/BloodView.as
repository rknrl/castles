//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.units {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.castles.model.points.Point;

public class BloodView extends Sprite {
    private static const bitmapData:BitmapData = createBloodBitmapData();

    private static function createBloodBitmapData():BitmapData {
        const shape:Shape = new Shape();
        shape.graphics.beginFill(0xff0000);
        shape.graphics.drawEllipse(0, 0, 4, 2);
        shape.graphics.endFill();

        const bitmapData:BitmapData = new BitmapData(4, 2, true, 0);
        bitmapData.draw(shape);
        return bitmapData;
    }

    public function addBlood(pos:Point):void {
        const bitmap:Bitmap = new Bitmap(bitmapData);
        bitmap.x = pos.x - 2;
        bitmap.y = pos.y - 1;
        addChild(bitmap);
    }
}
}
