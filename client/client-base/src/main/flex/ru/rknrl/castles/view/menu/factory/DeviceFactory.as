package ru.rknrl.castles.view.menu.factory {
import flash.display.DisplayObject;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.menu.navigate.navigator.*;

public interface DeviceFactory {
    function screenNavigator(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale):ScreenNavigator;

    function cursor():DisplayObject;

    function touchable():Boolean;
}
}
