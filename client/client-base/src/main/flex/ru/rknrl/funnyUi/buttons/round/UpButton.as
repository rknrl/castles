package ru.rknrl.funnyUi.buttons.round {
import flash.display.Graphics;
import flash.display.Shape;

public class UpButton extends RoundButton {
    public function UpButton(radius:int, color:uint) {
        super(radius, color);
    }

    override protected function createIcon():Shape {
        const shape:Shape = new Shape();
        const g:Graphics = shape.graphics;
        g.beginFill(0xffffff);
        g.moveTo(-5, 5);
        g.lineTo(-5, 0);
        g.lineTo(-10, 0);
        g.lineTo(0, -10);
        g.lineTo(10, 0);
        g.lineTo(5, 0);
        g.lineTo(5, 5);
        g.endFill();
        return shape;
    }
}
}
