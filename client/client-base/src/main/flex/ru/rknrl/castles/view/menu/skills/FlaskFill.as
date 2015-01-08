package ru.rknrl.castles.view.menu.skills {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.utils.getTimer;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.dto.SkillLevel;

public class FlaskFill extends Sprite {
    private static const fillWidth:int = 32;
    private static const fillBottom:int = 36;
    private static const levelHeight:int = 24;
    private static const speed:int = 200;

    private var bitmap:Bitmap;
    private var waterLine:FlaskWaterLine;

    public function FlaskFill(skillLevel:SkillLevel) {
        addChild(bitmap = new Bitmap(new BitmapData(1, 1, false, Colors.flaskFillColor)));
        bitmap.width = fillWidth;
        bitmap.x = -fillWidth / 2;

        addChild(waterLine = new FlaskWaterLine(fillWidth));

        this.skillLevel = skillLevel;
        onEnterFrame(0);
    }

    public function set skillLevel(value:SkillLevel):void {
        nextHeight = value.id() * levelHeight;
    }

    private var nextHeight:Number = 0;
    private var lastTime:int;

    public function onEnterFrame(fraction:Number):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;

        const deltaHeight:Number = nextHeight - bitmap.height;
        bitmap.height += deltaHeight * Math.min(1, deltaTime / speed);
        bitmap.y = waterLine.y = fillBottom - bitmap.height;

        waterLine.onEnterFrame(fraction);
    }
}
}
