package ru.rknrl.castles.model.game {
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.dto.BuildingIdDTO;

public class Building {
    private var _id:BuildingIdDTO;

    public function get id():BuildingIdDTO {
        return _id;
    }

    private var _pos:Point;

    public function get pos():Point {
        return _pos;
    }

    private var _owner:BuildingOwner;

    public function get owner():BuildingOwner {
        return _owner;
    }

    private var _strengthened:Boolean;

    public function get strengthened():Boolean {
        return _strengthened;
    }

    public function update(owner:BuildingOwner, strengthened:Boolean):void {
        _owner = owner;
        _strengthened = strengthened;
    }

    public function Building(id:BuildingIdDTO, pos:Point, owner:BuildingOwner, strengthened:Boolean) {
        _id = id;
        _pos = pos;
        _owner = owner;
        _strengthened = strengthened;
    }
}
}
