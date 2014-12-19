package ru.rknrl.castles.menu.screens.skills.flask {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.Utils;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class FlaskFill extends Sprite {
    public static const WIDTH:int = 34;
    public static const HEIGHT:int = 93;
    private static const levelHeight:int = 25;

    private var waterLine:FlaskWaterLine;
    private var fill:Bitmap;

    public function FlaskFill() {
        addChild(waterLine = new FlaskWaterLine(WIDTH));
        addChild(fill = new Bitmap(Utils.flaskFill));
        fill.x = -WIDTH / 2;
        fill.width = WIDTH;

        redrawFill();
    }

    private static const levelUpDuration:int = 1000;
    private static const easer:IEaser = new Linear(0, 1);
    private var startTime:int;

    private var _skillLevel:SkillLevel = SkillLevel.SKILL_LEVEL_0;
    private var _oldSkillLevel:SkillLevel = SkillLevel.SKILL_LEVEL_0;

    public function set skillLevel(value:SkillLevel):void {
        _oldSkillLevel = _skillLevel;
        _skillLevel = value;
        startEaser();
    }

    private function startEaser():void {
        startTime = getTimer();
        redrawFill();
    }

    public function onEnterFrame(fraction:Number):void {
        waterLine.onEnterFrame(fraction);
        if (getTimer() <= startTime + levelUpDuration) {
            redrawFill();
        }
    }

    public function redrawFill():void {
        const startFillHeight:int = _oldSkillLevel.id() * levelHeight;
        const endFillHeight:int = _skillLevel.id() * levelHeight;

        const fillHeight:int = interpolate(startFillHeight, endFillHeight, getTimer(), startTime, levelUpDuration, easer);

        waterLine.y = HEIGHT - fillHeight;
        fill.y = HEIGHT - fillHeight;
        fill.height = fillHeight;
    }
}
}
