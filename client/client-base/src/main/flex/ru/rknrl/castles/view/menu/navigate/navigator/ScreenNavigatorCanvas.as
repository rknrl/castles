//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.navigate.navigator {
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.*;
import ru.rknrl.utils.Tweener;

public class ScreenNavigatorCanvas extends ScreenNavigator {
    public function ScreenNavigatorCanvas(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale) {
        super(screens, gold, layout, locale);
        navigationPoints.addEventListener(ChangeScreenEvent.CHANGE_SCREEN, onChangeScreen);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    override protected function get navigationPointsScale():Number {
        return 1.5;
    }

    private var nextScreenIndex:int;

    private function onChangeScreen(event:ChangeScreenEvent):void {
        const index:Number = screens.indexOf(event.screen);
        if (nextScreenIndex != index) {
            tweener.nextValue = index > nextScreenIndex ? -layout.screenWidth : layout.screenWidth;
            nextScreenIndex = index;
            updateScreensPos();
        }
    }

    override protected function updateScreensPos():void {
        screens[currentScreenIndex].x = 0;
        screens[currentScreenIndex].visible = true;

        if (nextScreenIndex != currentScreenIndex) {
            screens[nextScreenIndex].x = layout.screenWidth;
            screens[nextScreenIndex].visible = true;
        }
    }

    private static const tweenSpeed:Number = 150;
    private const tweener:Tweener = new Tweener(tweenSpeed);

    private function onEnterFrame(event:Event):void {
        tweener.update(getTimer());

        if (holder.x >= layout.screenCenterX) {
            currentScreenIndex = nextScreenIndex;
            tweener.value -= layout.screenWidth;
            tweener.nextValue = 0;
        } else if (holder.x <= -layout.screenCenterX) {
            currentScreenIndex = nextScreenIndex;
            tweener.value += layout.screenWidth;
            tweener.nextValue = 0;
        }

        holder.x = tweener.value;

        const transition:Number = 1 - Math.abs(holder.x) / layout.screenCenterX;
        screens[currentScreenIndex].transition = transition;
        if (screens[currentScreenIndex].showBalance) balanceTextField.alpha = transition;
    }

}
}
