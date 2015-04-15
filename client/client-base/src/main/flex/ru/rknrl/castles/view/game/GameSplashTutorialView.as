//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game {
import flash.events.Event;

import ru.rknrl.core.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.TutorialView;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;

public class GameSplashTutorialView extends TutorialView {
    private var _arrows:ArrowsView;

    public function get arrows():ArrowsView {
        return _arrows;
    }

    private var locale:CastlesLocale;

    public function GameSplashTutorialView(layout:Layout, locale:CastlesLocale, deviceFactory:DeviceFactory) {
        super(layout, deviceFactory);
        this.locale = locale;
        addChild(_arrows = new ArrowsView());
        _arrows.transform.colorTransform = Colors.tutorTransform;
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function onEnterFrame(event:Event):void {
        _arrows.orientArrows(new Point(cursor.x, cursor.y));
    }

    public function tween(a:Point, b:Point):ITutorCommand {
        return _tween(a, b);
    }
}
}
