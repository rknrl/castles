package ru.rknrl.castles.view.menu.shop {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.AnimatedShadow;
import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.dto.ItemType;

public class ShopMagicItem extends Sprite {
    private static const mouseHolderW:Number = Layout.itemSize + Layout.itemGap;
    private static const mouseHolderH:Number = 96;

    private var fly:Fly;
    private var magicItem:ShopMagicItemIcon;
    private var shadow:AnimatedShadow;

    public function ShopMagicItem(itemType:ItemType, count:int) {
        _itemType = itemType;

        mouseChildren = false;

        const mouseHolder:Bitmap = new Bitmap(Colors.transparent);
        mouseHolder.width = mouseHolderW;
        mouseHolder.height = mouseHolderH;
        mouseHolder.x = -mouseHolderW / 2;
        mouseHolder.y = -40;
        addChild(mouseHolder);

        addChild(magicItem = new ShopMagicItemIcon(itemType, count));

        shadow = new AnimatedShadow();
        shadow.y = Layout.shadowDistance;
        addChild(shadow);

        fly = new Fly(magicItem, shadow);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var _itemType:ItemType;

    public function get itemType():ItemType {
        return _itemType;
    }

    public function set count(value:int):void {
        magicItem.count = value;
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
    }

    public function set lock(value:Boolean):void {
        magicItem.lock = value;
        mouseEnabled = !value;
    }

    public function bounce():void {
        magicItem.bounce();
        shadow.bounce();
    }
}
}
