package ru.rknrl.castles.view.menu.bank {
import flash.display.Shape;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.applyStarTextFormat;
import ru.rknrl.castles.view.utils.centerize;
import ru.rknrl.castles.view.utils.createTextField;

public class Button extends Sprite {
    private var rect:Shape;
    private var textField:TextField;
    private var lockView:LockView;

    public function Button(layout:Layout) {
        addChild(rect = new Shape());
        addChild(textField = createTextField(Fonts.button));
        addChild(lockView = new LockView());
        this.layout = layout;
    }

    public function set layout(value:Layout):void {
        rect.graphics.clear();
        rect.graphics.beginFill(Colors.magenta);
        rect.graphics.drawRoundRect(-value.buttonWidth / 2, -value.buttonHeight / 2, value.buttonWidth, value.buttonHeight, value.corner, value.corner);
        rect.graphics.endFill();

        textField.scaleX = textField.scaleY = value.scale;
        centerize(textField);
    }

    public function set text(value:String):void {
        textField.text = value;
        applyStarTextFormat(textField);
        centerize(textField);
    }

    public function set lock(value:Boolean):void {
        lockView.visible = value;
        mouseEnabled = !value;
    }
}
}
