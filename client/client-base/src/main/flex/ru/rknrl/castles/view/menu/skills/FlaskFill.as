//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.skills {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.utils.getTimer;

import ru.rknrl.castles.view.Colors;
import protos.SkillLevel;
import ru.rknrl.utils.Tweener;

public class FlaskFill extends Sprite {
    private static const fillWidth:int = 32;
    private static const fillBottom:int = 36;
    private static const levelHeight:int = 24;
    private static const speed:int = 200;
    private const tweener:Tweener = new Tweener(speed);

    private var bitmap:Bitmap;
    private var waterLine:FlaskWaterLine;

    public function FlaskFill(skillLevel:SkillLevel) {
        addChild(bitmap = new Bitmap(Colors.flaskFillBitmapData));
        bitmap.width = fillWidth;
        bitmap.x = -fillWidth / 2;

        addChild(waterLine = new FlaskWaterLine(fillWidth));
        this.skillLevel = skillLevel;
        onEnterFrame(0);
    }

    public function set skillLevel(value:SkillLevel):void {
        tweener.nextValue = value.id * levelHeight;
    }

    public function onEnterFrame(fraction:Number):void {
        tweener.update(getTimer());

        bitmap.height = tweener.value;
        bitmap.y = waterLine.y = fillBottom - bitmap.height + 1;

        waterLine.onEnterFrame(fraction);
    }
}
}
