//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.getTimer;

import ru.rknrl.Log;
import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameTutorEvents;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.game.Buildings;
import ru.rknrl.castles.model.game.ItemStates;
import ru.rknrl.castles.model.game.Players;
import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.core.GameObjectsController;
import ru.rknrl.core.Movable;
import ru.rknrl.core.Static;
import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingId;
import ru.rknrl.dto.BuildingUpdateDTO;
import ru.rknrl.dto.BulletDTO;
import ru.rknrl.dto.CastTornadoDTO;
import ru.rknrl.dto.FireballDTO;
import ru.rknrl.dto.GameOverDTO;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.GameStateUpdateDTO;
import ru.rknrl.dto.ItemStatesDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.MoveDTO;
import ru.rknrl.dto.PlayerDTO;
import ru.rknrl.dto.PlayerId;
import ru.rknrl.dto.SlotsPosDTO;
import ru.rknrl.dto.TornadoDTO;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitId;
import ru.rknrl.dto.UnitUpdateDTO;
import ru.rknrl.dto.VolcanoDTO;
import ru.rknrl.rmi.GameOverEvent;
import ru.rknrl.rmi.GameStateUpdatedEvent;
import ru.rknrl.rmi.Server;

public class GameController extends EventDispatcher {
    private var width:Number;
    private var height:Number;
    private var view:GameView;
    private var server:Server;
    private var selfId:PlayerId;

    private var bullets:GameObjectsController;
    private var fireballs:FireballsController;
    private var tornadoes:GameObjectsController;
    private var volcanoes:VolcanoesController;
    private var units:UnitsController;
    private var arrows:Arrows;
    private var tornadoPath:TornadoPath;
    private var magicItems:MagicItems;

    private var tutor:GameTutorController;
    private var players:Players;

    public function GameController(view:GameView,
                                   server:Server,
                                   gameState:GameStateDTO,
                                   isFirstGame:Boolean) {
        this.view = view;
        this.server = server;

        width = gameState.width;
        height = gameState.height;

        selfId = gameState.selfId;
        players = new Players(gameState.players, selfId);

        bullets = new GameObjectsController(view.area.bullets);
        fireballs = new FireballsController(view.area.fireballs, view.area.explosions, view.area.explosionsFactory);
        tornadoes = new GameObjectsController(view.area.tornadoes);
        volcanoes = new VolcanoesController(view.area.volcanoes);
        units = new UnitsController(view.area.units, view.area.blood);
        arrows = new Arrows(view.area.arrows);
        tornadoPath = new TornadoPath(view.area.tornadoPath);
        magicItems = new MagicItems(view.magicItems);

        updateItemStates(gameState.itemStates);

        for each(var slotsPos:SlotsPosDTO in gameState.slots) view.area.addSlots(slotsPos);

        const buildingList:Vector.<Building> = new <Building>[];
        for each(var b:BuildingDTO in gameState.buildings) {
            buildingList.push(Building.fromDto(b));

            const owner:BuildingOwner = new BuildingOwner(b.hasOwner, b.owner);
            const pos:Point = Point.fromDto(b.pos);
            view.area.addBuilding(b.id, b.building.buildingType, b.building.buildingLevel, owner, b.population, b.strengthened, pos);
        }
        buildings = new Buildings(buildingList);

        for each(var unit:UnitDTO in gameState.units) addUnit(unit);
        for each(var fireball:FireballDTO in gameState.fireballs) addFireball(fireball);
        for each(var tornado:TornadoDTO in gameState.tornadoes) addTornado(tornado);
        for each(var volcano:VolcanoDTO in gameState.volcanoes) addVolcano(volcano);
        for each(var bullet:BulletDTO in gameState.bullets) addBullet(bullet);
        for each(var gameOverDto:GameOverDTO in gameState.gameOvers) gameOver(gameOverDto);

        view.addEventListener(GameViewEvents.SURRENDER, onSurrender);
        view.addEventListener(GameViewEvents.LEAVE_BUTTON_CLICK, onLeaveButtonClick);
        view.addEventListener(MagicItemClickEvent.MAGIC_ITEM_CLICK, onMagicItemClick);

        view.addEventListener(GameMouseEvent.ENTER_FRAME, onEnterFrame);
        view.addEventListener(GameMouseEvent.MOUSE_DOWN, onMouseDown);
        view.addEventListener(GameMouseEvent.MOUSE_UP, onMouseUp);

        server.addEventListener(GameStateUpdatedEvent.GAMESTATEUPDATED, onGameStateUpdated);
        server.addEventListener(GameOverEvent.GAMEOVER, onGameOver);

        // Человек мог играть на компе, а потом перезайти в бой на мобиле
        if (gameState.players.length > view.supportedPlayersCount) onSurrender();

        if (isFirstGame) {
            tutor = new GameTutorController(view, this, players, buildings, server);
            view.y += 32; // todo
        }
        view.addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
    }

    public function destroy():void {
        Log.info("game destroy");

        server.removeEventListener(GameStateUpdatedEvent.GAMESTATEUPDATED, onGameStateUpdated);
        server.removeEventListener(GameOverEvent.GAMEOVER, onGameOver);
    }

    private function onAddedToStage(event:Event):void {
        view.removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        if (tutor) view.tutor.play(tutor.firstGame());
    }

    private function onEnterFrame(event:GameMouseEvent):void {
        const time:int = getTimer();
        update(time);

        if (magicItems.selected) {
            tornadoPath.mouseMove(event.mousePos);
        } else {
            if (arrows.drawing) {
                const building:Building = buildings.selfInXy(selfId, event.mousePos);
                if (building) arrows.addArrow(building.id.id, building.pos);

                arrows.mouseMove(event.mousePos);
            }
        }
    }

    private function update(time:int):void {
        units.update(time);
        fireballs.update(time);
        volcanoes.update(time);
        tornadoes.update(time);
        bullets.update(time);

        updateDustByVolcanoes(time);
        addKillsByTornadoes(time);

        for each(var itemType:ItemType in ItemType.values) {
            view.magicItems.setItemCooldown(itemType, magicItemStates.get(itemType).cooldown.progressInRange(time))
        }
    }

    private function updateDustByVolcanoes(time:int):void {
        for each(b in buildings.buildings) {
            view.area.buildings.setBuildingsDust(b.id, false);
        }

        const volcanoDamageRadius:int = 48;
        for (var key:* in volcanoes.objectToView) {
            const volcano:Static = key;
            const inRadius:Vector.<Building> = buildings.inRadius(volcano.pos(time), volcanoDamageRadius);
            for each(var b:Building in inRadius) {
                view.area.buildings.setBuildingsDust(b.id, true);
            }
        }
    }

    private var tornadoKillsLastTime:int;

    private function addKillsByTornadoes(time:int):void {
        if (time - tornadoKillsLastTime > 100) {
            tornadoKillsLastTime = time;

            const tornadoDamageRadius:int = 48;
            const damagedBuildings:Vector.<Building> = new <Building>[];
            for (var key:* in tornadoes.objectToView) {
                const tornado:Movable = key;
                const inRadius:Vector.<Building> = buildings.inRadius(tornado.pos(time), tornadoDamageRadius);
                for each(var b:Building in inRadius) {
                    if (damagedBuildings.indexOf(b) == -1) damagedBuildings.push(b);
                }
            }
            for each(b in damagedBuildings) {
                view.area.blood.addBlood(b.pos, b.owner.hasOwner ? Colors.playerColor(b.owner.ownerId) : Colors.noOwnerColor);
            }
        }
    }

    public function onGameStateUpdated(e:GameStateUpdatedEvent):void {
        const update:GameStateUpdateDTO = e.gameStateUpdate;
        for each(var newUnit:UnitDTO in update.newUnits) addUnit(newUnit);
        for each(var unitUpdate:UnitUpdateDTO in update.unitUpdates) updateUnit(unitUpdate);
        for each(var unitId:UnitId in update.killUnits) killUnit(unitId);
        for each(var buildingUpdate:BuildingUpdateDTO in update.buildingUpdates) updateBuilding(buildingUpdate);
        for each(var newFireball:FireballDTO in update.newFireballs) addFireball(newFireball);
        for each(var newVolcano:VolcanoDTO in update.newVolcanoes) addVolcano(newVolcano);
        for each(var newTornado:TornadoDTO in update.newTornadoes) addTornado(newTornado);
        for each(var newBullet:BulletDTO in update.newBullets) addBullet(newBullet);
        for each(var itemStateUpdate:ItemStatesDTO in update.itemStatesUpdates) updateItemStates(itemStateUpdate);
        for each(var newGameOver:GameOverDTO in update.newGameOvers) gameOver(newGameOver);
    }

    private function addUnit(dto:UnitDTO):void {
        const endPos:Point = buildings.byId(dto.targetBuildingId).pos;
        units.addUnit(getTimer(), endPos, dto);
    }

    private function updateUnit(dto:UnitUpdateDTO):void {
        units.updateUnit(getTimer(), dto);
    }

    private function killUnit(id:UnitId):void {
        units.kill(getTimer(), id);
    }

    private function addFireball(dto:FireballDTO):void {
        const time:int = getTimer();

        const fromLeft:Boolean = dto.pos.x > view.area.width / 2;
        const fromTop:Boolean = dto.pos.y > view.area.height / 2;
        const dx:Number = fromLeft ? dto.pos.x : view.area.width - dto.pos.x;
        const dy:Number = fromTop ? dto.pos.y : view.area.height - dto.pos.y;
        const d:Number = Math.max(dx, dy);

        const startPos:Point = new Point(fromLeft ? dto.pos.x - d : dto.pos.x + d, fromTop ? dto.pos.y - d : dto.pos.y + d);
        const endPos:Point = new Point(dto.pos.x, dto.pos.y);
        const points:Points = Points.two(startPos, endPos);

        const fireball:Movable = new Movable(points, time, dto.millisTillSplash);
        fireballs.add(time, fireball, view.area.fireballsFactory.create(time));
    }

    private function addVolcano(dto:VolcanoDTO):void {
        const time:int = getTimer();
        const volcano:Static = new Static(Point.fromDto(dto.pos), time, dto.millisTillEnd);
        volcanoes.add(getTimer(), volcano, view.area.volcanoesFactory.create(time));
    }

    private function addTornado(dto:TornadoDTO):void {
        const time:int = getTimer();
        const startTime:int = time - dto.millisFromStart;
        const duration:int = dto.millisFromStart + dto.millisTillEnd;
        const points:Points = Points.fromDto(dto.points);
        const tornado:Movable = new Movable(points, startTime, duration);
        tornadoes.add(time, tornado, view.area.tornadoesFactory.create(time));
    }

    private function addBullet(dto:BulletDTO):void {
        const unit:Unit = units.getUnit(dto.unitId);
        if (unit) {
            const time:int = getTimer();
            const startPos:Point = buildings.byId(dto.buildingId).pos;
            const endPos:Point = unit.pos(time + dto.duration);
            const points:Points = Points.two(startPos, endPos);
            const bullet:Movable = new Movable(points, time, dto.duration);
            bullets.add(time, bullet, view.area.bulletsFactory.create(time));
        }
    }

    private var buildings:Buildings;

    private function updateBuilding(dto:BuildingUpdateDTO):void {
        const building:Building = buildings.byId(dto.id);
        const newOwner:BuildingOwner = new BuildingOwner(dto.hasOwner, dto.owner);
        const wasOwned:Boolean = building.owner.equalsId(selfId);
        const willOwned:Boolean = newOwner.equalsId(selfId);

        const capture:Boolean = !wasOwned && (willOwned || dto.population < building.population);
        if (capture) dispatchEvent(new Event(GameTutorEvents.BUILDING_CAPTURED));

        building.update(newOwner, dto.population, dto.strengthened);

        view.area.buildings.setBuildingCount(dto.id, dto.population);
        view.area.setBuildingOwner(dto.id, newOwner);
        view.area.buildings.setBuildingStrengthened(dto.id, dto.strengthened);

        // Если ты вел стрелку из домика, а его захватили - убираем стрелку
        if (arrows.hasArrow(building.id.id) && (wasOwned && !willOwned)) {
            arrows.removeArrow(building.id.id)
        }
    }

    // magic items

    private var magicItemStates:ItemStates;

    private function updateItemStates(dto:ItemStatesDTO):void {
        if (dto.playerId.id == selfId.id) {
            magicItemStates = new ItemStates(dto, getTimer());

            for each(var itemType:ItemType in ItemType.values) {
                view.magicItems.setItemCount(itemType, magicItemStates.get(itemType).count)
            }
            view.magicItems.lock = false;
        }
    }

    private function onMagicItemClick(event:MagicItemClickEvent):void {
        const itemType:ItemType = event.itemType;
        if (magicItemStates.get(itemType).canUse(getTimer())) {
            if (magicItems.selected == itemType) {
                magicItems.selected = null;
            } else {
                magicItems.selected = itemType;
                dispatchEvent(new Event(GameTutorEvents.selected(itemType)));
            }
        }
    }

    // game over

    private function onGameOver(e:GameOverEvent):void {
        gameOver(e.gameOver);
    }

    private function gameOver(dto:GameOverDTO):void {
        if (dto.playerId.id == selfId.id) {
            view.tutor.clear();
            view.y = 0; // todo
            view.removeEventListener(GameViewEvents.SURRENDER, onSurrender);
            const winners:Vector.<PlayerDTO> = dto.place == 1 ? new <PlayerDTO>[players.getSelfPlayer()] : players.getEnemiesPlayers();
            const losers:Vector.<PlayerDTO> = dto.place == 1 ? players.getEnemiesPlayers() : new <PlayerDTO>[players.getSelfPlayer()];
            view.openGameOverScreen(PlayerInfo.fromDtoVector(winners), PlayerInfo.fromDtoVector(losers), dto.place == 1, dto.reward);
        } else {
            view.setDeadAvatar(dto.playerId);
        }
    }

    private function onSurrender(event:Event = null):void {
        view.removeEventListener(GameViewEvents.SURRENDER, onSurrender);
        server.surrender();
    }

    private function onLeaveButtonClick(event:Event):void {
        server.leaveGame();
    }

    // mouse

    private function onMouseDown(event:GameMouseEvent):void {
        view.tutor.visible = false;

        if (magicItems.selected) {
            if (event.mousePos.x > 0 && event.mousePos.y > 0 && event.mousePos.x < width && event.mousePos.y < height) {
                itemMouseDown(event.mousePos);
            }
        } else {
            const building:Building = buildings.selfInXy(selfId, event.mousePos);
            if (building) arrows.startDraw(building.id.id, building.pos);
        }
    }

    private static const tutorBigTowerId:int = 20; // todo hardcode
    private static const tutorArrowsTowerId:int = 16; // todo hardcode

    private function onMouseUp(event:GameMouseEvent):void {
        view.tutor.visible = true;

        if (magicItems.selected) {
            if (tornadoPath.drawing) {
                if (tornadoPath.points.length >= 2 && checkTornadoPoints(tornadoPath.points)) {
                    const gameState:CastTornadoDTO = new CastTornadoDTO();
                    gameState.points = Points.pointsToDto(tornadoPath.points);
                    server.castTornado(gameState);
                    magicItems.useItem();
                    dispatchEvent(new Event(GameTutorEvents.casted(ItemType.TORNADO)));
                }
                tornadoPath.endDraw()
            }
        } else {
            if (arrows.drawing) {
                const toBuilding:Building = buildings.inXy(event.mousePos);

                if (toBuilding) {
                    const fromBuildingsIds:Vector.<BuildingId> = arrows.getFromBuildingsIds();
                    const filteredIds:Vector.<BuildingId> = new <BuildingId>[];
                    for each(var id:BuildingId in fromBuildingsIds) {
                        if (id.id != toBuilding.id.id) {
                            filteredIds.push(id);
                        }
                    }

                    if (filteredIds.length > 0 &&
                            (toBuilding.id.id != tutorBigTowerId || (!tutor || tutor.canCaptureBigTower)) &&
                            (toBuilding.id.id != tutorArrowsTowerId || (!tutor || tutor.canArrows))) {
                        if (!toBuilding.owner.equalsId(selfId)) {
                            if (filteredIds.length > 1)
                                dispatchEvent(new Event(GameTutorEvents.ARROWS_SENDED));
                            dispatchEvent(new Event(GameTutorEvents.ARROW_SENDED));
                        }

                        const dto:MoveDTO = new MoveDTO();
                        dto.toBuilding = toBuilding.id;
                        dto.fromBuildings = filteredIds;
                        server.move(dto);
                    }
                }

                arrows.endDraw();
            }
        }
    }

    /** Нельзя запустить торнадо, не нарисовав траекторию достаточной длины */
    private static function checkTornadoPoints(points:Vector.<Point>):Boolean {
        const distance:int = 48;
        for (var i:int = 1; i < points.length; i++) {
            if (points[i].distance(points[0]) > distance) return true;
        }
        return false;
    }

    private function itemMouseDown(mousePos:Point):void {
        switch (magicItems.selected) {

            case ItemType.FIREBALL:
                server.castFireball(mousePos.dto());
                magicItems.useItem();
                dispatchEvent(new Event(GameTutorEvents.casted(ItemType.FIREBALL)));
                break;

            case ItemType.STRENGTHENING:
                const strBuilding:Building = buildings.selfInXy(selfId, mousePos);
                if (strBuilding) {
                    server.castStrengthening(strBuilding.id);
                    magicItems.useItem();
                    dispatchEvent(new Event(GameTutorEvents.casted(ItemType.STRENGTHENING)));
                }
                break;

            case ItemType.VOLCANO:
                server.castVolcano(mousePos.dto());
                magicItems.useItem();
                dispatchEvent(new Event(GameTutorEvents.casted(ItemType.VOLCANO)));
                break;

            case ItemType.TORNADO:
                tornadoPath.startDraw(mousePos);
                break;

            case ItemType.ASSISTANCE:
                const building:Building = buildings.selfInXy(selfId, mousePos);
                if (building) {
                    server.castAssistance(building.id);
                    magicItems.useItem();
                    dispatchEvent(new Event(GameTutorEvents.casted(ItemType.ASSISTANCE)));
                }
                break;
        }
    }
}
}
