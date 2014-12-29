package ru.rknrl.castles.controller.game {
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.game.Buildings;
import ru.rknrl.castles.model.game.GameMagicItems;
import ru.rknrl.castles.rmi.GameFacadeSender;
import ru.rknrl.castles.rmi.IGameFacade;
import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingUpdateDTO;
import ru.rknrl.dto.BulletDTO;
import ru.rknrl.dto.CastTorandoDTO;
import ru.rknrl.dto.FireballDTO;
import ru.rknrl.dto.GameOverDTO;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.ItemsStateDTO;
import ru.rknrl.dto.MoveDTO;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.StartLocationPosDTO;
import ru.rknrl.dto.TornadoDTO;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitIdDTO;
import ru.rknrl.dto.UnitUpdateDTO;
import ru.rknrl.dto.VolcanoDTO;

public class GameController implements IGameFacade {
    private var view:GameView;
    private var sender:GameFacadeSender;
    private var selfId:PlayerIdDTO;

    private var bullets:Bullets;
    private var fireballs:Fireballs;
    private var tornadoes:Tornadoes;
    private var volcanoes:Volcanoes;
    private var units:Units;
    private var arrows:Arrows;
    private var tornadoPath:TornadoPath;
    private var magicItems:MagicItems;

    public function GameController(view:GameView,
                                   sender:GameFacadeSender,
                                   gameState:GameStateDTO) {
        this.view = view;
        this.sender = sender;

        selfId = gameState.selfId;

        bullets = new Bullets(view.area.bullets);
        fireballs = new Fireballs(view.area.fireballs, gameState.width, gameState.height);
        tornadoes = new Tornadoes(view.area.tornadoes);
        volcanoes = new Volcanoes(view.area.volcanoes);
        units = new Units(view.area.units);
        arrows = new Arrows(view.area.arrows);
        tornadoPath = new TornadoPath(view.area.tornadoPath);
        magicItems = new MagicItems(view.ui.magicItems);

        onUpdateItemStates(gameState.itemsState);

        for each(var startLocationPos:StartLocationPosDTO in gameState.startLocations) view.area.addStartLocation(startLocationPos);

        var buildingList:Vector.<Building> = new <Building>[];
        for each(var b:BuildingDTO in gameState.buildings) {
            const owner:BuildingOwner = new BuildingOwner(b.hasOwner, b.owner);
            const pos:Point = new Point(b.x, b.y);
            buildingList.push(new Building(b.id, pos, owner));
            view.area.addBuilding(b.id, b.building.type, b.building.level, owner, b.population, b.strengthened, pos);
        }
        buildings = new Buildings(buildingList);

        for each(var unit:UnitDTO in gameState.units) onAddUnit(unit);
        for each(var fireball:FireballDTO in gameState.fireballs) onAddFireball(fireball);
        for each(var tornado:TornadoDTO in gameState.tornadoes) onAddTornado(tornado);
        for each(var volcano:VolcanoDTO in gameState.volcanoes) onAddVolcano(volcano);
        for each(var bullet:BulletDTO in gameState.bullets) onAddBullet(bullet);

        view.addEventListener(GameViewEvents.SURRENDER, onSurrender);
        view.addEventListener(GameViewEvents.LEAVE_BUTTON_CLICK, onLeaveButtonClick);
        view.addEventListener(MagicItemClickEvent.MAGIC_ITEM_CLICK, onMagicItemClick);

        view.addEventListener(Event.ENTER_FRAME, onEnterFrame);
        view.addEventListener(GameMouseEvent.MOUSE_DOWN, onMouseDown);
        view.addEventListener(GameMouseEvent.MOUSE_MOVE, onMouseMove);
        view.addEventListener(GameMouseEvent.MOUSE_UP, onMouseUp);
    }

    public function destroy():void {

// todo
    }

    private function onEnterFrame(event:Event):void {
        update(getTimer());
    }

    private function update(time:int):void {
        units.update(time);
        fireballs.update(time);
        volcanoes.update(time);
        tornadoes.update(time);
        bullets.update(time);

        for each(var itemType:ItemType in ItemType.values) {
            view.ui.magicItems.setItemCooldown(itemType, magicItemStates.cooldownProgress(itemType, time))
        }
    }

    public function onAddUnit(dto:UnitDTO):void {
        const endPos:Point = buildings.byId(dto.targetBuildingId).pos;
        units.add(endPos, dto);
    }

    public function onUpdateUnit(dto:UnitUpdateDTO):void {
        units.updateUnit(dto);
    }

    public function onRemoveUnit(id:UnitIdDTO):void {
        units.remove(id);
    }

    public function onAddFireball(dto:FireballDTO):void {
        fireballs.add(dto);
    }

    public function onAddVolcano(dto:VolcanoDTO):void {
        volcanoes.add(dto);
    }

    public function onAddTornado(dto:TornadoDTO):void {
        tornadoes.add(dto);
    }

    public function onAddBullet(dto:BulletDTO):void {
        const time:int = getTimer();
        const startPos:Point = buildings.byId(dto.buildingId).pos;
        const endPos:Point = units.getUnit(dto.unitId).pos(time + dto.duration);
        bullets.add(time, startPos, endPos, dto.duration);
    }

    private var buildings:Buildings;

    public function onUpdateBuilding(dto:BuildingUpdateDTO):void {
        const owner:BuildingOwner = new BuildingOwner(dto.hasOwner, dto.owner);
        buildings.byId(dto.id).owner = owner;

        view.area.setBuildingCount(dto.id, dto.population);
        view.area.setBuildingOwner(dto.id, owner);
        view.area.setBuildingStrengthened(dto.id, dto.strengthened);
    }

    // magic items

    private var magicItemStates:GameMagicItems;

    public function onUpdateItemStates(dto:ItemsStateDTO):void {
        magicItemStates = new GameMagicItems(dto);

        for each(var itemType:ItemType in ItemType.values) {
            view.ui.magicItems.setItemCount(itemType, magicItemStates.count(itemType))
        }
        view.ui.magicItems.lock = false;
    }

    private function onMagicItemClick(event:MagicItemClickEvent):void {
        if (magicItemStates.canUse(event.itemType, getTimer())) {
            if (magicItems.selected == event.itemType) {
                magicItems.selected = null;
            } else {
                magicItems.selected = event.itemType;
            }
        }
    }

    // game over

    public function onGameOver(dto:GameOverDTO):void {
        if (dto.playerId.id == selfId.id) {
            view.openGameOverScreen();
        }
    }

    private function onSurrender(event:Event):void {
        sender.surrender();
    }

    private function onLeaveButtonClick(event:Event):void {
        sender.leave();
    }

    // mouse

    private function onMouseDown(event:GameMouseEvent):void {
        if (magicItems.selected) {
            itemMouseDown(event.mousePos);
        } else {
            const building:Building = buildings.selfInXy(selfId, event.mousePos);
            if (building) arrows.startDraw(building);
        }
    }

    private function onMouseMove(event:GameMouseEvent):void {
        if (magicItems.selected) {
            tornadoPath.mouseMove(event.mousePos);
        } else {
            if (arrows.drawing) {
                const building:Building = buildings.selfInXy(selfId, event.mousePos);
                if (building) arrows.addArrow(building);
                arrows.mouseMove(event.mousePos);
            }
        }
    }

    private function onMouseUp(event:GameMouseEvent):void {
        if (magicItems.selected) {
            if (tornadoPath.drawing) {
                const gameState:CastTorandoDTO = new CastTorandoDTO();
                gameState.points = Point.pointsToDto(tornadoPath.points);
                sender.castTornado(gameState);

                magicItems.useItem();
                tornadoPath.endDraw()
            }
        } else {
            if (arrows.drawing) {
                const toBuilding:Building = buildings.inXy(event.mousePos);

                if (toBuilding) {
                    const filteredIds:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];
                    for each(var id:BuildingIdDTO in arrows.fromBuildingIds) {
                        if (id.id != toBuilding.id.id) {
                            filteredIds.push(id);
                        }
                    }

                    if (filteredIds.length > 0) {
                        const dto:MoveDTO = new MoveDTO();
                        dto.toBuilding = toBuilding.id;
                        dto.fromBuildings = filteredIds;
                        sender.move(dto);
                    }
                }

                arrows.endDraw();
            }
        }
    }

    private function itemMouseDown(mousePos:Point):void {
        switch (magicItems.selected) {
            case ItemType.FIREBALL:
                sender.castFireball(mousePos.dto());
                magicItems.useItem();
                break;
            case ItemType.STRENGTHENING:
                const strBuilding:Building = buildings.selfInXy(selfId, mousePos);
                if (strBuilding) {
                    sender.castStrengthening(strBuilding.id);
                    magicItems.useItem();
                }
                break;
            case ItemType.VOLCANO:
                sender.castVolcano(mousePos.dto());
                magicItems.useItem();
                break;
            case ItemType.TORNADO:
                tornadoPath.startDraw(mousePos);
                break;
            case ItemType.ASSISTANCE:
                const building:Building = buildings.selfInXy(selfId, mousePos);
                if (building) {
                    sender.castAssistance(building.id);
                    magicItems.useItem();
                }
                break;
        }
    }
}
}
