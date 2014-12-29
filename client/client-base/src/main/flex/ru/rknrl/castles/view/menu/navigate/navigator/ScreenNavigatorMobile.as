package ru.rknrl.castles.view.menu.navigate.navigator {
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.*;

public class ScreenNavigatorMobile extends ScreenNavigator {
    public function ScreenNavigatorMobile(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale) {
        super(screens, gold, layout, locale);

        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onAddedToStage(event:Event):void {
        removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        stage.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
    }

    // mouse

    private static const epsilon:Number = 0.5;

    private static const lastXInterval:int = 100;
    private static const inertiaFactor:Number = 100;

    private var lastTime:int;
    private var lastX:Number;
    private var lastXTime:int;

    private static const mouseMoveSpeed:Number = 50;
    private static const tweenSpeed:Number = 150;
    private var nextX:Number = 0;

    private var mouseDown:Boolean;
    private var mouseDeltaX:Number;
    private var speed:Number;

    private function onMouseDown(event:MouseEvent):void {
        mouseDown = true;
        mouseDeltaX = holder.x - mouseX;
    }

    private function onMouseUp(event:MouseEvent):void {
        if (mouseDown) {
            mouseDown = false;

            var inertia:Number = (holder.x - lastX) * inertiaFactor;
            if (inertia > layout.screenCenterX) inertia = layout.screenCenterX;
            if (inertia < -layout.screenCenterX) inertia = -layout.screenCenterX;

            if (holder.x + inertia >= layout.screenCenterX) nextX = layout.screenWidth;
            else if (holder.x + inertia <= -layout.screenCenterX) nextX = -layout.screenWidth;
            else nextX = 0;

            speed = tweenSpeed;
        }
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;

        if (mouseDown) {
            nextX = mouseX + mouseDeltaX;
            speed = mouseMoveSpeed;

            if (time - lastXTime > lastXInterval) {
                lastX = holder.x;
                lastXTime = time;
            }
        }

        const delta:Number = nextX - holder.x;

        if (Math.abs(delta) < epsilon) {
            holder.x = nextX;
        } else {
            holder.x += delta * (deltaTime / speed);
        }

        if (holder.x >= layout.screenCenterX) {
            currentScreenIndex = getNextIndex(currentScreenIndex);
            holder.x -= layout.screenWidth;
            mouseDeltaX -= layout.screenWidth;
            nextX = 0;
            lastX = holder.x;
            speed = tweenSpeed;
        } else if (holder.x <= -layout.screenCenterX) {
            currentScreenIndex = getPrevIndex(currentScreenIndex);
            holder.x += layout.screenWidth;
            mouseDeltaX += layout.screenWidth;
            nextX = 0;
            lastX = holder.x;
            speed = tweenSpeed;
        }

        screens[currentScreenIndex].transition = 1 - Math.abs(holder.x) / layout.screenCenterX;
    }
}
}
