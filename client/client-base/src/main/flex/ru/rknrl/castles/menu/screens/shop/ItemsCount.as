package ru.rknrl.castles.menu.screens.shop {
import ru.rknrl.dto.ItemDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.ItemsDTO;

public class ItemsCount {
    private var _itemsDto:ItemsDTO;

    public function ItemsCount(itemsDto:ItemsDTO) {
        _itemsDto = itemsDto;
    }

    public function getCount(itemType:ItemType):int {
        for each(var item:ItemDTO in _itemsDto.items) {
            if (item.type == itemType) return item.count;
        }
        return 0;
    }
}
}
