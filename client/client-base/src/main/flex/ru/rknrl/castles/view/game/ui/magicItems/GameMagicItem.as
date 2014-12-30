package ru.rknrl.castles.view.game.ui.magicItems {
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.dto.ItemType;

public class GameMagicItem extends Sprite {
    private var fly:Fly;
    private var magicItem:GameMagicItemIcon;

    public function GameMagicItem(itemType:ItemType, count:int) {
        _itemType = itemType;

        mouseChildren = false;

        const shadow:Shadow = new Shadow();
        shadow.y = Layout.shadowDistance;
        addChild(shadow);

        magicItem = new GameMagicItemIcon(itemType, count);
        addChild(magicItem);

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
        scaleX = scaleY = value ? 2 : 1;
    }

    public function set lock(value:Boolean):void {
        mouseEnabled = !value;
    }
}
}
