//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.core.Periodic;
import protos.ItemStateDTO;

public class ItemState {
    private var _count:int;

    public function get count():int {
        return _count;
    }

    private var _cooldown:Periodic;

    public function get cooldown():Periodic {
        return _cooldown;
    }

    public function canUse(time:int):Boolean {
        return _count > 0 && _cooldown.isFinish(time);
    }

    public function ItemState(dto:ItemStateDTO, startTime:int):void {
        _count = dto.count;
        _cooldown = new Periodic(startTime - dto.millisFromStart, dto.cooldownDuration)
    }
}
}
