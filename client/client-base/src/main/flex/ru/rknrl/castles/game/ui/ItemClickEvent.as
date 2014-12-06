package ru.rknrl.castles.game.ui {
import flash.events.Event;

public class ItemClickEvent extends Event {
    public static const ITEM_CLICK:String = "itemClick";

    private var _gameItem:GameItem;

    public function get gameItem():GameItem {
        return _gameItem;
    }

    public function ItemClickEvent(gameItem:GameItem) {
        super(ITEM_CLICK);
        _gameItem = gameItem;
    }
}
}
