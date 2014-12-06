package ru.rknrl.castles.menu.screens.main.popup.popup.item {
import flash.display.DisplayObject;
import flash.text.TextField;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.layout.LayoutPortrait;
import ru.rknrl.funnyUi.GoldTextField;
import ru.rknrl.utils.changeTextFormat;
import ru.rknrl.utils.createTextField;

public class PopupItemPortrait extends PopupItem {
    private var icon:DisplayObject;
    private var iconOriginalHeight: int;
    private var nameTextField:TextField;
    private var infoTextField:TextField;
    private var priceTextField:GoldTextField;

    public function PopupItemPortrait(icon:DisplayObject, name:String, info:String, price:int, color:uint, popupWidth:int, layout:LayoutPortrait) {
        addChild(this.icon = icon);
        iconOriginalHeight = icon.height;

        nameTextField = createTextField(layout.popupItemNameTextFormat, name);
        nameTextField.textColor = color;
        addChild(nameTextField);

        addChild(infoTextField = createTextField(layout.popupItemInfoTextFormat, info));

        addChild(priceTextField = new GoldTextField("", layout.popupItemPriceTextFormat, price, Colors.magenta));

        updateLayout(popupWidth, layout);
    }

    public function updateLayout(popupWidth:int, layout:LayoutPortrait):void {
        const iconSize:int = layout.popupIconSize;
        const popupPadding:int = layout.popupPadding;
        const popupIconGap:int = layout.popupIconGap;
        const textLeft:int = popupPadding + iconSize + popupIconGap;

        icon.x = popupPadding + iconSize / 2;
        icon.y = iconSize / 2;
        icon.scaleX = icon.scaleY = iconSize / iconOriginalHeight;

        changeTextFormat(nameTextField, layout.popupItemNameTextFormat);
        nameTextField.x = textLeft;
        nameTextField.y = 0;

        changeTextFormat(infoTextField, layout.popupItemInfoTextFormat);
        infoTextField.x = textLeft;
        infoTextField.y = iconSize - infoTextField.height;

        priceTextField.textFormat = layout.popupItemPriceTextFormat;
        priceTextField.x = popupWidth - popupPadding - priceTextField.width;
        priceTextField.y = (iconSize - priceTextField.height) / 2;
    }

    override public function animate():void {
        priceTextField.animate();
    }
}
}
