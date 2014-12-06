package ru.rknrl.castles.game {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.geom.ColorTransform;
import flash.geom.Point;
import flash.ui.Keyboard;
import flash.utils.Dictionary;
import flash.utils.getTimer;

import ru.rknrl.Points;
import ru.rknrl.castles.game.layout.GameLayout;
import ru.rknrl.castles.game.ui.GameItem;
import ru.rknrl.castles.game.ui.GameUI;
import ru.rknrl.castles.game.ui.ItemClickEvent;
import ru.rknrl.castles.game.view.Arrow;
import ru.rknrl.castles.game.view.Building;
import ru.rknrl.castles.game.view.GameConstants;
import ru.rknrl.castles.game.view.GameView;
import ru.rknrl.castles.game.view.Unit;
import ru.rknrl.castles.game.view.items.BulletView;
import ru.rknrl.castles.game.view.items.FireballView;
import ru.rknrl.castles.game.view.items.TornadoView;
import ru.rknrl.castles.game.view.items.VolcanoView;
import ru.rknrl.castles.menu.screens.gameOver.GameOverScreen;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingUpdateDTO;
import ru.rknrl.dto.BulletDTO;
import ru.rknrl.dto.CastTorandoDTO;
import ru.rknrl.dto.FireballDTO;
import ru.rknrl.dto.GameOverDTO;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.ItemStateDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.ItemsStateDTO;
import ru.rknrl.dto.MoveDTO;
import ru.rknrl.dto.PointDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.StartLocationOrientation;
import ru.rknrl.dto.StartLocationPosDTO;
import ru.rknrl.dto.TornadoDTO;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitIdDTO;
import ru.rknrl.dto.UnitUpdateDTO;
import ru.rknrl.dto.VolcanoDTO;
import ru.rknrl.jnb.rmi.GameFacadeSender;
import ru.rknrl.jnb.rmi.IGameFacade;

public class Game extends Sprite implements IGameFacade {
    private var sender:GameFacadeSender;

    private var view:GameView;
    private var ui:GameUI;

    private var gameLayout:GameLayout;

    public function Game(sender:GameFacadeSender, gameState:GameStateDTO, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;

        const w:int = layout is LayoutLandscape ? 16 : 8;
        const h:int = layout is LayoutLandscape ? 16 : 12;

        gameLayout = layout.createGameLayout(w, h);

        addChild(view = new GameView(w, h));

        addChild(ui = new GameUI(layout, gameLayout, locale, gameState.itemsState, cooldownDuration));
        ui.addEventListener(ItemClickEvent.ITEM_CLICK, onItemClick);

        updateLayout(layout);

        parseGameState(gameState);

        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
    }

    public function updateLayout(layout:Layout):void {
        gameLayout.update(layout.stageWidth, layout.stageHeight, layout.scale);

        view.scaleX = view.scaleY = layout.scale;

        view.x = gameLayout.gameLeft;
        view.y = gameLayout.gameTop;

        ui.updateLayout(layout, gameLayout);
    }

    public function get mousePos():Point {
        return new Point(view.mouseX, view.mouseY);
    }

    private function onAddedToStage(event:Event):void {
        removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        stage.addEventListener(Event.ENTER_FRAME, onEnterFrame);
        stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        stage.addEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
        stage.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
        stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    public function removeListeners():void {
        stage.removeEventListener(Event.ENTER_FRAME, onEnterFrame);
        stage.removeEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseUp);
        stage.removeEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    private function onKeyDown(event:KeyboardEvent):void {
        if (event.charCode == Keyboard.ESCAPE) {
            sender.surrender();
        }
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        updateUnitsPositions(time);
        updateFireballs(time);
        updateVolcanoes(time);
        updateTornadoes(time);
        updateBullets(time);
    }

    // game state

    private var selfId:int;

    private function parseGameState(gameState:GameStateDTO):void {
        selfId = gameState.selfId.id;

        for each(var starLocation:StartLocationPosDTO in gameState.startLocations) {
            for each(var slotId:SlotId in SlotId.values) {
                const pos:Point = Utils.slotsPositions[slotId];
                const x:Number = starLocation.x + pos.x;
                const y:Number = starLocation.orientation == StartLocationOrientation.TOP ? starLocation.y - pos.y : starLocation.y + pos.y;
                view.updateGroundColor(x, y, new BuildingOwner(true, starLocation.playerId.id));
            }
        }

        for each(var building:BuildingDTO in gameState.buildings) {
            const colorTransform:ColorTransform = building.hasOwner ? Colors.playerColorTransforms[building.owner.id] : Colors.noOwnerColorTransform;
            const owner:BuildingOwner = building.hasOwner ? new BuildingOwner(true, building.owner.id) : new BuildingOwner(false);
            addBuilding(new Building(building.id.id, building.x, building.y, building.building.type, building.building.level, building.population, colorTransform, owner, building.strengthened));
        }

        for each(var unit:UnitDTO in gameState.units) {
            onAddUnit(unit);
        }

        for each(var volcano:VolcanoDTO in gameState.volcanoes) {
            onAddVolcano(volcano);
        }

        for each(var tornado:TornadoDTO in gameState.tornadoes) {
            onAddTornado(tornado);
        }

        for each(var bullet:BulletDTO in gameState.bullets) {
            onAddBullet(bullet);
        }
    }

    // buildings

    private const buildings:Vector.<Building> = new Vector.<Building>;

    private function getBuildingById(id:int):Building {
        for each(var building:Building in buildings) {
            if (building.id == id) return building;
        }
        return null;
    }

    private function getBuildingInXY(pos:Point):Building {
        for each(var building:Building in buildings) {
            const dx:Number = building.x - pos.x;
            const dy:Number = building.y - pos.y;
            const distance:Number = Math.sqrt(dx * dx + dy * dy);
            if (distance < GameConstants.cellSize / 2) return building;
        }
        return null;
    }

    private function getSelfBuildingInXY(pos:Point):Building {
        const building:Building = getBuildingInXY(pos);

        if (building && building.owner.hasOwner && building.owner.ownerId == selfId) {
            return building;
        }
        return null;
    }

    private function addBuilding(building:Building):void {
        if (getBuildingById(building.id)) throw new Error("building already exists " + building.id);
        buildings.push(building);
        view.addBuilding(building);
    }

    public function onUpdateBuilding(update:BuildingUpdateDTO):void {
        const colorTransform:ColorTransform = update.hasOwner ? Colors.playerColorTransforms[update.owner.id] : Colors.noOwnerColorTransform;
        const owner:BuildingOwner = update.hasOwner ? new BuildingOwner(true, update.owner.id) : new BuildingOwner(false);
        const building:Building = getBuildingById(update.id.id);
        building.update(update.population, colorTransform, owner, update.strengthened);

        view.updateGroundColor(building.x, building.y, owner);
    }

    // units

    private const units:Vector.<Unit> = new Vector.<Unit>;

    private function getUnitById(id:int):Unit {
        for each(var unit:Unit in units) {
            if (unit.id == id) return unit;
        }
        return null;
    }

    public function onAddUnit(unitDTO:UnitDTO):void {
        if (getUnitById(unitDTO.id.id)) throw new Error("unit already exists " + unitDTO.id.id);

        const toBuilding:Building = getBuildingById(unitDTO.targetBuildingId.id);
        const endXY:Point = new Point(toBuilding.x, toBuilding.y);
        const endIJ:Point = new Point(endXY.x, endXY.y);

        const unit:Unit = new Unit(unitDTO.id.id, unitDTO.x, unitDTO.y, endIJ.x, endIJ.y, getTimer(), unitDTO.type, unitDTO.count, Colors.playerColorTransforms[unitDTO.owner.id], unitDTO.speed, unitDTO.strengthened);
        const pos:Point = new Point(unit.startX, unit.startY);
        unit.x = pos.x;
        unit.y = pos.y;
        units.push(unit);
        view.addUnit(unit);
    }

    private function updateUnitsPositions(time:int):void {
        for each(var unit:Unit in units) {
            const pos:Point = unit.getPos(time);
            unit.x = pos.x;
            unit.y = pos.y;
            unit.update(time);
        }
    }

    public function onUpdateUnit(unitUpdateDTO:UnitUpdateDTO):void {
//        getUnitById(unitUpdateDTO.id.id).update();
    }

    public function onRemoveUnit(unitIdDTO:UnitIdDTO):void {
        const id:int = unitIdDTO.id;
        const unit:Unit = getUnitById(id);
        if (!unit) throw new Error("can't find unit " + unit.id);
        const index:int = units.indexOf(unit);
        units.splice(index, 1);
        view.removeUnit(unit);
    }

    // fireball

    private const fireballs:Vector.<FireballView> = new <FireballView>[];

    public function onAddFireball(dto:FireballDTO):void {
        const fromLeft:Boolean = dto.x > gameLayout.originalGameWidth / 2;
        const fromTop:Boolean = dto.y > gameLayout.originalGameHeight / 2;
        const dx:Number = fromLeft ? dto.x : gameLayout.originalGameWidth - dto.x;
        const dy:Number = fromTop ? dto.y : gameLayout.originalGameHeight - dto.y;
        const d:Number = Math.max(dx, dy);

        const fireball:FireballView = new FireballView(getTimer(), fromLeft ? -d : d, fromTop ? -d : d);
        fireball.x = dto.x;
        fireball.y = dto.y;
        view.addFireball(fireball);
        fireballs.push(fireball);
    }

    private function updateFireballs(time:int):void {
        const toRemove:Vector.<FireballView> = new <FireballView>[];

        for each(var fireball:FireballView in fireballs) {
            fireball.update(time);
            if (time - fireball.startTime > fireball.duration) {
                toRemove.push(fireball);
            }
        }

        for each(fireball in toRemove) {
            const index:int = fireballs.indexOf(fireball);
            view.removeFireball(fireball);
            fireballs.splice(index, 1)
        }
    }

    // volcano

    private const volcanoes:Vector.<VolcanoView> = new <VolcanoView>[];

    public function onAddVolcano(dto:VolcanoDTO):void {
        const volcano:VolcanoView = new VolcanoView(getTimer(), dto.millisTillEnd);
        volcano.x = dto.x;
        volcano.y = dto.y;
        view.addVolcano(volcano);
        volcanoes.push(volcano);
    }

    private function updateVolcanoes(time:int):void {
        const toRemove:Vector.<VolcanoView> = new <VolcanoView>[];

        for each(var volcano:VolcanoView in volcanoes) {
            volcano.update(time);
            if (time - volcano.startTime > volcano.millisTillEnd) {
                toRemove.push(volcano);
            }
        }

        for each(volcano in toRemove) {
            const index:int = volcanoes.indexOf(volcano);
            view.removeVolcano(volcano);
            volcanoes.splice(index, 1)
        }
    }

    // tornado

    private const tornadoes:Vector.<TornadoView> = new <TornadoView>[];

    public function onAddTornado(dto:TornadoDTO):void {
        const tornado:TornadoView = new TornadoView(getTimer(), dto.millisFromStart, dto.millisTillEnd, new Points(dtoToPoints(dto.points)), GameConstants.tornadoSpeed);
        tornadoes.push(tornado);
        view.addTornado(tornado);
    }

    private function updateTornadoes(time:int):void {
        const toRemove:Vector.<TornadoView> = new <TornadoView>[];

        for each(var tornado:TornadoView in tornadoes) {
            const pos:Point = tornado.getPos(time);
            tornado.update(time);
            tornado.x = pos.x;
            tornado.y = pos.y;

            if (time - tornado.startTime > tornado.millisTillEnd) {
                toRemove.push(tornado);
            }
        }

        for each(tornado in toRemove) {
            const index:int = tornadoes.indexOf(tornado);
            view.removeTornado(tornado);
            tornadoes.splice(index, 1)
        }
    }

    // bullets

    private const bullets:Vector.<BulletView> = new <BulletView>[];

    public function onAddBullet(bulletDTO:BulletDTO):void {
        const unit:Unit = getUnitById(bulletDTO.unitId.id);
        const building:Building = getBuildingById(bulletDTO.buildingId.id);
        const duration:int = bulletDTO.duration;

        const startPos:Point = new Point(building.x, building.y);
        const unitPos:Point = unit.getPos(getTimer() + duration);
        const endPos:Point = new Point(unitPos.x, unitPos.y);

        const bullet:BulletView = new BulletView(startPos, endPos, getTimer(), duration);
        bullets.push(bullet);
        view.addBullet(bullet);
    }

    private function updateBullets(time:int):void {
        const toRemove:Vector.<BulletView> = new <BulletView>[];

        for each(var bullet:BulletView in bullets) {
            const pos:Point = bullet.getPos(time);
            bullet.x = pos.x;
            bullet.y = pos.y;

            if (time - bullet.startTime > bullet.duration) {
                toRemove.push(bullet);
            }
        }

        for each(bullet in toRemove) {
            const index:int = bullets.indexOf(bullet);
            view.removeBullet(bullet);
            bullets.splice(index, 1)
        }
    }

    // item states
    private static const cooldownDuration:int = 5000; // todo

    public function onUpdateItemStates(dto:ItemsStateDTO):void {
        for each(var itemState:ItemStateDTO in dto.items) {
            ui.updateItem(itemState.itemType, itemState.millisTillEnd, getTimer(), cooldownDuration, itemState.count);
        }
    }

    // mouse

    private function onMouseDown(event:MouseEvent):void {
        if (selectedItem) {
            itemMouseDown();
        } else {
            arrowMouseDown();
        }
    }


    private function onMouseMove(event:MouseEvent):void {
        if (selectedItem) {
            tornadoMouseMove();
        } else {
            arrowMouseMove();
        }
    }

    private function onMouseUp(event:MouseEvent):void {
        if (selectedItem) {
            tornadoMouseUp();
        } else {
            arrowMouseUp();
        }
    }

    // arrows

    private var drawArrow:Boolean;

    private const arrows:Vector.<Arrow> = new <Arrow>[];

    private function getArrowByBuildingId(id:int):Arrow {
        for each(var arrow:Arrow in arrows) {
            if (arrow.fromBuildingId == id) return arrow;
        }
        return null;
    }

    private function addArrow(fromBuilding:Building):void {
        if (getArrowByBuildingId(fromBuilding.id)) return;

        const pos:Point = new Point(fromBuilding.x, fromBuilding.y);

        const arrow:Arrow = new Arrow(fromBuilding.id, pos.x, pos.y);
        arrow.orient(pos);
        view.addArrow(arrow);

        arrows.push(arrow);
    }

    private function arrowMouseDown():void {
        const building:Building = getSelfBuildingInXY(mousePos);
        if (building) {
            drawArrow = true;
            addArrow(building);
        }
    }

    private function arrowMouseMove():void {
        if (drawArrow) {
            const building:Building = getSelfBuildingInXY(mousePos);
            if (building) {
                addArrow(building);
            }

            for each(var arrow:Arrow in arrows) {
                arrow.orient(mousePos);
            }
        }
    }

    private function arrowMouseUp():void {
        if (drawArrow) {
            const toBuilding:Building = getBuildingInXY(mousePos);

            if (toBuilding) {
                const filteredArrow:Vector.<Arrow> = new <Arrow>[];
                for each(var arrow:Arrow in arrows) {
                    if (arrow.fromBuildingId != toBuilding.id) {
                        filteredArrow.push(arrow);
                    }
                }

                if (filteredArrow.length > 0) {
                    const dto:MoveDTO = new MoveDTO();
                    dto.toBuilding = buildingIdDto(toBuilding.id);
                    dto.fromBuildings = new <BuildingIdDTO>[];
                    for each(var arrow:Arrow in filteredArrow) {
                        dto.fromBuildings.push(buildingIdDto(arrow.fromBuildingId));
                    }
                    sender.move(dto);
                }
            }

            drawArrow = false;
            for each(var arrow:Arrow in arrows) {
                view.removeArrow(arrow);
            }
            arrows.length = 0;
        }
    }

    // item

    private var _selectedItem:GameItem;

    private function set selectedItem(value:GameItem):void {
        if (_selectedItem) {
            _selectedItem.selected = false;
        }
        _selectedItem = value;
        if (_selectedItem) {
            _selectedItem.selected = true;
        }
    }

    private function get selectedItem():GameItem {
        return _selectedItem;
    }

    private function onItemClick(event:ItemClickEvent):void {
        if (event.gameItem.canUse(getTimer())) {
            if (selectedItem == event.gameItem) {
                selectedItem = null;
            } else {
                selectedItem = event.gameItem;
            }
        }
    }


    private function useItem():void {
        selectedItem.lock();
        selectedItem = null;
    }

    private function itemMouseDown():void {
        switch (selectedItem.itemType) {
            case ItemType.FIREBALL:
                sender.castFireball(pointToDto(mousePos));
                useItem();
                break;
            case ItemType.STRENGTHENING:
                const strBuilding:Building = getSelfBuildingInXY(mousePos);
                if (strBuilding) {
                    sender.castStrengthening(buildingIdDto(strBuilding.id));
                    useItem();
                }
                break;
            case ItemType.VOLCANO:
                sender.castVolcano(pointToDto(mousePos));
                useItem();
                break;
            case ItemType.TORNADO:
                tornadoPoints = new <Point>[mousePos];
                drawingTornado = true;
                break;
            case ItemType.ASSISTANCE:
                const building:Building = getSelfBuildingInXY(mousePos);
                if (building) {
                    sender.castAssistance(buildingIdDto(building.id));
                    useItem();
                }
                break;
        }
    }

    // dto utils

    private static function buildingIdDto(id:int):BuildingIdDTO {
        const dto:BuildingIdDTO = new BuildingIdDTO();
        dto.id = id;
        return dto;
    }

    private static function pointToDto(p:Point):PointDTO {
        const dto:PointDTO = new PointDTO();
        dto.x = p.x;
        dto.y = p.y;
        return dto;
    }

    private static function pointsToDto(points:Vector.<Point>):Vector.<PointDTO> {
        const result:Vector.<PointDTO> = new Vector.<PointDTO>(points.length, true);
        for (var i:int = 0; i < points.length; i++) {
            result[i] = pointToDto(points[i]);
        }
        return result;
    }

    private static function dtoToPoint(dto:PointDTO):Point {
        return new Point(dto.x, dto.y);
    }

    private static function dtoToPoints(dto:Vector.<PointDTO>):Vector.<Point> {
        const result:Vector.<Point> = new Vector.<Point>(dto.length, true);
        for (var i:int = 0; i < dto.length; i++) {
            result[i] = dtoToPoint(dto[i]);
        }
        return result;
    }

    // tornado

    private static const tornadoMaxPoints:int = 10;
    private static const tornadoInterval:int = 100;

    private var drawingTornado:Boolean;
    private var tornadoPoints:Vector.<Point>;

    private var tornadoLastTime:int;

    private function tornadoMouseMove():void {
        if (drawingTornado) {
            const time:int = getTimer();
            if (time - tornadoLastTime > tornadoInterval) {
                tornadoPoints.push(mousePos);
                if (tornadoPoints.length > tornadoMaxPoints) {
                    tornadoPoints.shift();
                }
                tornadoLastTime = time;
                view.drawTornadoPoints(tornadoPoints);
            }
        }
    }

    private function tornadoMouseUp():void {
        if (drawingTornado) {
            const dto:CastTorandoDTO = new CastTorandoDTO();
            dto.points = pointsToDto(tornadoPoints);
            sender.castTornado(dto);
            drawingTornado = false;

            useItem();
            view.clearTornadoPoints();
        }
    }

    // game over screen

    public function onGameOver(gameOverDTO:GameOverDTO):void {
        if (gameOverDTO.playerId.id == selfId) {
            view.alpha = 0.1;
            const gameOverScreen:EventDispatcher = ui.openGameOverScreen(gameOverDTO.win, gameOverDTO.reward);
            gameOverScreen.addEventListener(GameOverScreen.PLAY_AGAIN, onPlayAgain);
            gameOverScreen.addEventListener(GameOverScreen.TO_MENU, onToMenu);
        } else {
            trace("Игрок " + gameOverDTO.playerId.id + " занимает " + gameOverDTO.place + " место");
        }
    }

    private var _wantPlayAgain:Boolean;

    public function get wantPlayAgain():Boolean {
        return _wantPlayAgain;
    }

    private function onPlayAgain(event:Event):void {
        _wantPlayAgain = true;
        sender.leave();
    }

    private function onToMenu(event:Event):void {
        sender.leave();
    }
}
}
