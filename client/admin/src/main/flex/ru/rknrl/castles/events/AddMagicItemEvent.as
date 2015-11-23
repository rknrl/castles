//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.events {
import flash.events.Event;

import protos.ItemType;

public class AddMagicItemEvent extends Event {
    public static const ADD_MAGIC_ITEM:String = "addMagicItem";

    private var _itemType:ItemType;

    public function get itemType():ItemType {
        return _itemType;
    }

    private var _amount:int;

    public function get amount():int {
        return _amount;
    }

    public function AddMagicItemEvent(itemType:ItemType, amount:int) {
        super(ADD_MAGIC_ITEM, true);
        _itemType = itemType;
        _amount = amount;
    }
}
}
