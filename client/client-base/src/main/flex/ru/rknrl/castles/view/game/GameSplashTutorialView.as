//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game {
import flash.events.Event;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.TutorialView;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;

public class GameSplashTutorialView extends TutorialView {
    private var arrows:ArrowsView;
    private var locale:CastlesLocale;

    public function GameSplashTutorialView(layout:Layout, locale:CastlesLocale, deviceFactory:DeviceFactory) {
        super(layout, deviceFactory);
        this.locale = locale;
        addChild(arrows = new ArrowsView());
        arrows.transform.colorTransform = Colors.tutorTransform;
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function playArrow(startBuildingPos:Point, endBuildingPos:Point):void {
//        play(new <ITutorCommand>[
//            showCursor,
//            open,
//            tween(screenCorner, startBuildingPos),
//            mouseDown,
//            wait(400),
//            exec(function ():void {
//                arrows.addArrow(startBuildingPos);
//            }),
//            tween(startBuildingPos, endBuildingPos),
//            wait(400),
//            mouseUp,
//            exec(arrows.removeArrows),
//            wait(400)
//        ]);
    }

    private function onEnterFrame(event:Event):void {
        arrows.orientArrows(new Point(cursor.x, cursor.y));
    }
}
}
