//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.dto.PlayerIdDTO;

public class BuildingOwner {
    private var _hasOwner:Boolean;

    public function get hasOwner():Boolean {
        return _hasOwner;
    }

    private var _ownerId:PlayerIdDTO;

    public function get ownerId():PlayerIdDTO {
        if (!hasOwner) throw new Error("hasn't owner");
        return _ownerId;
    }

    public function BuildingOwner(hasOwner:Boolean, ownerId:PlayerIdDTO = null) {
        _hasOwner = hasOwner;
        _ownerId = ownerId;
    }

    public function equals(owner:BuildingOwner):Boolean {
        if (hasOwner != owner.hasOwner) return false;
        return !hasOwner && !owner.hasOwner || (ownerId.id == owner.ownerId.id);
    }

    public function equalsId(playerId:PlayerIdDTO):Boolean {
        return hasOwner && ownerId.id == playerId.id;
    }
}
}
