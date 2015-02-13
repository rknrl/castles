//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.shop {
import ru.rknrl.dto.ItemDTO;
import ru.rknrl.dto.ItemType;

public class ItemsCount {
    private var _itemsDto:Vector.<ItemDTO>;

    public function ItemsCount(itemsDto:Vector.<ItemDTO>) {
        _itemsDto = itemsDto;
    }

    public function getCount(itemType:ItemType):int {
        for each(var item:ItemDTO in _itemsDto) {
            if (item.type == itemType) return item.count;
        }
        throw new Error("can't find item " + itemType);
    }
}
}
