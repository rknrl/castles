package ru.rknrl.castles.view.menu.shop {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.text.TextField;

import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.menu.shop.ItemsCount;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.utils.Align;
import ru.rknrl.castles.view.utils.applyStarTextFormat;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.ItemType;

public class ShopScreen extends Screen {
    private var locale:CastlesLocale;

    private var magicItemsHolder:Sprite;
    private const magicItems:Vector.<ShopMagicItem> = new <ShopMagicItem>[];
    private var titleTextField:TextField;

    public function ShopScreen(itemsCount:ItemsCount, itemPrice:int, layout:Layout, locale:CastlesLocale) {
        this.locale = locale;
        addChild(magicItemsHolder = new Sprite());

        for each(var itemType:ItemType in ItemType.values) {
            const item:ShopMagicItem = new ShopMagicItem(itemType, itemsCount.getCount(itemType));
            item.addEventListener(MouseEvent.CLICK, onClick);
            magicItems.push(item);
            magicItemsHolder.addChild(item);
        }

        titleTextField = createTextField(Fonts.title);

        _layout = layout;
        this.itemPrice = itemPrice;
        this.layout = layout;
    }

    private function getMagicItem(itemType:ItemType):ShopMagicItem {
        for each(var item:ShopMagicItem in magicItems) {
            if (item.itemType == itemType) return item;
        }
        throw new Error("can't find magic item " + itemType)
    }

    public function set itemsCount(value:ItemsCount):void {
        for each(var itemType:ItemType in ItemType.values) {
            getMagicItem(itemType).count = value.getCount(itemType);
        }
    }

    public function set itemPrice(value:int):void {
        titleTextField.text = locale.shopTitle(value);
        applyStarTextFormat(titleTextField);
        alignTitle();
    }

    private var _layout:Layout;

    override public function set layout(value:Layout):void {
        _layout = value;

        magicItemsHolder.scaleX = magicItemsHolder.scaleY = value.scale;
        const totalWidth:Number = Align.horizontal(Vector.<DisplayObject>(magicItems), Layout.itemSize, Layout.itemGap) * value.scale;
        magicItemsHolder.x = value.screenCenterX - totalWidth / 2;
        magicItemsHolder.y = value.contentCenterY;

        alignTitle();
    }

    private function alignTitle():void {
        titleTextField.scaleX = titleTextField.scaleY = _layout.scale;
        const titlePos:Point = _layout.title(titleTextField.width, titleTextField.height);
        titleTextField.x = titlePos.x;
        titleTextField.y = titlePos.y;
    }

    private function onClick(event:MouseEvent):void {
        const item:ShopMagicItem = ShopMagicItem(event.target);
        dispatchEvent(new MagicItemClickEvent(item.itemType));
    }

    override public function set lock(value:Boolean):void {
        for each(var item:ShopMagicItem in magicItems) item.lock = value;
    }

    override public function get titleContent():DisplayObject {
        return titleTextField;
    }
}
}
