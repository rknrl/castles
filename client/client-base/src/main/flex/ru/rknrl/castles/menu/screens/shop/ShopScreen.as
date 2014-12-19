package ru.rknrl.castles.menu.screens.shop {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.Dictionary;

import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Align;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuyItemDTO;
import ru.rknrl.dto.ItemType;

public class ShopScreen extends Screen {
    private var sender:AccountFacadeSender;
    private var locale:CastlesLocale;

    private var itemsHolder:Sprite;
    private const typeToItemView:Dictionary = new Dictionary();

    public function ShopScreen(itemsCount:ItemsCount, itemPrice:int, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.locale = locale;

        addChild(itemsHolder = new Sprite());

        for each(var itemType:ItemType in ItemType.values) {
            const item:Item = new Item(itemType, itemsCount.getCount(itemType), Colors.randomColor());
            item.addEventListener(MouseEvent.CLICK, onItemClick);
            typeToItemView[itemType] = item;
            itemsHolder.addChild(item);
        }

        this.itemPrice = itemPrice;

        updateLayout(layout);
    }

    private var _itemPrice:int;

    public function set itemPrice(value:int):void {
        _itemPrice = value;
    }

    override public function get titleText():String {
        return locale.shopTitle + " " + _itemPrice;
    }

    public function updateLayout(layout:Layout):void {
        const itemsWidth:int = Align.horizontal(typeToItemView, Item.SIZE, layout.shopItemGap);
        itemsHolder.x = layout.stageCenterX - itemsWidth / 2;
        itemsHolder.y = layout.bodyCenterY;
    }

    public function set itemsCount(value:ItemsCount):void {
        for (var itemType:ItemType in typeToItemView) {
            const item:Item = typeToItemView[itemType];
            item.count = value.getCount(itemType);
        }
        lock = false;
    }

    private function onItemClick(event:MouseEvent):void {
        if (gold < _itemPrice) {
            dispatchEvent(new Event(Utils.NOT_ENOUGH_GOLD))
        } else {
            const item:Item = Item(event.target);

            lock = true;

            const dto:BuyItemDTO = new BuyItemDTO();
            dto.type = item.itemType;
            sender.buyItem(dto);

            item.playBounce();
        }
    }

    public function set lock(value:Boolean):void {
        for each(var item:Item in typeToItemView) {
            item.lock = value;
        }
    }
}
}
