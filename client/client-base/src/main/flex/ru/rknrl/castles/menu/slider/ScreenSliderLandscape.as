package ru.rknrl.castles.menu.slider {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class ScreenSliderLandscape extends ScreenSlider {
    private var screensHolder:Sprite;

    private var pointsHolder:Sprite;

    private var points:Vector.<MenuPoint> = new <MenuPoint>[];

    private function getPointByScreen(screen:Screen):MenuPoint {
        for each(var point:MenuPoint in points) {
            if (point.screen == screen) return point;
        }
        throw new Error("can't find point " + screen);
    }

    public function ScreenSliderLandscape(screens:Vector.<Screen>, layout:LayoutLandscape) {
        super(screens);

        addChild(screensHolder = new Sprite());

        for each(var screen:Screen in screens) {
            screensHolder.addChild(screen);
        }

        addChild(pointsHolder = new Sprite());


        for (var i:int = 0; i < screens.length; i++) {
            const point:MenuPoint = new MenuPoint(screens[i], layout.pointRadius);
            point.addEventListener(MouseEvent.CLICK, onMenuPointClick);
            points.push(point);
            pointsHolder.addChild(point);
        }

        addEventListener(Event.ENTER_FRAME, onEnterFrame);

        currentScreen = screens[0];

        updateLayout(layout);
    }

    private var layout:LayoutLandscape;

    public function updateLayout(layout:LayoutLandscape):void {
        this.layout = layout;

        for (var i:int = 0; i < points.length; i++) {
            const point:MenuPoint = points[i];
            point.radius = layout.pointRadius;
            point.x = (point.width + layout.pointGap) * i;
            point.y = layout.pointY;
        }

        pointsHolder.x = layout.stageCenterX - pointsHolder.width / 2 + point.width / 2;

        posScreens();
        updateScreenHolderPosition();
    }

    private function onMenuPointClick(event:MouseEvent):void {
        const point:MenuPoint = MenuPoint(event.target);
        const newScreen:Screen = point.screen;

        if (newScreen != currentScreen) {
            oldScreen = currentScreen;
            currentScreen = newScreen;
            startEaser();
        }
    }

    // current screen

    private var oldScreen:Screen;

    private var _currentScreen:Screen;

    public function get currentScreen():Screen {
        return _currentScreen;
    }

    public function set currentScreen(value:Screen):void {
        if (_currentScreen) {
            getPointByScreen(_currentScreen).selected = false;
        }
        _currentScreen = value;

        getPointByScreen(_currentScreen).selected = true;
    }

    // posScreens

    private function posScreens():void {
        for each(var screen:Screen in screens) {
            screen.visible = false;
        }

        currentScreen.x = -layout.stageWidth;
        currentScreen.visible = true;

        if (oldScreen) {
            oldScreen.x = 0;
            oldScreen.visible = true;
        }
    }

    // screenHolder easer

    private static const easer:IEaser = new Linear(0, 1);
    private static const duration:int = 500;

    private var easerStartTime:int;

    private function startEaser():void {
        posScreens();
        easerStartTime = getTimer();
        updateScreenHolderPosition();
    }

    private function onEnterFrame(event:Event):void {
        if (getTimer() <= easerStartTime + duration) {
            updateScreenHolderPosition();
        }
    }

    private function updateScreenHolderPosition():void {
        screensHolder.x = interpolate(0, layout.stageWidth, getTimer(), easerStartTime, duration, easer);
    }
}
}
