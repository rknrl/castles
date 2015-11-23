//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import flash.utils.Dictionary;

import protos.ItemStateDTO;
import protos.ItemStatesDTO;
import protos.ItemType;

public class ItemStates {
    private const itemTypeToState:Dictionary = new Dictionary();

    public function ItemStates(dto:ItemStatesDTO, startTime:int) {
        for each(var state:ItemStateDTO in dto.items) {
            itemTypeToState[state.itemType] = new ItemState(state, startTime);
        }
    }

    public function get(itemType:ItemType):ItemState {
        const state:ItemState = itemTypeToState[itemType];
        if (!state)throw new Error("can't find item state " + itemType);
        return state;
    }
}
}