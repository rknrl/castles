package ru.rknrl.castles.view.menu.navigate.navigator {
import flash.display.Bitmap;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.*;
import ru.rknrl.castles.view.utils.Tweener;

public class ScreenNavigatorMobile extends ScreenNavigator {
    private var mouseHolder:Bitmap;

    public function ScreenNavigatorMobile(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale) {
        addChild(mouseHolder = new Bitmap(Colors.transparent));

        super(screens, gold, layout, locale);

        addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    override protected function get navigationPointsScale():Number {
        return 1;
    }

    override public function set layout(value:Layout):void {
        super.layout = value;
        mouseHolder.width = value.screenWidth;
        mouseHolder.height = value.screenHeight;
    }

    override protected function updateScreensPos():void {
        const prevIndex:int = getNextIndex(currentScreenIndex);
        const nextIndex:int = getPrevIndex(currentScreenIndex);

        screens[currentScreenIndex].x = 0;
        screens[currentScreenIndex].visible = true;

        screens[prevIndex].x = -layout.screenWidth;
        screens[prevIndex].visible = true;

        screens[nextIndex].x = layout.screenWidth;
        screens[nextIndex].visible = true;
    }

    protected function getNextIndex(index:int):int {
        return index > 0 ? index - 1 : screens.length - 1;
    }

    protected function getPrevIndex(index:int):int {
        return index < screens.length - 1 ? index + 1 : 0;
    }

    // mouse

    private const tweener:Tweener = new Tweener(tweenSpeed);

    private static const lastXInterval:int = 100;
    private static const inertiaFactor:Number = 100;

    private var lastX:Number;
    private var lastXTime:int;

    private static const mouseMoveSpeed:Number = 50;
    private static const tweenSpeed:Number = 150;

    private var mouseDown:Boolean;
    private var mouseDeltaX:Number;

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

            if (holder.x + inertia >= layout.screenCenterX) tweener.nextValue = layout.screenWidth;
            else if (holder.x + inertia <= -layout.screenCenterX) tweener.nextValue = -layout.screenWidth;
            else tweener.nextValue = 0;

            tweener.speed = tweenSpeed;
        }
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();

        if (mouseDown) {
            tweener.nextValue = mouseX + mouseDeltaX;
            tweener.speed = mouseMoveSpeed;

            if (time - lastXTime > lastXInterval) {
                lastX = holder.x;
                lastXTime = time;
            }
        }

        tweener.update(time);

        if (holder.x >= layout.screenCenterX) {
            currentScreenIndex = getNextIndex(currentScreenIndex);
            mouseDeltaX -= layout.screenWidth;
            tweener.value -= layout.screenWidth;
            tweener.nextValue = 0;
            lastX = holder.x;
            tweener.speed = tweenSpeed;
        } else if (holder.x <= -layout.screenCenterX) {
            currentScreenIndex = getPrevIndex(currentScreenIndex);
            mouseDeltaX += layout.screenWidth;
            tweener.value += layout.screenWidth;
            tweener.nextValue = 0;
            lastX = holder.x;
            tweener.speed = tweenSpeed;
        }

        holder.x = tweener.value;

        screens[currentScreenIndex].transition = 1 - Math.abs(holder.x) / layout.screenCenterX;
    }
}
}
