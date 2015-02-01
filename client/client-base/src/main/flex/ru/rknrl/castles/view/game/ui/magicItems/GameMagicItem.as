package ru.rknrl.castles.view.game.ui.magicItems {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.AnimatedShadow;
import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.dto.ItemType;

public class GameMagicItem extends Sprite {
    private static const mouseHolderW:Number = Layout.itemSize + Layout.itemGap;
    private static const mouseHolderH:Number = 96;

    private var fly:Fly;
    private var holder:Sprite;
    private var magicItem:GameMagicItemIcon;
    private var shadow:AnimatedShadow;

    public function GameMagicItem(itemType:ItemType, count:int) {
        _itemType = itemType;

        mouseChildren = false;

        const mouseHolder:Bitmap = new Bitmap(Colors.transparent);
        mouseHolder.width = mouseHolderW;
        mouseHolder.height = mouseHolderH;
        mouseHolder.x = -mouseHolderW / 2;
        mouseHolder.y = -40;
        addChild(mouseHolder);

        addChild(holder = new Sprite());

        shadow = new AnimatedShadow();
        shadow.y = Layout.shadowDistance;
        holder.addChild(shadow);

        magicItem = new GameMagicItemIcon(itemType, count);
        holder.addChild(magicItem);

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

    public function set cooldownProgress(value:Number):void {
        magicItem.cooldownProgress = value;
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
    }

    public function set selected(value:Boolean):void {
        holder.scaleX = holder.scaleY = value ? 1.8 : 1;
    }

    public function set lock(value:Boolean):void {
        mouseEnabled = !value;
    }

    public function animate():void {
        shadow.bounce();
        magicItem.bounce();
    }
}
}
