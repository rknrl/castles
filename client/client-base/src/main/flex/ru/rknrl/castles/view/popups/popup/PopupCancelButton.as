package ru.rknrl.castles.view.popups.popup {
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.createTextField;

public class PopupCancelButton extends Sprite {
    private var textField:TextField;

    public function PopupCancelButton(layout:Layout, width:Number, text:String) {
        addChild(textField = createTextField(Fonts.popupCancel));
        textField.text = text;

        setLayout(width, layout);
    }

    public function setLayout(width:Number, value:Layout):void {
        graphics.clear();
        graphics.beginFill(0xffffff);
        graphics.drawRoundRect(0, 0, width, value.popupCancelHeight, value.corner, value.corner);
        graphics.endFill();

        textField.scaleX = textField.scaleY = value.scale;
        textField.x = (width - textField.width) / 2;
        textField.y = (value.popupCancelHeight - textField.height) / 2;
    }
}
}
