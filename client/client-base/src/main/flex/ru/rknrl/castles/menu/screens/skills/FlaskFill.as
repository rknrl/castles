package ru.rknrl.castles.menu.screens.skills {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class FlaskFill extends Sprite {
    private var w:int;
    private var h:int;
    private var waterLine:FlaskWaterLine;
    private var fill:Bitmap;

    public function FlaskFill(w:int, h:int, color:uint, layout:Layout) {
        _color = color;

        addChild(waterLine = new FlaskWaterLine(w, 10));
        addChild(fill = new Bitmap(new BitmapData(1, 1, false, 0xcccccc)));

        updateLayout(w, h, layout);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function updateLayout(w:int, h:int, layout:Layout):void {
        this.w = w;
        this.h = h;

        redrawFill();
    }

    private var _color:uint;

    public function set color(value:uint):void {
        _color = value;
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

    private function onEnterFrame(event:Event):void {
        if (getTimer() <= startTime + levelUpDuration) {
            redrawFill();
        }
    }

    public function redrawFill():void {
        const levelH:int = 25;

        const startFillHeight:int = _oldSkillLevel.id() * levelH;
        const endFillHeight:int = _skillLevel.id() * levelH;

        const fillHeight:int = interpolate(startFillHeight, endFillHeight, getTimer(), startTime, levelUpDuration, easer);

        fill.x = -w / 2;
        fill.y = h - fillHeight;
        fill.width = w;
        fill.height = fillHeight;

        waterLine.y = h - fillHeight;
    }
}
}
