//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.events.Event;
import flash.utils.getTimer;
import flash.utils.setTimeout;

import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.game.Buildings;
import ru.rknrl.castles.model.game.GameMagicItems;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.utils.tutor.TutorialView;
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
import ru.rknrl.dto.PlayerDTO;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.SlotsPosDTO;
import ru.rknrl.dto.TornadoDTO;
import ru.rknrl.dto.TutorStateDTO;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitIdDTO;
import ru.rknrl.dto.UnitUpdateDTO;
import ru.rknrl.dto.VolcanoDTO;
import ru.rknrl.rmi.AddBulletEvent;
import ru.rknrl.rmi.AddFireballEvent;
import ru.rknrl.rmi.AddTornadoEvent;
import ru.rknrl.rmi.AddUnitEvent;
import ru.rknrl.rmi.AddVolcanoEvent;
import ru.rknrl.rmi.GameOverEvent;
import ru.rknrl.rmi.RemoveUnitEvent;
import ru.rknrl.rmi.Server;
import ru.rknrl.rmi.UpdateBuildingEvent;
import ru.rknrl.rmi.UpdateItemStatesEvent;
import ru.rknrl.rmi.UpdateUnitEvent;

public class GameController {
    private var width:Number;
    private var height:Number;
    private var view:GameView;
    private var server:Server;
    private var selfId:PlayerIdDTO;
    private var tutorState:TutorStateDTO;

    private var bullets:Bullets;
    private var fireballs:Fireballs;
    private var tornadoes:Tornadoes;
    private var volcanoes:Volcanoes;
    private var units:Units;
    private var arrows:Arrows;
    private var tornadoPath:TornadoPath;
    private var magicItems:MagicItems;

    private var players:Vector.<PlayerDTO>;

    public function getPlayerInfo(playerId:PlayerIdDTO):PlayerDTO {
        for each(var playerInfo:PlayerDTO in players) {
            if (playerInfo.id.id == playerId.id) return playerInfo;
        }
        throw new Error("can't find playerInfo " + playerId.id);
    }

    public function getSelfPlayerInfo():PlayerDTO {
        return getPlayerInfo(selfId);
    }

    public function getEnemiesPlayerInfos():Vector.<PlayerDTO> {
        const result:Vector.<PlayerDTO> = new <PlayerDTO>[];
        for each(var playerInfo:PlayerDTO in players) {
            if (playerInfo.id.id != selfId.id) result.push(playerInfo);
        }
        return result;
    }

    public function GameController(view:GameView,
                                   server:Server,
                                   gameState:GameStateDTO,
                                   tutorState:TutorStateDTO) {
        this.view = view;
        this.server = server;

        this.tutorState = tutorState;

        width = gameState.width;
        height = gameState.height;

        selfId = gameState.selfId;
        players = gameState.players;

        bullets = new Bullets(view.area.bullets);
        fireballs = new Fireballs(view.area.fireballs, view.area.explosions, gameState.width, gameState.height);
        tornadoes = new Tornadoes(view.area.tornadoes);
        volcanoes = new Volcanoes(view.area.volcanoes);
        units = new Units(view.area.units, view.area.blood);
        arrows = new Arrows(view.area.arrows);
        tornadoPath = new TornadoPath(view.area.tornadoPath);
        magicItems = new MagicItems(view.magicItems);

        updateItemStates(gameState.itemsState);

        for each(var slotsPos:SlotsPosDTO in gameState.slots) view.area.addSlots(slotsPos);

        const buildingList:Vector.<Building> = new <Building>[];
        for each(var b:BuildingDTO in gameState.buildings) {
            buildingList.push(Building.fromDto(b));

            const owner:BuildingOwner = new BuildingOwner(b.hasOwner, b.owner);
            const pos:Point = Point.fromDto(b.pos);
            view.area.addBuilding(b.id, b.building.type, b.building.level, owner, b.population, b.strengthened, pos);
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

        server.addEventListener(UpdateBuildingEvent.UPDATEBUILDING, onUpdateBuilding);
        server.addEventListener(UpdateItemStatesEvent.UPDATEITEMSTATES, onUpdateItemStates);
        server.addEventListener(AddUnitEvent.ADDUNIT, onAddUnit);
        server.addEventListener(UpdateUnitEvent.UPDATEUNIT, onUpdateUnit);
        server.addEventListener(RemoveUnitEvent.REMOVEUNIT, onRemoveUnit);
        server.addEventListener(AddFireballEvent.ADDFIREBALL, onAddFireball);
        server.addEventListener(AddVolcanoEvent.ADDVOLCANO, onAddVolcano);
        server.addEventListener(AddTornadoEvent.ADDTORNADO, onAddTornado);
        server.addEventListener(AddBulletEvent.ADDBULLET, onAddBullet);
        server.addEventListener(GameOverEvent.GAMEOVER, onGameOver);

        // Человек мог играть на компе, а потом перезайти в бой на мобиле
        if (gameState.players.length > view.supportedPlayersCount) onSurrender()
    }

    public function destroy():void {
        server.removeEventListener(UpdateBuildingEvent.UPDATEBUILDING, onUpdateBuilding);
        server.removeEventListener(UpdateItemStatesEvent.UPDATEITEMSTATES, onUpdateItemStates);
        server.removeEventListener(AddUnitEvent.ADDUNIT, onAddUnit);
        server.removeEventListener(UpdateUnitEvent.UPDATEUNIT, onUpdateUnit);
        server.removeEventListener(RemoveUnitEvent.REMOVEUNIT, onRemoveUnit);
        server.removeEventListener(AddFireballEvent.ADDFIREBALL, onAddFireball);
        server.removeEventListener(AddVolcanoEvent.ADDVOLCANO, onAddVolcano);
        server.removeEventListener(AddTornadoEvent.ADDTORNADO, onAddTornado);
        server.removeEventListener(AddBulletEvent.ADDBULLET, onAddBullet);
        server.removeEventListener(GameOverEvent.GAMEOVER, onGameOver);
    }

    private static const tutorDelay:int = 30000;
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

        if (!isGameOver && !view.tutor.playing && !arrows.drawing && !tornadoPath.drawing && !magicItems.selected) {
            if (!tutorState.selfBuildings)
                playSelfBuildingsTutor();
            else if (time - winTutorTime > tutorDelay &&
                    time - tutorLastTime > tutorInterval) {
                tutorLastTime = time;
                if (!tutorState.fireball)
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

    private function onAddUnit(e:AddUnitEvent):void {
        addUnit(e.unit);
    }

    private function addUnit(dto:UnitDTO):void {
        const endPos:Point = buildings.byId(dto.targetBuildingId).pos;
        units.add(endPos, dto);
    }

    private function onUpdateUnit(e:UpdateUnitEvent):void {
        updateUnit(e.unitUpdate);
    }

    private function updateUnit(dto:UnitUpdateDTO):void {
        units.updateUnit(dto);
    }

    private function onRemoveUnit(e:RemoveUnitEvent):void {
        removeUnit(e.id);
    }

    private function removeUnit(id:UnitIdDTO):void {
        units.remove(id);
    }

    private function onAddFireball(e:AddFireballEvent):void {
        addFireball(e.fireball);
    }

    private function addFireball(dto:FireballDTO):void {
        fireballs.add(dto);
    }

    private function onAddVolcano(e:AddVolcanoEvent):void {
        addVolcano(e.volcano);
    }

    private function addVolcano(dto:VolcanoDTO):void {
        volcanoes.add(dto);
    }

    private function onAddTornado(e:AddTornadoEvent):void {
        addTornado(e.tornado);
    }

    private function addTornado(dto:TornadoDTO):void {
        tornadoes.add(dto);
    }

    private function onAddBullet(e:AddBulletEvent):void {
        addBullet(e.bullet);
    }

    private function addBullet(dto:BulletDTO):void {
        const time:int = getTimer();
        const startPos:Point = buildings.byId(dto.buildingId).pos;
        const endPos:Point = units.getUnit(dto.unitId).pos(time + dto.duration);
        bullets.add(time, startPos, endPos, dto.duration);
    }

    private var buildings:Buildings;

    private function onUpdateBuilding(e:UpdateBuildingEvent):void {
        updateBuilding(e.building);
    }

    private function updateBuilding(dto:BuildingUpdateDTO):void {
        const owner:BuildingOwner = new BuildingOwner(dto.hasOwner, dto.owner);
        buildings.byId(dto.id).update(owner, dto.population, dto.strengthened);

        view.area.setBuildingCount(dto.id, dto.population);
        view.area.setBuildingOwner(dto.id, owner);
        view.area.setBuildingStrengthened(dto.id, dto.strengthened);
    }

    // magic items

    private var magicItemStates:GameMagicItems;

    private function onUpdateItemStates(e:UpdateItemStatesEvent):void {
        updateItemStates(e.states);
    }

    private function updateItemStates(dto:ItemsStateDTO):void {
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

    private function onGameOver(e:GameOverEvent):void {
        gameOver(e.gameOver);
    }

    private var isGameOver:Boolean;

    private function gameOver(dto:GameOverDTO):void {
        if (dto.playerId.id == selfId.id) {
            view.removeEventListener(GameViewEvents.SURRENDER, onSurrender);
            isGameOver = true;
            const winners:Vector.<PlayerDTO> = dto.place == 1 ? new <PlayerDTO>[getSelfPlayerInfo()] : getEnemiesPlayerInfos();
            const losers:Vector.<PlayerDTO> = dto.place == 1 ? getEnemiesPlayerInfos() : new <PlayerDTO>[getSelfPlayerInfo()];
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
                        sendTutorState();
                    }

                    const gameState:CastTorandoDTO = new CastTorandoDTO();
                    gameState.points = Point.pointsToDto(tornadoPath.points);
                    server.castTornado(gameState);
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
                                onArrowsTutorComplete();
                                sendTutorState();
                            }
                        } else {
                            if (!tutorState.arrow) {
                                tutorState.arrow = true;
                                onArrowTutorComplete();
                                sendTutorState();
                            }
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

    private function itemMouseDown(mousePos:Point):void {
        switch (magicItems.selected) {
            case ItemType.FIREBALL:
                if (!tutorState.fireball) {
                    tutorState.fireball = true;
                    sendTutorState();
                }

                server.castFireball(mousePos.dto());
                magicItems.useItem();
                break;
            case ItemType.STRENGTHENING:
                const strBuilding:Building = buildings.selfInXy(selfId, mousePos);
                if (strBuilding) {
                    if (!tutorState.strengthened) {
                        tutorState.strengthened = true;
                        sendTutorState();
                    }

                    server.castStrengthening(strBuilding.id);
                    magicItems.useItem();
                }
                break;
            case ItemType.VOLCANO:
                if (!tutorState.volcano) {
                    tutorState.volcano = true;
                    sendTutorState();
                }

                server.castVolcano(mousePos.dto());
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
                        sendTutorState();
                    }

                    server.castAssistance(building.id);
                    magicItems.useItem();
                }
                break;
        }
    }

    //----------------------------------------------
    //
    // TUTOR
    //
    //----------------------------------------------

    private function sendTutorState():void {
        server.updateTutorState(tutorState);
        tutorLastTime = getTimer();
    }

    // Твои домики желтого цвета

    private function playSelfBuildingsTutor():void {
        view.tutor.playSelfBuildings(buildings.getSelfBuildings(selfId), PlayerInfo.fromDto(getSelfPlayerInfo()));
        view.tutor.addEventListener(TutorialView.TUTOR_COMPLETE, onSelfBuildingsComplete);
    }

    private function onSelfBuildingsComplete(e:Event):void {
        view.tutor.removeEventListener(TutorialView.TUTOR_COMPLETE, onSelfBuildingsComplete);

        tutorState.selfBuildings = true;
        sendTutorState();

        if (!tutorState.enemyBuildings) playEnemyBuildingsTutor();
    }

    // У тебя 3 противника

    private function playEnemyBuildingsTutor():void {
        const playerInfos:Vector.<PlayerDTO> = getEnemiesPlayerInfos();
        const ids:Vector.<PlayerIdDTO> = new <PlayerIdDTO>[];
        for each(var playerInfo:PlayerDTO in playerInfos) ids.push(playerInfo.id);
        view.tutor.playEnemyBuildings(buildings.getEnemyBuildings(ids), PlayerInfo.fromDtoVector(playerInfos));
        view.tutor.addEventListener(TutorialView.TUTOR_COMPLETE, onEnemyBuildingsComplete);
    }

    private function onEnemyBuildingsComplete(e:Event):void {
        view.tutor.removeEventListener(TutorialView.TUTOR_COMPLETE, onEnemyBuildingsComplete);

        tutorState.enemyBuildings = true;
        sendTutorState();

        playArrowTutor();
    }

    // Отправляй отряды и захватывай чужие домики

    private function playArrowTutor():void {
        const startPos:Point = buildings.getSelfBuildingPos(selfId);
        const endPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playArrow(startPos, endPos);
    }

    private function onArrowTutorComplete():void {
        setTimeout(playArrowsTutor, 3000); // todo повесить на захват домика; повторять через интервал
    }

    // Можно отправлять отряды сразу из нескольких домиков

    private function playArrowsTutor():void {
        const startPos:Vector.<Point> = buildings.getSelfBuildingsPos(selfId);
        const endPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playArrows(startPos[0], startPos[1], endPos);
    }

    private function onArrowsTutorComplete():void {
        playWinTutor();
    }

    // Захвати все домики противников, чтобы выиграть

    private var winTutorTime:int = int.MAX_VALUE;

    private function playWinTutor():void {
        view.tutor.playWin();
        view.tutor.addEventListener(TutorialView.TUTOR_COMPLETE, onWinTutorComplete);
    }

    private function onWinTutorComplete(e:Event):void {
        view.tutor.removeEventListener(TutorialView.TUTOR_COMPLETE, onWinTutorComplete);
        winTutorTime = getTimer();
        server.startTutorGame();
    }

    // Запусти фаербол в противника

    private function playFireballTutor():void {
        const buildingPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playFireball(buildingPos);
    }

    // Создай вулкан под башней противника

    private function playVolcanoTutor():void {
        const buildingPos:Point = buildings.getEnemyBuildingPos(selfId);
        view.tutor.playVolcano(buildingPos);
    }

    // Используй торнадо против противника

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

    // Усилить свой домик

    private function playStrengthenedTutor():void {
        const buildingPos:Point = buildings.getUnstrengthenedSelfBuildingPos(selfId);
        if (buildingPos) {
            view.tutor.playStrengthening(buildingPos);
        }
    }

    // Вызывай подмогу

    private function playAssistanceTutor():void {
        const buildingPos:Point = buildings.getSelfBuildingPos(selfId);
        view.tutor.playAssistance(buildingPos);
    }
}
}
