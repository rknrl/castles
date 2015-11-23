//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.popups.popup {
import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.core.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.AnimatedTextField;
import ru.rknrl.display.createTextField;

public class PopupItem extends Sprite {
    private var mouseHolder:Bitmap;
    private var icon:DisplayObject;
    private var textField:TextField;
    private var priceTextField:AnimatedTextField;

    public function PopupItem(layout:Layout, icon:DisplayObject, text:String, price:int) {
        mouseChildren = false;

        addChild(mouseHolder = new Bitmap(Colors.transparent));

        addChild(this.icon = icon);

        addChild(textField = createTextField(Fonts.popupText));
        textField.text = text;

        addChild(priceTextField = new AnimatedTextField(Fonts.popupPrice));
        priceTextField.text = price + "★";

        this.layout = layout;
    }

    public function set layout(value:Layout):void {
        icon.scaleX = icon.scaleY = value.scale;

        mouseHolder.width = value.popupItemWidth;
        mouseHolder.height = value.popupItemSize;

        const iconPos:Point = value.popupIconPos;
        icon.x = iconPos.x;
        icon.y = iconPos.y;

        textField.scaleX = textField.scaleY = value.scale;
        const textPos:Point = value.popupTextPos(textField.width, textField.height);
        textField.x = textPos.x;
        textField.y = textPos.y;

        priceTextField.textScale = value.scale;
        const pricePos:Point = value.popupPricePos(priceTextField.textWidth, priceTextField.textHeight);
        priceTextField.x = pricePos.x + priceTextField.textWidth / 2;
        priceTextField.y = pricePos.y + priceTextField.textHeight / 2;
    }

    public function animatePrices():void {
        priceTextField.elastic();
    }
}
}
