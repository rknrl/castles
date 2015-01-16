package ru.rknrl.castles.view.menu.navigate.navigator {
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.*;

public class ScreenNavigatorCanvas extends ScreenNavigator {
    public function ScreenNavigatorCanvas(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale) {
        super(screens, gold, layout, locale);
        navigationPoints.addEventListener(ChangeScreenEvent.CHANGE_SCREEN, onChangeScreen);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    override protected function get navigationPointsScale():Number {
        return 1.5;
    }

    private function onChangeScreen(event:ChangeScreenEvent):void {
        // nextScreenIndex = screens.indexOf(event.screen);
        nextX = layout.screenWidth;
    }

    private static const epsilon:Number = 0.5;
    private var lastTime:int;
    private static const tweenSpeed:Number = 150;
    private var nextX:Number = 0;

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;

        const delta:Number = nextX - holder.x;

        if (Math.abs(delta) < epsilon) {
            holder.x = nextX;
        } else {
            holder.x += delta * (deltaTime / tweenSpeed);
        }

        if (holder.x >= layout.screenCenterX) {
            currentScreenIndex = getNextIndex(currentScreenIndex);
            holder.x -= layout.screenWidth;
            nextX = 0;
        } else if (holder.x <= -layout.screenCenterX) {
            currentScreenIndex = getPrevIndex(currentScreenIndex);
            holder.x += layout.screenWidth;
            nextX = 0;
        }

        screens[currentScreenIndex].transition = 1 - Math.abs(holder.x) / layout.screenCenterX;
    }
}
}
