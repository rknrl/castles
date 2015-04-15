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
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.game.area.buildings.BuildingsView;
import ru.rknrl.castles.view.game.area.factories.BulletViewFactory;
import ru.rknrl.castles.view.game.area.factories.ExplosionViewFactory;
import ru.rknrl.castles.view.game.area.factories.FireballViewFactory;
import ru.rknrl.castles.view.game.area.factories.TornadoViewFactory;
import ru.rknrl.castles.view.game.area.factories.VolcanoViewFactory;
import ru.rknrl.castles.view.game.area.units.BloodView;
import ru.rknrl.core.GameObjectViewFactory;
import ru.rknrl.core.points.Point;
import ru.rknrl.dto.BuildingId;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.SlotsOrientation;
import ru.rknrl.dto.SlotsPosDTO;

public class GameArea extends Sprite {
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

    private const buildings:BuildingsView = new BuildingsView();
    public const blood:BloodView = new BloodView();
    public const arrows:ArrowsView = new ArrowsView();
    public const units:Sprite = new Sprite();
    public const volcanoes:Sprite = new Sprite();
    public const tornadoes:Sprite = new Sprite();
    public const bullets:Sprite = new Sprite();
    public const fireballs:Sprite = new Sprite();
    public const explosions:ExplosionsView = new ExplosionsView();
    public const tornadoPath:TornadoPathView = new TornadoPathView();

    public const bulletsFactory:GameObjectViewFactory = new BulletViewFactory();
    public const explosionsFactory:GameObjectViewFactory = new ExplosionViewFactory();
    public const fireballsFactory:GameObjectViewFactory = new FireballViewFactory();
    public const tornadoesFactory:GameObjectViewFactory = new TornadoViewFactory();
    public const volcanoesFactory:GameObjectViewFactory = new VolcanoViewFactory();

    public function GameArea(h:int, v:int) {
        _h = h;
        _v = v;
        addChild(_ground = new Ground(h, v));
        addChild(blood);
        addChild(volcanoes);
        addChild(arrows);
        addChild(units);
        addChild(buildings);
        addChild(tornadoes);
        addChild(bullets);
        addChild(fireballs);
        addChild(explosions);
        addChild(tornadoPath);
    }

    public function addBuilding(id:BuildingId, buildingType:BuildingType, buildingLevel:BuildingLevel, owner:BuildingOwner, count:int, strengthened:Boolean, pos:Point):void {
        _ground.updateGroundColor(pos, owner);
        buildings.addBuilding(id, buildingType, buildingLevel, owner, count, strengthened, pos);
    }

    public function setBuildingCount(id:BuildingId, count:int):void {
        buildings.setBuildingCount(id, count);
    }

    public function setBuildingDust(id:BuildingId, visible:Boolean):void {
        buildings.setBuildingsDust(id, visible);
    }

    public function setBuildingOwner(id:BuildingId, owner:BuildingOwner):void {
        buildings.setBuildingOwner(id, owner);
        _ground.updateGroundColor(buildings.byId(id).pos, owner);
    }

    public function setBuildingStrengthened(id:BuildingId, strengthened:Boolean):void {
        buildings.setBuildingStrengthened(id, strengthened);
    }

    public function tutorBlur(buildingIds:Vector.<BuildingId>):void {
        buildings.tutorBlur(buildingIds);
    }

    public function tutorUnblur():void {
        buildings.tutorUnblur();
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
