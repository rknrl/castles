package ru.rknrl.castles.menu.screens.main.popup.popup {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.utils.changeTextFormat;
import ru.rknrl.utils.createTextField;

public class PopupLandscape extends Popup {
    private var background:Sprite;
    private var title:TextField;

    private var items:Vector.<DisplayObject>;

    public function PopupLandscape(titleText:String, items:Vector.<DisplayObject>, layout:LayoutLandscape) {
        this.items = items;

        addChild(background = new Sprite());

        background.addChild(title = createTextField(layout.popupTitleTextFormat, titleText));

        for each(var item:DisplayObject in items) {
            background.addChild(item);
        }

        updateLayout(layout);
        super();
    }

    public function updateLayout(layout:LayoutLandscape):void {
        x = layout.bodyCenterX;
        y = layout.bodyCenterY;

        const cornerRadius:int = layout.popupCornerRadius;

        const width:int = items.length * (layout.popupItemWidth + layout.popupGap) - layout.popupGap + cornerRadius * 2;
        const height:int = layout.popupItemWidth + cornerRadius * 2;

        background.graphics.clear();
        background.graphics.beginFill(0xffffff);
        background.graphics.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
        background.graphics.endFill();

        background.x = -width / 2;
        background.y = -height / 2;

        changeTextFormat(title, layout.popupTitleTextFormat);
        title.x = (width - title.width) / 2;
        title.y = layout.popupPadding;

        for (var i:int = 0; i < items.length; i++) {
            items[i].x = cornerRadius + i * (layout.popupItemWidth + layout.popupGap);
            items[i].y = layout.popupPadding + layout.popupTitleHeight + layout.popupGap;
        }
    }

    override protected function updateValue(value:Number):void {
        scaleX = scaleY = value;
    }
}
}
