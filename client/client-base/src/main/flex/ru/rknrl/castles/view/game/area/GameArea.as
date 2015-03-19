//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.display.Sprite;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.getSlotPos;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.game.area.buildings.BuildingsView;
import ru.rknrl.castles.view.game.area.units.BloodView;
import ru.rknrl.castles.view.game.area.units.UnitsView;
import ru.rknrl.castles.view.game.area.volcanoes.VolcanoesView;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.SlotsOrientation;
import ru.rknrl.dto.SlotsPosDTO;

public class GameArea extends Sprite {
    private var buildings:BuildingsView;

    private var _h:int;

    public function get h():int {
        return _h;
    }

    private var _v:int;

    public function get v():int {
        return _v;
    }

    private var _ground:Ground;

    public function get ground():Ground {
        return _ground;
    }

    private var _blood:BloodView;

    public function get blood():BloodView {
        return _blood;
    }

    private var _volcanoes:VolcanoesView;

    public function get volcanoes():VolcanoesView {
        return _volcanoes;
    }

    private var _arrows:ArrowsView;

    public function get arrows():ArrowsView {
        return _arrows;
    }

    private var _units:UnitsView;

    public function get units():UnitsView {
        return _units;
    }

    private var _tornadoes:TornadoesView;

    public function get tornadoes():TornadoesView {
        return _tornadoes;
    }

    private var _bullets:BulletsView;

    public function get bullets():BulletsView {
        return _bullets;
    }

    private var _fireballs:FireballsView;

    public function get fireballs():FireballsView {
        return _fireballs;
    }

    private var _explosions:ExplosionsView;

    public function get explosions():ExplosionsView {
        return _explosions;
    }

    private var _tornadoPath:TornadoPathView;

    public function get tornadoPath():TornadoPathView {
        return _tornadoPath;
    }

    public function GameArea(h:int, v:int) {
        _h = h;
        _v = v;
        addChild(_ground = new Ground(h, v));
        addChild(_blood = new BloodView());
        addChild(_volcanoes = new VolcanoesView());
        addChild(_arrows = new ArrowsView());
        addChild(_units = new UnitsView());
        addChild(buildings = new BuildingsView());
        addChild(_tornadoes = new TornadoesView());
        addChild(_bullets = new BulletsView());
        addChild(_fireballs = new FireballsView());
        addChild(_explosions = new ExplosionsView());
        addChild(_tornadoPath = new TornadoPathView());
    }

    public function addBuilding(id:BuildingIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point):void {
        _ground.updateGroundColor(pos, owner);
        buildings.addBuilding(id, buildingType, buildingLevel, owner, count, strengthened, pos);
    }

    public function setBuildingCount(id:BuildingIdDTO, count:int):void {
        buildings.setBuildingCount(id, count);
    }

    public function setBuildingDust(id:BuildingIdDTO, visible:Boolean):void {
        buildings.setBuildingsDust(id, visible);
    }

    public function setBuildingOwner(id:BuildingIdDTO, owner:BuildingOwner):void {
        buildings.setBuildingOwner(id, owner);
        _ground.updateGroundColor(buildings.byId(id).pos, owner);
    }

    public function setBuildingStrengthened(id:BuildingIdDTO, strengthened:Boolean):void {
        buildings.setBuildingStrengthened(id, strengthened);
    }

    public function addSlots(dto:SlotsPosDTO):void {
        const cellSize:int = CellSize.SIZE.id();
        for each(var slotId:SlotId in SlotId.values) {
            const pos:Point = getSlotPos(slotId);
            const orientation:SlotsOrientation = dto.orientation;
            const isMirrorH:Boolean = orientation == SlotsOrientation.TOP_RIGHT || orientation == SlotsOrientation.BOTTOM_RIGHT;
            const isMirrorV:Boolean = orientation == SlotsOrientation.TOP_LEFT || orientation == SlotsOrientation.TOP_RIGHT;
            const x:Number = isMirrorH ? dto.pos.x - pos.x * cellSize : dto.pos.x + pos.x * cellSize;
            const y:Number = isMirrorV ? dto.pos.y - pos.y * cellSize : dto.pos.y + pos.y * cellSize;
            _ground.updateGroundColor(new Point(x, y), new BuildingOwner(true, dto.playerId));
        }
    }
}
}
