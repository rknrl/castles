package ru.rknrl.castles.view.menu.navigate.navigator.factory {
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.menu.navigate.navigator.*;

public class ScreenNavigatorCanvasFactory implements ScreenNavigatorFactory {
    public function create(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale):ScreenNavigator {
        return new ScreenNavigatorCanvas(screens, gold, layout, locale);
    }
}
}
