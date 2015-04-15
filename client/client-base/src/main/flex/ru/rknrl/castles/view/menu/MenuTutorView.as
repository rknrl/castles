//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu {
import ru.rknrl.core.points.Point;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.*;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.dto.SlotId;

public class MenuTutorView extends TutorialView {
    public function MenuTutorView(layout:Layout, deviceFactory:DeviceFactory) {
        _touchable = deviceFactory.touchable();
        super(layout, deviceFactory);
    }

    private var _touchable:Boolean;

    public function get touchable():Boolean {
        return _touchable;
    }

    public function get middleNavigationPointPos():Point {
        return layout.middleNavigationPoint;
    }

    private function get swipeY():Number {
        return layout.navigationPointsY - 64 * layout.scale;
    }

    public function get swipeStartPos():Point {
        return new Point(layout.screenCenterX - 80 * layout.scale, swipeY);
    }

    public function get swipeEndPos():Point {
        return new Point(layout.screenCenterX + 80 * layout.scale, swipeY);
    }

    public function slotPos(slotId:SlotId):Point {
        return layout.slotPosGlobal(slotId);
    }

    public function get firstMagicItemPos():Point {
        return layout.firstMagicItem;
    }

    public function get firstFlaskPos():Point {
        return layout.firstFlask;
    }

    public function tween(a:Point, b:Point):ITutorCommand {
        return _tween(a, b);
    }

    public function addArrow():void {
        const arrow:SwipeArrowMC = new SwipeArrowMC();
        arrow.scaleX = arrow.scaleY = layout.scale;
        arrow.x = layout.screenCenterX;
        arrow.y = y;
        itemsLayer.addChild(arrow);
    }
}
}
