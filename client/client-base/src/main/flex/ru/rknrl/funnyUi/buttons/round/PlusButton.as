package ru.rknrl.funnyUi.buttons.round {
import flash.display.Graphics;
import flash.display.Shape;

public class PlusButton extends RoundButton {
    public function PlusButton(radius:int, color:uint) {
        super(radius, color);
    }

    override protected function createIcon():Shape {
        const shape:Shape = new Shape();
        const g:Graphics = shape.graphics;
        g.beginFill(0xffffff);
        g.drawRect(-8, -2, 16, 4);
        g.endFill();
        g.beginFill(0xffffff);
        g.drawRect(-2, -8, 4, 16);
        g.endFill();
        return shape;
    }
}
}
