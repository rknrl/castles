package ru.rknrl.castles.game {
public class BuildingOwner {
    private var _hasOwner:Boolean;

    public function get hasOwner():Boolean {
        return _hasOwner;
    }

    private var _ownerId:int;

    public function get ownerId():int {
        if (!hasOwner) throw new Error("hasn't owner");
        return _ownerId;
    }

    public function BuildingOwner(hasOwner:Boolean, ownerId:int = -1) {
        _hasOwner = hasOwner;
        _ownerId = ownerId;
    }

    public function equals(owner:BuildingOwner):Boolean {
        if (hasOwner != owner.hasOwner) return false;
        return !hasOwner && !owner.hasOwner || (ownerId == owner.ownerId);
    }
}
}
