package ru.rknrl.castles.menu.screens.main.popup.popup {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.layout.LayoutPortrait;
import ru.rknrl.utils.changeTextFormat;
import ru.rknrl.utils.createTextField;

public class PopupPortrait extends Popup {
    private var background:Sprite;
    private var title:TextField;
    private var items:Vector.<DisplayObject>;

    public function PopupPortrait(titleText:String, items:Vector.<DisplayObject>, layout:LayoutPortrait) {
        this.items = items;

        addChild(background = new Sprite());

        background.addChild(title = createTextField(layout.popupTitleTextFormat, titleText));

        for each(var item:DisplayObject in items) {
            background.addChild(item);
        }

        updateLayout(layout);
        super();
    }

    private var layout:Layout;

    public function updateLayout(layout:LayoutPortrait):void {
        this.layout = layout;

        const top:int = layout.popupPadding + layout.popupTitleHeight + layout.popupGap;

        changeTextFormat(title, layout.popupTitleTextFormat);
        title.x = (width - title.width) / 2;
        title.y = layout.popupPadding;

        for (var i:int = 0; i < items.length; i++) {
            items[i].x = 0;
            items[i].y = top + i * (layout.popupIconSize + layout.popupGap);
        }

        const cornerRadius:int = layout.popupCornerRadius;
        const height:int = items.length * (layout.popupIconSize + layout.popupGap) - layout.popupGap + top;
        background.graphics.clear();
        background.graphics.beginFill(0xffffff);
        background.graphics.drawRoundRect(0, 0, layout.stageWidth, height + cornerRadius, cornerRadius, cornerRadius);
        background.graphics.endFill();

        updateY();
    }

    private var _transition: Number;

    override public function set transition(value:Number):void {
        _transition = value;
        updateY();
    }

    private function updateY():void {
        y = layout.stageHeight - _transition * height;
    }
}
}
