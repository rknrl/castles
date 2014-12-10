package ru.rknrl.castles.menu.screens.main.popup.popup.item {
import flash.display.DisplayObject;

import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.funnyUi.GoldTextField;
import ru.rknrl.utils.changeTextFormat;

public class PopupItemLandscape extends PopupItem {
    private var icon:DisplayObject;
    private var iconOriginalHeight:int;
    private var nameTextField:Label;
    private var infoTextField:Label;
    private var priceTextField:GoldTextField;

    public function PopupItemLandscape(icon:DisplayObject, name:String, info:String, price:int, color:uint, layout:LayoutLandscape) {
        addChild(this.icon = icon);
        iconOriginalHeight = icon.height;

        nameTextField = createTextField(layout.popupItemNameTextFormat, name);
        nameTextField.textColor = color;
        addChild(nameTextField);

        addChild(infoTextField = createTextField(layout.popupItemInfoTextFormat, info));
        addChild(priceTextField = new GoldTextField("", layout.popupItemPriceTextFormat, price, 0x555555));

        updateLayout(layout);
    }

    public function updateLayout(layout:LayoutLandscape):void {
        const popupItemWidth:int = layout.popupItemWidth;
        const iconSize:int = layout.popupIconSize;

        icon.x = popupItemWidth / 2;
        icon.y = iconSize / 2;
        icon.scaleX = icon.scaleY = iconSize / iconOriginalHeight;

        changeTextFormat(nameTextField, layout.popupItemNameTextFormat);
        nameTextField.x = (popupItemWidth - nameTextField.width) / 2;
        nameTextField.y = layout.popupItemNameY;

        changeTextFormat(infoTextField, layout.popupItemInfoTextFormat);
        infoTextField.x = (popupItemWidth - infoTextField.width) / 2;
        infoTextField.y = layout.popupItemInfoY;

        priceTextField.textFormat = layout.popupItemPriceTextFormat;
        priceTextField.x = (popupItemWidth - priceTextField.width) / 2;
        priceTextField.y = layout.popupItemPriceY;
    }

    override public function animate():void {
        priceTextField.animate();
    }
}
}
