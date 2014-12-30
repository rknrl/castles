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
import ru.rknrl.dto.StartLocationOrientation;
import ru.rknrl.dto.StartLocationPosDTO;

public class GameArea extends Sprite {
    public var ground:Ground;
    public var volcanoes:VolcanoesView;
    public var arrows:ArrowsView;
    private var buildings:BuildingsView;
    public var units:UnitsView;
    public var tornadoes:TornadoesView;
    public var bullets:BulletsView;
    public var fireballs:FireballsView;
    public var explosions:ExplosionsView;
    public var tornadoPath:TornadoPathView;

    public function GameArea(h:int, v:int) {
        addChild(ground = new Ground(h, v));
        addChild(volcanoes = new VolcanoesView());
        addChild(arrows = new ArrowsView());
        addChild(buildings = new BuildingsView());
        addChild(units = new UnitsView());
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

    public function addStartLocation(dto:StartLocationPosDTO):void {
        const cellSize:int = CellSize.SIZE.id();
        for each(var slotId:SlotId in SlotId.values) {
            const pos:Point = getSlotPos(slotId);
            const orientation:StartLocationOrientation = dto.orientation;
            const isMirrorH:Boolean = orientation == StartLocationOrientation.TOP_RIGHT || orientation == StartLocationOrientation.BOTTOM_RIGHT;
            const isMirrorV:Boolean = orientation == StartLocationOrientation.TOP_LEFT || orientation == StartLocationOrientation.TOP_RIGHT;
            const x:Number = isMirrorH ? dto.x - pos.x * cellSize : dto.x + pos.x * cellSize;
            const y:Number = isMirrorV ? dto.y - pos.y * cellSize : dto.y + pos.y * cellSize;
            ground.updateGroundColor(new Point(x, y), new BuildingOwner(true, dto.playerId));
        }
    }
}
}
