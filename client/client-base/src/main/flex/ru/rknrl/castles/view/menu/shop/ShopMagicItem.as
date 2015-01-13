package ru.rknrl.castles.view.menu.shop {
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.dto.ItemType;

public class ShopMagicItem extends Sprite {
    private var fly:Fly;
    private var magicItem:ShopMagicItemIcon;

    public function ShopMagicItem(itemType:ItemType, count:int) {
        _itemType = itemType;

        mouseChildren = false;

        addChild(magicItem = new ShopMagicItemIcon(itemType, count));

        const shadow:Shadow = new Shadow();
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
}
}
