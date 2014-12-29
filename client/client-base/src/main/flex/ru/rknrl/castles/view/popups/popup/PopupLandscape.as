package ru.rknrl.castles.view.popups.popup {
import flash.display.Shape;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.popups.PopupEvent;

public class PopupLandscape extends Popup {
    private var content:Sprite;
    private var bg:Shape;
    private var items:Vector.<PopupItem>;
    private var title:PopupTitle;
    private var cancelButton:CloseMC;

    public function PopupLandscape(titleText:String, items:Vector.<PopupItem>, layout:Layout) {
        this.items = items;

        addChild(content = new Sprite());

        content.addChild(bg = new Shape());

        content.addChild(title = new PopupTitle(titleText, layout.popupWidth(items.length), layout));

        for each(var item:PopupItem in items) content.addChild(item);

        content.addChild(cancelButton = new CloseMC());
        cancelButton.addEventListener(MouseEvent.CLICK, onClick);

        this.layout = layout;
    }

    override public function set layout(value:Layout):void {
        const width:Number = value.popupWidth(items.length);
        const height:Number = value.popupPadding + value.popupTitleHeight + value.popupPadding + value.popupItemSize + value.popupPadding;

        bg.graphics.clear();
        bg.graphics.beginFill(0xffffff);
        bg.graphics.drawRoundRect(0, 0, width, height, value.corner, value.corner);
        bg.graphics.endFill();

        title.setLayout(width, value);
        title.y = value.popupPadding;

        for (var i:int = 0; i < items.length; i++) {
            const item:PopupItem = items[i];
            item.layout = value;
            item.x = value.popupPadding + (value.popupItemSize + value.popupPadding) * i;
            item.y = value.popupPadding + value.popupTitleHeight + value.popupPadding;
        }

        cancelButton.scaleX = cancelButton.scaleY = value.scale;
        cancelButton.x = width - value.popupPadding - cancelButton.width / 2;
        cancelButton.y = value.popupPadding + cancelButton.height / 2;

        content.x = -width / 2;
        content.y = -height / 2;
        x = value.screenCenterX;
        y = value.contentCenterY;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(PopupEvent.CLOSE, true));
    }

    override public function set transition(value:Number):void {
        scaleX = scaleY = value;
    }
}
}
