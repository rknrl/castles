//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.ui.magicItems {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;

import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.view.layout.Layout;
import protos.ItemType;
import ru.rknrl.display.Align;

public class MagicItemsView extends Sprite {
    private var holder:Sprite = new Sprite();

    private const magicItems:Vector.<GameMagicItem> = new <GameMagicItem>[];

    private function getMagicItem(itemType:ItemType):GameMagicItem {
        for each(var item:GameMagicItem in magicItems) {
            if (item.itemType == itemType) return item;
        }
        throw new Error("can't find magic item " + itemType);
    }

    public function MagicItemsView(layout:Layout) {
        addChild(holder = new Sprite());

        for each(var itemType:ItemType in ItemType.values) {
            const item:GameMagicItem = new GameMagicItem(itemType, 1);
            item.addEventListener(MouseEvent.MOUSE_DOWN, onClick);
            holder.addChild(item);
            magicItems.push(item);
        }

        this.layout = layout;
    }

    public function set layout(value:Layout):void {
        holder.scaleX = holder.scaleY = value.scale;
        const totalWidth:Number = Align.horizontal(Vector.<DisplayObject>(magicItems), Layout.itemSize, Layout.itemGap) * value.scale;
        holder.x = value.screenCenterX - totalWidth / 2;
        holder.y = value.gameMagicItemsY;
    }

    public function setItemCount(itemType:ItemType, count:int):void {
        getMagicItem(itemType).count = count;
    }

    public function setItemCooldown(itemType:ItemType, progress:Number):void {
        getMagicItem(itemType).cooldownProgress = progress;
    }

    private var selectedItem:GameMagicItem;

    public function set selected(value:ItemType):void {
        if (selectedItem) selectedItem.selected = false;
        selectedItem = value ? getMagicItem(value) : null;
        if (selectedItem) selectedItem.selected = true;
    }

    public function set lock(value:Boolean):void {
        for each(var item:GameMagicItem in magicItems) item.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        event.stopImmediatePropagation();
        const item:GameMagicItem = GameMagicItem(event.target);
        dispatchEvent(new MagicItemClickEvent(item.itemType));
    }

    public function useItem(itemType:ItemType):void {
        var item:GameMagicItem = getMagicItem(itemType);
        item.animate();
        item.cooldownProgress = 0;
    }

    public function tutorLock(itemType:ItemType, value:Boolean):void {
        var item:GameMagicItem = getMagicItem(itemType);
        item.tutorLock = value;
    }
}
}
