package ru.rknrl.castles.view.menu {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.*;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.dto.SlotId;

public class MenuTutorView extends TutorialView {
    private var touchable:Boolean;

    public function MenuTutorView(layout:Layout, deviceFactory:DeviceFactory) {
        touchable = deviceFactory.touchable();
        super(layout, deviceFactory);
    }

    public function playSwipe():void {
        if (touchable) {
            playMobileSwipe();
        } else {
            tweenAndClick(layout.middleNavigationPoint);
        }
    }

    private function playMobileSwipe():void {
        const y:Number = layout.navigationPointsY - 64 * layout.scale;
        const startPos:Point = new Point(layout.screenCenterX - 80 * layout.scale, y);
        const endPos:Point = new Point(layout.screenCenterX + 80 * layout.scale, y);

        const arrow:SwipeArrowMC = new SwipeArrowMC();
        arrow.scaleX = arrow.scaleY = layout.scale;
        arrow.x = layout.screenCenterX;
        arrow.y = y;
        itemsLayer.addChild(arrow);

        function removeArrow():void {
            itemsLayer.removeChild(arrow);
        }

        play(new <ITutorCommand>[
            wait(500),
            open,
            cursorPos(startPos),
            wait(500),
            tween(startPos, endPos),
            wait(1000),
            close,
            exec(removeArrow)
        ]);
    }

    public function playSlot(slotId:SlotId):void {
        tweenAndClick(layout.slotPosGlobal(slotId));
    }

    public function playMagicItem():void {
        tweenAndClick(layout.firstMagicItem);
    }

    public function playFlask():void {
        tweenAndClick(layout.firstFlask);
    }

    private function tweenAndClick(clickPos:Point):void {
        play(new <ITutorCommand>[
            wait(500),
            open,
            tween(screenCorner, clickPos),
            wait(100),
            click,
            wait(1000),
            close
        ]);
    }
}
}
