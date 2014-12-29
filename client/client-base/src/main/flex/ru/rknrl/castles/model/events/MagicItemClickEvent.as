package ru.rknrl.castles.model.events {
import flash.events.Event;

import ru.rknrl.dto.ItemType;

public class MagicItemClickEvent extends Event {
    public static const MAGIC_ITEM_CLICK:String = "magicItemClick";

    private var _itemType:ItemType;

    public function get itemType():ItemType {
        return _itemType;
    }

    public function MagicItemClickEvent(itemType:ItemType) {
        _itemType = itemType;
        super(MAGIC_ITEM_CLICK, true);
    }
}
}
