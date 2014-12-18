package ru.rknrl.castles.menu.screens.shop {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.Dictionary;

import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuyItemDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.funnyUi.GoldTextField;
import ru.rknrl.utils.centerize;

public class ShopScreen extends MenuScreen {
    private var sender:AccountFacadeSender;

    private var titleHolder:Sprite;
    private var title:GoldTextField;

    private const typeToItemView:Dictionary = new Dictionary();

    private const animated:Vector.<DisplayObject> = new <DisplayObject>[];

    public function ShopScreen(id:String, itemsCount:ItemsCount, itemPrice:int, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;

        titleHolder = new Sprite();
        animated.push(titleHolder);
        addChild(titleHolder);

        titleHolder.addChild(title = new GoldTextField(locale.shopTitle, layout.shopTitleTextFormat, itemPrice, Colors.randomColor()));

        for (var i:int = 0; i < Utils.SHOP_ALL_ITEMS.length; i++) {
            const itemType:ItemType = Utils.SHOP_ALL_ITEMS[i];
            const item:Item = new Item(itemType, itemsCount.getCount(itemType), Colors.randomColor(), layout);
            item.addEventListener(MouseEvent.CLICK, onItemClick);
            typeToItemView[itemType] = item;
            animated.push(item);
            addChild(item);
        }

        this.itemPrice = itemPrice;

        updateLayout(layout);

        super(id);
    }

    public function updateLayout(layout:Layout):void {
        titleHolder.x = layout.titleCenterX;
        titleHolder.y = layout.titleCenterY;

        title.textFormat = layout.shopTitleTextFormat;
        centerize(title);

        const count:int = Utils.SHOP_ALL_ITEMS.length;

        for (var i:int = 0; i < count; i++) {
            const itemType:ItemType = Utils.SHOP_ALL_ITEMS[i];

            const itemWidth:int = layout.shopItemWidth;
            const itemHeight:int = layout.shopItemHeight;

            const horizontalGap:int = (layout.bodyWidth - itemWidth * count) / (count + 1);
            const left:int = horizontalGap;

            const item:Item = typeToItemView[itemType];
            item.updateLayout(layout);
            item.x = left + i * (itemHeight + horizontalGap);
            item.y = layout.bodyCenterY;
        }
    }

    override public function changeColors():void {
        title.color = Colors.randomColor();

        for each(var item:Item in typeToItemView) {
            item.color = Colors.randomColor();
        }
    }

    override public function set transition(value:Number):void {
        for each(var displayObject:DisplayObject in animated) {
            displayObject.scaleX = displayObject.scaleY = 0.6 + value * 0.4;
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
        title.gold = value;
        centerize(title);
    }

    private function onItemClick(event:MouseEvent):void {
        if (gold < _itemPrice) {
            title.animate();
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
        item.lock();
        lockedItems[item] = item;
    }

    public function unlockItem(item:Item):void {
        item.unlock();
        delete lockedItems[item];
    }
}
}
