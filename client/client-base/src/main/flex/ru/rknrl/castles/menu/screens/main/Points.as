package ru.rknrl.castles.menu.screens.main {
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.utils.createCircle;

public class Points extends Sprite {
    public function Points() {
        const circle1:Shape = createCircle(4, 0xff0000);
        circle1.x = -24;
        addChild(circle1);

        const circle2:Shape = createCircle(4, 0xcccccc);
        circle2.x = -8;
        addChild(circle2);

        const circle3:Shape = createCircle(4, 0xcccccc);
        circle3.x = 8;
        addChild(circle3);

        const circle4:Shape = createCircle(4, 0xcccccc);
        circle4.x = 24;
        addChild(circle4);
    }
}
}
