package ru.rknrl.castles.utils {
import flash.display.Shape;

public class Shadow extends Shape {
    private static const WIDTH:int = 24;
    private static const HEIGHT:int = 3;

    public function Shadow() {
        graphics.beginFill(0x444444, 0.2);
        graphics.drawEllipse(-WIDTH / 2, -HEIGHT / 2, WIDTH, HEIGHT);
        graphics.endFill();
    }
}
}
