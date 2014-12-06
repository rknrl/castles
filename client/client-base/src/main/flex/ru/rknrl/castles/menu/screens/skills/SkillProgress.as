package ru.rknrl.castles.menu.screens.skills {
import flash.display.Graphics;
import flash.display.Shape;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class SkillProgress extends Sprite {
    private var w:int;
    private var h:int;
    private var corner:int;
    private var fill:Shape;
    private var marks:Shape;

    public function SkillProgress(w:int, h:int, color:uint, layout:Layout) {
        _color = color;

        addChild(fill = new Shape());
        addChild(marks = new Shape());

        updateLayout(w, h, layout);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function updateLayout(w:int, h:int, layout:Layout):void {
        this.w = w;
        this.h = h;
        corner = layout.skillViewCorner;

        const markH:int = layout.skillViewMarkHeight;

        marks.graphics.clear();
        for (var i:int = 1; i < 3; i++) {
            marks.graphics.beginFill(0xffffff);
            marks.graphics.drawRect(-w / 2, (h / 3) * i - markH / 2, w, markH);
            marks.graphics.endFill();
        }

        redrawRect();
        redrawFill();
    }

    private var _color:uint;

    public function set color(value:uint):void {
        _color = value;
        redrawRect();
        redrawFill();
    }

    private function redrawRect():void {
        graphics.clear();
        graphics.beginFill(_color, 0.3);
        graphics.drawRoundRect(-w / 2, 0, w, h, corner, corner);
        graphics.endFill();
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
        const startFillHeight:int = _oldSkillLevel.id() * (h / 3);
        const endFillHeight:int = _skillLevel.id() * (h / 3);

        const fillHeight:int = interpolate(startFillHeight, endFillHeight, getTimer(), startTime, levelUpDuration, easer);

        const g:Graphics = fill.graphics;
        g.clear();
        g.beginFill(_color);
        g.drawRoundRect(-w / 2, h - fillHeight, w, fillHeight, corner, corner);
        g.endFill();
    }
}
}
