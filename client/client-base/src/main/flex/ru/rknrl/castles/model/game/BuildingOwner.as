//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.dto.PlayerId;

public class BuildingOwner {
    private var _hasOwner:Boolean;

    public function get hasOwner():Boolean {
        return _hasOwner;
    }

    private var _ownerId:PlayerId;

    public function get ownerId():PlayerId {
        if (!hasOwner) throw new Error("hasn't owner");
        return _ownerId;
    }

    public function BuildingOwner(hasOwner:Boolean, ownerId:PlayerId = null) {
        _hasOwner = hasOwner;
        _ownerId = ownerId;
    }

    public function equals(owner:BuildingOwner):Boolean {
        if (hasOwner != owner.hasOwner) return false;
        return !hasOwner && !owner.hasOwner || (ownerId.id == owner.ownerId.id);
    }

    public function equalsId(playerId:PlayerId):Boolean {
        return hasOwner && ownerId.id == playerId.id;
    }
}
}
