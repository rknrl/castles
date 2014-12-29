package ru.rknrl.castles.model.game {
import flash.utils.getTimer;

import ru.rknrl.dto.ItemStateDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.ItemsStateDTO;

public class GameMagicItems {
    private var dto:ItemsStateDTO;
    private var startTime:int;

    public function GameMagicItems(dto:ItemsStateDTO) {
        this.dto = dto;
        this.startTime = getTimer();
    }

    private function getItemState(itemType:ItemType):ItemStateDTO {
        for each(var itemState:ItemStateDTO in dto.items) {
            if (itemState.itemType == itemType) return itemState;
        }
        throw new Error("can't find item state " + itemType);
    }

    public function count(itemType:ItemType):int {
        return getItemState(itemType).count;
    }

    public function cooldownProgress(itemType:ItemType, time:int):Number {
        const state:ItemStateDTO = getItemState(itemType);
        const elapsed:int = time - startTime + (state.cooldownDuration - state.millisTillCooldownEnd);
        var progress:Number = elapsed / state.cooldownDuration;
        if (progress > 1) progress = 1;
        if (progress < 0) progress = 0;
        return progress;
    }

    public function canUse(itemType:ItemType, time:int):Boolean {
        return count(itemType) > 0 && cooldownProgress(itemType, time) == 1;
    }
}
}
