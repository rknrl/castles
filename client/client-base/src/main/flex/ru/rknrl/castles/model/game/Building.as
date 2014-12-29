package ru.rknrl.castles.model.game {
import ru.rknrl.castles.utils.points.Point;
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

    public function set owner(value:BuildingOwner):void {
        _owner = value;
    }

    public function get owner():BuildingOwner {
        return _owner;
    }

    public function Building(id:BuildingIdDTO, pos:Point, owner:BuildingOwner) {
        _id = id;
        _pos = pos;
        _owner = owner;
    }
}
}
