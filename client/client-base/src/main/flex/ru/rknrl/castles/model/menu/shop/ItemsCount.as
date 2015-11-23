//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.shop {
import protos.Item;
import protos.ItemType;

public class ItemsCount {
    private var _itemsDto:Vector.<Item>;

    public function ItemsCount(itemsDto:Vector.<Item>) {
        _itemsDto = itemsDto;
    }

    public function getCount(itemType:ItemType):int {
        for each(var item:Item in _itemsDto) {
            if (item.itemType == itemType) return item.count;
        }
        throw new Error("can't find item " + itemType);
    }
}
}
