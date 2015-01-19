package ru.rknrl.castles.view.menu.factory {
import flash.display.DisplayObject;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.menu.navigate.navigator.*;

public class CanvasFactory implements DeviceFactory {
    public function screenNavigator(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale):ScreenNavigator {
        return new ScreenNavigatorCanvas(screens, gold, layout, locale);
    }

    public function cursor():DisplayObject {
        return new CursorMC();
    }

    public function swipe():Boolean {
        return false;
    }
}
}
