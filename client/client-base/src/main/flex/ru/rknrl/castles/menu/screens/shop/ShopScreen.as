package ru.rknrl.castles.menu.screens.shop {
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.Dictionary;

import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuyItemDTO;
import ru.rknrl.dto.ItemType;

public class ShopScreen extends Screen {
    private var sender:AccountFacadeSender;
    private var locale:CastlesLocale;

    private const typeToItemView:Dictionary = new Dictionary();

    public function ShopScreen(itemsCount:ItemsCount, itemPrice:int, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.locale = locale;

        for (var i:int = 0; i < Utils.SHOP_ALL_ITEMS.length; i++) {
            const itemType:ItemType = Utils.SHOP_ALL_ITEMS[i];
            const item:Item = new Item(itemType, itemsCount.getCount(itemType), Colors.randomColor());
            item.addEventListener(MouseEvent.CLICK, onItemClick);
            typeToItemView[itemType] = item;
            addChild(item);
        }

        this.itemPrice = itemPrice;

        updateLayout(layout);
    }

    override public function get titleText():String {
        return locale.shopTitle + " " + _itemPrice;
    }

    public function updateLayout(layout:Layout):void {
        const count:int = Utils.SHOP_ALL_ITEMS.length;
        const horizontalGap:int = layout.shopItemGap;
        const left:int = layout.stageCenterX - ((Item.SIZE + horizontalGap) * count - horizontalGap) / 2;

        for (var i:int = 0; i < count; i++) {
            const itemType:ItemType = Utils.SHOP_ALL_ITEMS[i];
            const item:Item = typeToItemView[itemType];
            item.x = left + i * (Item.SIZE + horizontalGap);
            item.y = layout.bodyCenterY;
        }
    }

    public function set itemsCount(value:ItemsCount):void {
        for each(var itemType:ItemType in Utils.ALL_ITEMS) {
            const item:Item = typeToItemView[itemType];
            const newCount:int = value.getCount(itemType);
            if (newCount > item.count) unlockItem(item);
            item.count = newCount;
        }
    }

    private var _itemPrice:int;

    public function set itemPrice(value:int):void {
        _itemPrice = value;
    }

    private function onItemClick(event:MouseEvent):void {
        if (gold < _itemPrice) {
            dispatchEvent(new Event(Utils.NOT_ENOUGH_GOLD))
        } else {
            const item:Item = Item(event.target);

            lockItem(item);

            const dto:BuyItemDTO = new BuyItemDTO();
            dto.type = item.itemType;
            sender.buyItem(dto);

            item.playBounce();
        }
    }

    private const lockedItems:Dictionary = new Dictionary();

    public function lockItem(item:Item):void {
        item.lock = true;
        lockedItems[item] = item;
    }

    public function unlockItem(item:Item):void {
        item.lock = false;
        delete lockedItems[item];
    }
}
}
