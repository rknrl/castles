package ru.rknrl.castles.view.popups.popup {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.geom.Point;
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.createTextField;

public class PopupItem extends Sprite {
    private var icon:DisplayObject;
    private var textField:TextField;
    private var priceTextField:TextField;

    public function PopupItem(layout:Layout, icon:DisplayObject, text:String, price:int) {
        mouseChildren = false;

        addChild(this.icon = icon);

        addChild(textField = createTextField(Fonts.popupText));
        textField.text = text;

        addChild(priceTextField = createTextField(Fonts.popupPrice));
        priceTextField.text = price.toString();

        this.layout = layout;
    }

    public function set layout(value:Layout):void {
        icon.scaleX = icon.scaleY = value.scale;

        const iconPos:Point = value.popupIconPos;
        icon.x = iconPos.x;
        icon.y = iconPos.y;

        textField.scaleX = textField.scaleY = value.scale;
        const textPos:Point = value.popupTextPos(textField.width, textField.height);
        textField.x = textPos.x;
        textField.y = textPos.y;

        priceTextField.scaleX = priceTextField.scaleY = value.scale;
        const pricePos:Point = value.popupPricePos(priceTextField.width, priceTextField.height);
        priceTextField.x = pricePos.x;
        priceTextField.y = pricePos.y;
    }
}
}
