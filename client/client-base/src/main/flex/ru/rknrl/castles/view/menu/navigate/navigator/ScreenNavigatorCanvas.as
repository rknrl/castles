package ru.rknrl.castles.view.menu.navigate.navigator {
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.*;

public class ScreenNavigatorCanvas extends ScreenNavigator {
    public function ScreenNavigatorCanvas(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale) {
        super(screens, gold, layout, locale);
        navigationPoints.addEventListener(ChangeScreenEvent.CHANGE_SCREEN, onChangeScreen);
    }

    private function onChangeScreen(event:ChangeScreenEvent):void {
        currentScreenIndex = screens.indexOf(event.screen);
    }
}
}
