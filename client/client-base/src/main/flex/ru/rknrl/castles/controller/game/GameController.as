package ru.rknrl.castles.controller.game {
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.model.CastlesLocalStorage;
import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.game.Buildings;
import ru.rknrl.castles.model.game.GameMagicItems;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.tutor.GameTutorState;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.rmi.GameFacadeSender;
import ru.rknrl.castles.rmi.IGameFacade;
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
import ru.rknrl.dto.PlayerInfoDTO;
import ru.rknrl.dto.StartLocationPosDTO;
import ru.rknrl.dto.TornadoDTO;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitIdDTO;
import ru.rknrl.dto.UnitUpdateDTO;
import ru.rknrl.dto.VolcanoDTO;

public class GameController implements IGameFacade {
    private var width:Number;
    private var height:Number;
    private var view:GameView;
    private var sender:GameFacadeSender;
    private var selfId:PlayerIdDTO;
    private var tutorState:GameTutorState;
    private var localStorage:CastlesLocalStorage;

    private var bullets:Bullets;
    private var fireballs:Fireballs;
    private var tornadoes:Tornadoes;
    private var volcanoes:Volcanoes;
    private var units:Units;
    private var arrows:Arrows;
    private var tornadoPath:TornadoPath;
    private var magicItems:MagicItems;

    private var players:Vector.<PlayerInfoDTO>;

    public function getPlayerInfo(playerId:PlayerIdDTO):PlayerInfoDTO {
        for each(var playerInfo:PlayerInfoDTO in players) {
            if (playerInfo.id.id == playerId.id) return playerInfo;
        }
        throw new Error("can't find playerInfo " + playerId.id);
    }

    public function getSelfPlayerInfo():PlayerInfoDTO {
        return getPlayerInfo(selfId);
    }

    public function getEnemiesPlayerInfos():Vector.<PlayerInfoDTO> {
        const result:Vector.<PlayerInfoDTO> = new <PlayerInfoDTO>[];
        for each(var playerInfo:PlayerInfoDTO in players) {
            if (playerInfo.id.id != selfId.id) result.push(playerInfo);
        }
        return result;
    }

    public function GameController(view:GameView,
                                   sender:GameFacadeSender,
                                   gameState:GameStateDTO,
                                   localStorage:CastlesLocalStorage) {
        this.view = view;
        this.sender = sender;
        this.localStorage = localStorage;

        tutorState = localStorage.gameTutorState;

        width = gameState.width;
        height = gameState.height;

        selfId = gameState.selfId;
        players = gameState.playerInfos;

        bullets = new Bullets(view.area.bullets);
        fireballs = new Fireballs(view.area.fireballs, view.area.explosions, gameState.width, gameState.height);
        tornadoes = new Tornadoes(view.area.tornadoes);
        volcanoes = new Volcanoes(view.area.volcanoes);
        units = new Units(view.area.units);
        arrows = new Arrows(view.area.arrows);
        tornadoPath = new TornadoPath(view.area.tornadoPath);
        magicItems = new MagicItems(view.magicItems);

        onUpdateItemStates(gameState.itemsState);

        for each(var startLocationPos:StartLocationPosDTO in gameState.startLocations) view.area.addStartLocation(startLocationPos);

        var buildingList:Vector.<Building> = new <Building>[];
        for each(var b:BuildingDTO in gameState.buildings) {
            const owner:BuildingOwner = new BuildingOwner(b.hasOwner, b.owner);
            const pos:Point = new Point(b.x, b.y);
            buildingList.push(new Building(b.id, pos, owner, b.strengthened));
            view.area.addBuilding(b.id, b.building.type, b.building.level, owner, b.population, b.strengthened, pos);
        }
        buildings = new Buildings(buildingList);

        for each(var unit:UnitDTO in gameState.units) onAddUnit(unit);
        for each(var fireball:FireballDTO in gameState.fireballs) onAddFireball(fireball);
        for each(var tornado:TornadoDTO in gameState.tornadoes) onAddTornado(tornado);
        for each(var volcano:VolcanoDTO in gameState.volcanoes) onAddVolcano(volcano);
        for each(var bullet:BulletDTO in gameState.bullets) onAddBullet(bullet);
        for each(var gameOver:GameOverDTO in gameState.gameOvers) onGameOver(gameOver);

        view.addEventListener(GameViewEvents.SURRENDER, onSurrender);
        view.addEventListener(GameViewEvents.LEAVE_BUTTON_CLICK, onLeaveButtonClick);
        view.addEventListener(MagicItemClickEvent.MAGIC_ITEM_CLICK, onMagicItemClick);

        view.addEventListener(GameMouseEvent.ENTER_FRAME, onEnterFrame);
        view.addEventListener(GameMouseEvent.MOUSE_DOWN, onMouseDown);
        view.addEventListener(GameMouseEvent.MOUSE_UP, onMouseUp);

        // Человек мог играть на компе, а потом перезайти в бой на мобиле
        if (gameState.players.length > view.supportedPlayersCount) onSurrender()
    }

    private static const tutorInterval:int = 10000;
    private var tutorLastTime:int = -8000;

    private function onEnterFrame(event:GameMouseEvent):void {
        const time:int = getTimer();
        update(time);

        if (magicItems.selected) {
            tornadoPath.mouseMove(event.mousePos);
        } else {
            if (arrows.drawing) {
                const building:Building = buildings.selfInXy(selfId, event.mousePos);
                if (building) arrows.addArrow(building);
                arrows.mouseMove(event.mousePos);
            }
        }

        if (time - tutorLastTime > tutorInterval && !view.tutor.playing && !arrows.drawing && !tornadoPath.drawing && !magicItems.selected) {
            tutorLastTime = time;
            if (!tutorState.arrow)
                playArrowTutor();
            else if (!tutorState.arrows && buildings.getSelfBuildingsPos(selfId))
                playArrowsTutor();
            else if (!tutorState.fireball)
                playFireballTutor();
            else if (!tutorState.tornado)
                playTornadoTutor();
            else if (!tutorState.strengthened)
                playStrengthenedTutor();
            else if (!tutorState.volcano)
                playVolcanoTutor();
            else if (!tutorState.assistance)
                playAssistanceTutor();
        }
    }

    private function update(time:int):void {
        units.update(time);
        fireballs.update(time);
        volcanoes.update(time);
        tornadoes.update(time);
        bullets.update(time);

        for each(var itemType:ItemType in ItemType.values) {
            view.magicItems.setItemCooldown(itemType, magicItemStates.cooldownProgress(itemType, time))
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
        buildings.byId(dto.id).update(owner, dto.strengthened);

        view.area.setBuildingCount(dto.id, dto.population);
        view.area.setBuildingOwner(dto.id, owner);
        view.area.setBuildingStrengthened(dto.id, dto.strengthened);
    }

    // magic items

    private var magicItemStates:GameMagicItems;

    public function onUpdateItemStates(dto:ItemsStateDTO):void {
        magicItemStates = new GameMagicItems(dto);

        for each(var itemType:ItemType in ItemType.values) {
            view.magicItems.setItemCount(itemType, magicItemStates.count(itemType))
        }
        view.magicItems.lock = false;
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
            const winners:Vector.<PlayerInfoDTO> = dto.place == 1 ? new <PlayerInfoDTO>[getSelfPlayerInfo()] : getEnemiesPlayerInfos();
            const losers:Vector.<PlayerInfoDTO> = dto.place == 1 ? getEnemiesPlayerInfos() : new <PlayerInfoDTO>[getSelfPlayerInfo()];
            view.openGameOverScreen(PlayerInfo.fromDtoVector(winners), PlayerInfo.fromDtoVector(losers), dto.place == 1, dto.reward);
        }
    }

    private function onSurrender(event:Event = null):void {
        view.removeEventListener(GameViewEvents.SURRENDER, onSurrender);
        sender.surrender();
    }

    private function onLeaveButtonClick(event:Event):void {
        sender.leave();
    }

    // mouse

    private function onMouseDown(event:GameMouseEvent):void {
        if (magicItems.selected) {
            if (event.mousePos.x > 0 && event.mousePos.y > 0 && event.mousePos.x < width && event.mousePos.y < height) {
                itemMouseDown(event.mousePos);
            }
        } else {
            const building:Building = buildings.selfInXy(selfId, event.mousePos);
            if (building) arrows.startDraw(building);
        }
    }

    private function onMouseUp(event:GameMouseEvent):void {
        if (magicItems.selected) {
            if (tornadoPath.drawing) {
                if (tornadoPath.points.length >= 2) {
                    if (!tutorState.tornado) {
                        tutorState.tornado = true;
                        localStorage.saveGameTutorState(tutorState);
                        tutorLastTime = getTimer();
                    }

                    const gameState:CastTorandoDTO = new CastTorandoDTO();
                    gameState.points = Point.pointsToDto(tornadoPath.points);
                    sender.castTornado(gameState);
                    magicItems.useItem();
                }
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
                        if (filteredIds.length > 1 && tutorState.arrow) {
                            if (!tutorState.arrows) {
                                tutorState.arrows = true;
                                localStorage.saveGameTutorState(tutorState);
                                tutorLastTime = getTimer();
                            }
                        } else {
                            if (!tutorState.arrow) {
                                tutorState.arrow = true;
                                localStorage.saveGameTutorState(tutorState);
                                tutorLastTime = getTimer();
                            }
                        }

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
                if (!tutorState.fireball) {
                    tutorState.fireball = true;
                    localStorage.saveGameTutorState(tutorState);
                    tutorLastTime = getTimer();
                }

                sender.castFireball(mousePos.dto());
                magicItems.useItem();
                break;
            case ItemType.STRENGTHENING:
                const strBuilding:Building = buildings.selfInXy(selfId, mousePos);
                if (strBuilding) {
                    if (!tutorState.strengthened) {
                        tutorState.strengthened = true;
                        localStorage.saveGameTutorState(tutorState);
                        tutorLastTime = getTimer();
                    }

                    sender.castStrengthening(strBuilding.id);
                    magicItems.useItem();
                }
                break;
            case ItemType.VOLCANO:
                if (!tutorState.volcano) {
                    tutorState.volcano = true;
                    localStorage.saveGameTutorState(tutorState);
                    tutorLastTime = getTimer();
                }

                sender.castVolcano(mousePos.dto());
                magicItems.useItem();
                break;
            case ItemType.TORNADO:
                tornadoPath.startDraw(mousePos);
                break;
            case ItemType.ASSISTANCE:
                const building:Building = buildings.selfInXy(selfId, mousePos);
                if (building) {
                    if (!tutorState.assistance) {
                        tutorState.assistance = true;
                        localStorage.saveGameTutorState(tutorState);
                        tutorLastTime = getTimer();
                    }

                    sender.castAssistance(building.id);
                    magicItems.useItem();
                }
                break;
        }
    }

    // tutor

    private function playFireballTutor():void {
        const buildingPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playFireball(buildingPos);
    }

    private function playVolcanoTutor():void {
        const buildingPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playVolcano(buildingPos);
    }

    private function playTornadoTutor():void {
        const buildingPos:Point = buildings.getEnemyBuildingPos(selfId);

        const points:Vector.<Point> = new <Point>[];
        const deltaX:int = 200;
        const deltaY:int = 50;
        for (var x:int = 0; x < deltaX; x++) {
            points.push(new Point(buildingPos.x - x, buildingPos.y + Math.sin(x * 2 * Math.PI / deltaX) * deltaY))
        }
        view.tutor.playTornado(points);
    }

    private function playAssistanceTutor():void {
        const buildingPos:Point = buildings.getSelfBuildingPos(selfId);
        view.tutor.playAssistance(buildingPos);
    }

    private function playStrengthenedTutor():void {
        const buildingPos:Point = buildings.getUnstrengthenedSelfBuildingPos(selfId);
        if (buildingPos) {
            view.tutor.playStrengthening(buildingPos);
        }
    }

    private function playArrowTutor():void {
        const startPos:Point = buildings.getSelfBuildingPos(selfId);
        const endPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playArrow(startPos, endPos);
    }

    private function playArrowsTutor():void {
        const startPos:Vector.<Point> = buildings.getSelfBuildingsPos(selfId);
        const endPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playArrows(startPos[0], startPos[1], endPos);
    }
}
}
