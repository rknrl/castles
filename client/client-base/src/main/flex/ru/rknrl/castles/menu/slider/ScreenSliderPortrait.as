package ru.rknrl.castles.menu.slider {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.LayoutPortrait;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class ScreenSliderPortrait extends ScreenSlider {
    private var mouseHolder:Bitmap;
    private var holder:Sprite;

    public function ScreenSliderPortrait(screens:Vector.<MenuScreen>, layout:LayoutPortrait) {
        super(screens);
        addChild(mouseHolder = new Bitmap(Utils.transparent));

        addChild(holder = new Sprite());

        for each(var screen:MenuScreen in screens) {
            holder.addChild(screen);
            screen.changeColors()
        }

        addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        addEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
        addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        currentScreen = getScreenById(Utils.SCREEN_CASTLE);

        updateLayout(layout);
    }

    private var layout:LayoutPortrait;

    public function updateLayout(layout:LayoutPortrait):void {
        this.layout = layout;

        mouseHolder.width = layout.stageWidth;
        mouseHolder.height = layout.stageHeight;

        inDrag = false;
        inEaser = false;
        updatePositions();
    }

    private var currentScreen:MenuScreen;
    private var leftScreen:MenuScreen;
    private var rightScreen:MenuScreen;

    private function updatePositions():void {
        for each(var screen:Sprite in screens) {
            screen.visible = false;
        }

        currentScreen.x = 0;

        const index:int = screens.indexOf(currentScreen);
        const leftIndex:int = index == 0 ? screens.length - 1 : index - 1;
        const rightIndex:int = index == screens.length - 1 ? 0 : index + 1;
        leftScreen = screens[leftIndex];
        leftScreen.x = -layout.stageWidth;
        rightScreen = screens[rightIndex];
        rightScreen.x = layout.stageWidth;

        currentScreen.visible = true;
        leftScreen.visible = true;
        rightScreen.visible = true;

        leftScreen.changeColors();
        rightScreen.changeColors();

        updateScreensMove();
    }

    // drag

    private var inDrag:Boolean;
    private var startMouseX:int;
    private var startX:int;
    private var oldX:int;

    private function onMouseDown(event:MouseEvent):void {
        inDrag = true;
        startMouseX = mouseX;
        startX = holder.x;
        oldX = holder.x;
    }

    private function onMouseMove(event:MouseEvent):void {
        if (inDrag) {
            oldX = holder.x;
            holder.x = mouseX - (startMouseX - startX);
            updateScreensMove();
        }
    }

    private function onMouseUp(event:MouseEvent):void {
        if (inDrag) {
            inDrag = false;

            var index:int = Math.round((holder.x + (holder.x - oldX) * 10) / layout.stageWidth);
            if (index < -1) index = -1;
            if (index > 1) index = 1;
            if (index < 0) {
                newScreen = rightScreen;
            } else if (index > 0) {
                newScreen = leftScreen;
            } else {
                newScreen = currentScreen;
            }
            startEaser(index);
        }
    }

    // easer

    private static const easerStageWidthDuration:int = 500;
    private static const easer:IEaser = new Linear(0, 1);
    private var inEaser:Boolean;
    private var easerStartX:int;
    private var easerEndX:int;
    private var easerDuration:int;
    private var easerStartTime:int;
    private var newScreen:MenuScreen;

    private function startEaser(screenIndex:int):void {
        inEaser = true;
        easerStartX = holder.x;
        easerEndX = screenIndex * layout.stageWidth;
        const distance:int = Math.abs(easerEndX - easerStartX);
        easerDuration = distance / layout.stageWidth * easerStageWidthDuration;
        easerStartTime = getTimer();
    }

    private function onEnterFrame(event:Event):void {
        if (inEaser) {
            holder.x = interpolate(easerStartX, easerEndX, getTimer(), easerStartTime, easerDuration, easer);
            if (holder.x == easerEndX) {
                holder.x = 0;
                inEaser = false;
                currentScreen = newScreen;
                updatePositions();
            } else {
                updateScreensMove();
            }
        }
    }

    private function updateScreensMove():void {
        var p:Number = Math.abs(holder.x / layout.stageWidth);
        leftScreen.transition = p;
        currentScreen.transition = 1 - p;
        rightScreen.transition = p;
    }
}
}
