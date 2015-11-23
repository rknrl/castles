//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import protos.BuildingId;
import protos.BuildingLevel;
import protos.BuildingType;
import protos.CellSize;
import protos.SlotId;
import protos.SlotsOrientation;
import protos.SlotsPos;

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

public class GameArea extends Sprite {
    private var _h:int;

    public function get h():int {
        return _h;
    }

    private var _v:int;

    public function get v():int {
        return _v;
    }

    private var ground:Ground;
    public const buildings:BuildingsView = new BuildingsView();
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
        addChild(ground = new Ground(h, v));
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
        ground.updateGroundColor(pos, owner);
        buildings.addBuilding(id, buildingType, buildingLevel, owner, count, strengthened, pos);
    }

    public function setBuildingOwner(id:BuildingId, owner:BuildingOwner):void {
        buildings.setBuildingOwner(id, owner);
        ground.updateGroundColor(buildings.byId(id).pos, owner);
    }

    public function addSlots(dto:SlotsPos):void {
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
