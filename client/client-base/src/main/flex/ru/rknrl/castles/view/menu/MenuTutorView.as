package ru.rknrl.castles.view.menu {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.*;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.dto.SlotId;

public class MenuTutorView extends TutorialView {
    private var swipe:Boolean;

    public function MenuTutorView(layout:Layout, deviceFactory:DeviceFactory) {
        swipe = deviceFactory.swipe();
        super(layout, deviceFactory);
    }

    public function playSwipe():void {
        if (swipe) {
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

        function onComplete():void {
            itemsLayer.removeChild(arrow);
        }

        play(new <ITutorCommand>[
            open,
            pos(startPos),
            wait(500),
            tween(startPos, endPos)
        ], onComplete);
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
            open,
            tween(screenCorner, clickPos),
            wait(100),
            click
        ]);
    }
}
}
