package ru.rknrl.castles.view.game.area {
import flash.display.Sprite;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.getSlotPos;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.game.area.buildings.BuildingsView;
import ru.rknrl.castles.view.game.area.bullets.BulletsView;
import ru.rknrl.castles.view.game.area.explosions.ExplosionsView;
import ru.rknrl.castles.view.game.area.fireballs.FireballsView;
import ru.rknrl.castles.view.game.area.tornadoes.TornadoesView;
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
    public var ground:Ground;
    public var volcanoes:VolcanoesView;
    public var arrows:ArrowsView;
    public var units:UnitsView;
    private var buildings:BuildingsView;
    public var tornadoes:TornadoesView;
    public var bullets:BulletsView;
    public var fireballs:FireballsView;
    public var explosions:ExplosionsView;
    public var tornadoPath:TornadoPathView;

    private var _h:int;

    public function get h():int {
        return _h;
    }

    private var _v:int;

    public function get v():int {
        return _v;
    }

    public function GameArea(h:int, v:int) {
        _h = h;
        _v = v;
        addChild(ground = new Ground(h, v));
        addChild(volcanoes = new VolcanoesView());
        addChild(arrows = new ArrowsView());
        addChild(units = new UnitsView());
        addChild(buildings = new BuildingsView());
        addChild(tornadoes = new TornadoesView());
        addChild(bullets = new BulletsView());
        addChild(fireballs = new FireballsView());
        addChild(explosions = new ExplosionsView());
        addChild(tornadoPath = new TornadoPathView());
    }

    public function addBuilding(id:BuildingIdDTO, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point):void {
        ground.updateGroundColor(pos, owner);
        buildings.addBuilding(id, buildingType, buildingLevel, owner, count, strengthened, pos);
    }

    public function setBuildingCount(id:BuildingIdDTO, count:int):void {
        buildings.setBuildingCount(id, count);
    }

    public function setBuildingOwner(id:BuildingIdDTO, owner:BuildingOwner):void {
        buildings.setBuildingOwner(id, owner);
        ground.updateGroundColor(buildings.byId(id).pos, owner);
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
            ground.updateGroundColor(new Point(x, y), new BuildingOwner(true, dto.playerId));
        }
    }
}
}
