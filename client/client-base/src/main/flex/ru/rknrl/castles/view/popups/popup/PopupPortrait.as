package ru.rknrl.castles.view.popups.popup {
import flash.display.Shape;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.popups.PopupEvent;

public class PopupPortrait extends Popup {
    private var bg:Shape;
    private var items:Vector.<PopupItem>;
    private var title:PopupTitle;
    private var cancelButton:PopupCancelButton;

    public function PopupPortrait(titleText:String, cancelText:String, items:Vector.<PopupItem>, layout:Layout) {
        this.items = items;

        addChild(bg = new Shape());

        addChild(title = new PopupTitle(titleText, layout.popupWidth(items.length), layout));

        for each(var item:PopupItem in items) addChild(item);

        addChild(cancelButton = new PopupCancelButton(layout, layout.popupWidth(items.length), cancelText));
        cancelButton.addEventListener(MouseEvent.MOUSE_DOWN, onClick);

        this.layout = layout;
    }

    private var _layout:Layout;

    override public function set layout(value:Layout):void {
        _layout = value;

        const width:Number = value.popupWidth(items.length);
        const height:Number = _layout.popupTitleHeight + _layout.popupItemSize * items.length;

        bg.graphics.clear();
        bg.graphics.beginFill(0xffffff);
        bg.graphics.drawRoundRect(0, 0, width, height, value.corner, value.corner);
        bg.graphics.endFill();
        bg.x = value.popupPadding;

        title.setLayout(width, value);
        title.x = value.popupPadding;

        for (var i:int = 0; i < items.length; i++) {
            const item:PopupItem = items[i];
            item.layout = value;
            item.x = value.popupPadding;
            item.y = value.popupTitleHeight + value.popupItemSize * i;
        }

        cancelButton.setLayout(width, value);
        cancelButton.x = value.popupPadding;
        cancelButton.y = height + value.popupCancelButtonGap;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(PopupEvent.CLOSE, true));
    }

    override public function set transition(value:Number):void {
        const height:Number = _layout.popupTitleHeight + _layout.popupItemSize * items.length + _layout.popupCancelButtonGap + _layout.popupCancelHeight + _layout.popupPadding;
        y = _layout.screenHeight - height * value;
    }
}
}
