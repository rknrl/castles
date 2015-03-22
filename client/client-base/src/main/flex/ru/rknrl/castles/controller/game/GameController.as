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
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.game.Buildings;
import ru.rknrl.castles.model.game.GameMagicItems;
import ru.rknrl.castles.model.game.GameTutorEvents;
import ru.rknrl.castles.model.game.Players;
import ru.rknrl.castles.model.game.Tornado;
import ru.rknrl.castles.model.game.Volcano;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.utils.tutor.commands.Exec;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
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
import ru.rknrl.rmi.KillUnitEvent;
import ru.rknrl.rmi.RemoveUnitEvent;
import ru.rknrl.rmi.Server;
import ru.rknrl.rmi.UpdateBuildingEvent;
import ru.rknrl.rmi.UpdateItemStatesEvent;
import ru.rknrl.rmi.UpdateUnitEvent;

public class GameController extends EventDispatcher {
    private var width:Number;
    private var height:Number;
    private var view:GameView;
    private var server:Server;
    private var selfId:PlayerIdDTO;

    private var bullets:Bullets;
    private var fireballs:Fireballs;
    private var tornadoes:Tornadoes;
    private var volcanoes:Volcanoes;
    private var units:Units;
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
        server.addEventListener(KillUnitEvent.KILLUNIT, onKillUnit);
        server.addEventListener(AddFireballEvent.ADDFIREBALL, onAddFireball);
        server.addEventListener(AddVolcanoEvent.ADDVOLCANO, onAddVolcano);
        server.addEventListener(AddTornadoEvent.ADDTORNADO, onAddTornado);
        server.addEventListener(AddBulletEvent.ADDBULLET, onAddBullet);
        server.addEventListener(GameOverEvent.GAMEOVER, onGameOver);

        // Человек мог играть на компе, а потом перезайти в бой на мобиле
        if (gameState.players.length > view.supportedPlayersCount) onSurrender();

        if (isFirstGame) {
            tutor = new GameTutorController(view, this, players, buildings);
            view.y += 32; // todo
        }
    }

    public function destroy():void {
        Log.info("game destroy");

        server.removeEventListener(UpdateBuildingEvent.UPDATEBUILDING, onUpdateBuilding);
        server.removeEventListener(UpdateItemStatesEvent.UPDATEITEMSTATES, onUpdateItemStates);
        server.removeEventListener(AddUnitEvent.ADDUNIT, onAddUnit);
        server.removeEventListener(UpdateUnitEvent.UPDATEUNIT, onUpdateUnit);
        server.removeEventListener(RemoveUnitEvent.REMOVEUNIT, onRemoveUnit);
        server.removeEventListener(KillUnitEvent.KILLUNIT, onKillUnit);
        server.removeEventListener(AddFireballEvent.ADDFIREBALL, onAddFireball);
        server.removeEventListener(AddVolcanoEvent.ADDVOLCANO, onAddVolcano);
        server.removeEventListener(AddTornadoEvent.ADDTORNADO, onAddTornado);
        server.removeEventListener(AddBulletEvent.ADDBULLET, onAddBullet);
        server.removeEventListener(GameOverEvent.GAMEOVER, onGameOver);
    }

    private var tutorStart:Boolean;

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

        if (!tutorStart) {
            tutorStart = true;
            view.tutor.play(new <ITutorCommand>[
                tutor.firstGame(),
                new Exec(server.startTutorGame)
            ])
        }
    }

    private function update(time:int):void {
        units.update(time);
        fireballs.update(time);
        volcanoes.update(time);
        tornadoes.update(time);
        bullets.update(time);

        updateDustByVolcanoes();
        addKillsByTornadoes(time);

        for each(var itemType:ItemType in ItemType.values) {
            view.magicItems.setItemCooldown(itemType, magicItemStates.cooldownProgress(itemType, time))
        }
    }

    private function updateDustByVolcanoes():void {
        for each(b in buildings.buildings) {
            view.area.setBuildingDust(b.id, false);
        }

        const volcanoDamageRadius:int = 48;
        for each(var volcano:Volcano in volcanoes.volcanoes) {
            const inRadius:Vector.<Building> = buildings.inRadius(volcano.pos, volcanoDamageRadius);
            for each(var b:Building in inRadius) {
                view.area.setBuildingDust(b.id, true);
            }
        }
    }

    private var tornadoKillsLastTime:int;

    private function addKillsByTornadoes(time:int):void {
        if (time - tornadoKillsLastTime > 100) {
            tornadoKillsLastTime = time;

            const tornadoDamageRadius:int = 48;
            const damagedBuildings:Vector.<Building> = new <Building>[];
            for each(var tornado:Tornado in tornadoes.tornadoes) {
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

    private function onKillUnit(e:KillUnitEvent):void {
        units.addBlood(e.killedId);
        removeUnit(e.killedId);
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
        const building:Building = buildings.byId(dto.id);
        const newOwner:BuildingOwner = new BuildingOwner(dto.hasOwner, dto.owner);
        const wasOwned:Boolean = building.owner.equalsId(selfId);
        const willOwned:Boolean = newOwner.equalsId(selfId);

        const capture:Boolean = !wasOwned && (building.population > dto.population || willOwned);
        if (capture) dispatchEvent(new Event(GameTutorEvents.BUILDING_CAPTURED));

        building.update(newOwner, dto.population, dto.strengthened);

        view.area.setBuildingCount(dto.id, dto.population);
        view.area.setBuildingOwner(dto.id, newOwner);
        view.area.setBuildingStrengthened(dto.id, dto.strengthened);

        // Если ты вел стрелку из домика, а его захватили - убираем стрелку
        if (arrows.hasArrow(building.id) && (wasOwned && !willOwned)) {
            arrows.removeArrow(building.id)
        }
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
        const itemType:ItemType = event.itemType;
        if (magicItemStates.canUse(itemType, getTimer())) {
            if (magicItems.selected == itemType) {
                // В туторе нельзя задиселектить выбранный предмет
                if (!view.tutor.playing) magicItems.selected = null;
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
            view.removeEventListener(GameViewEvents.SURRENDER, onSurrender);
            const winners:Vector.<PlayerDTO> = dto.place == 1 ? new <PlayerDTO>[players.getSelfPlayer()] : players.getEnemiesPlayers(selfId);
            const losers:Vector.<PlayerDTO> = dto.place == 1 ? players.getEnemiesPlayers(selfId) : new <PlayerDTO>[players.getSelfPlayer()];
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
                if (tornadoPath.points.length >= 2 && checkTornadoPoints(tornadoPath.points)) {
                    const gameState:CastTorandoDTO = new CastTorandoDTO();
                    gameState.points = Point.pointsToDto(tornadoPath.points);
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
                    const fromBuildingsIds:Vector.<BuildingIdDTO> = arrows.getFromBuildingsIds();
                    const filteredIds:Vector.<BuildingIdDTO> = new <BuildingIdDTO>[];
                    for each(var id:BuildingIdDTO in fromBuildingsIds) {
                        if (id.id != toBuilding.id.id) {
                            filteredIds.push(id);
                        }
                    }

                    if (filteredIds.length > 0) {
                        if (!toBuilding.owner.equalsId(selfId)) {
                            if (filteredIds.length > 1)
                                dispatchEvent(new Event(GameTutorEvents.ARROWS_SENDED));
                            else
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
