//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {

import flash.events.EventDispatcher;

import ru.rknrl.castles.model.game.Buildings;
import ru.rknrl.castles.model.game.GameTutorEvents;
import ru.rknrl.castles.model.game.Players;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.utils.tutor.commands.Exec;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.WaitForClick;
import ru.rknrl.castles.view.utils.tutor.commands.WaitForEvent;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.PlayerDTO;
import ru.rknrl.rmi.Server;

public class GameTutorController extends TutorControllerBase {
    private var view:GameView;
    private var dispatcher:EventDispatcher;
    private var players:Players;
    private var buildings:Buildings;
    private var server:Server;

    public function GameTutorController(view:GameView, dispatcher:EventDispatcher, players:Players, buildings:Buildings, server:Server) {
        this.view = view;
        this.dispatcher = dispatcher;
        this.players = players;
        this.buildings = buildings;
        this.server = server;
        super(view.tutor);
    }

    public function firstGame():ITutorCommand {
        return sequence(new <ITutorCommand>[
            disableMouse,
            lockAllItems,
            hideMagicItems,
            hideCursor,
            wait(1000),

            // self buildings

            addText(view.tutor.locale.tutorSelfBuildings),
            highlightSelfBuildings,
            wait(1500),
            addNextButton,
            waitForClick,
            unhighlightBuildings,
            clear,

            // enemies buildings

            addText(view.tutor.locale.tutorEnemyBuildings(players.isBigGame)),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    highlightNextEnemyBuildings,
                    wait(1000)
                ]),
                sequence(new <ITutorCommand>[
                    wait(2500),
                    addNextButton,
                    waitForClick
                ])
            ]),
            unhighlightBuildings,
            clear,

            // arrow

            addText(view.tutor.locale.tutorArrow),
            wait(500),
            enableMouse,
            arrowTutor(sourceBuilding1, targetBuilding1),

            // arrows

            addText(view.tutor.locale.tutorArrows),
            wait(500),
            showCursor,
            enableArrows,
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tweenFromCorner(sourceBuilding2_1),
                    mouseDown,
                    wait(400),
                    addArrow(sourceBuilding2_1),
                    tweenGame(sourceBuilding2_1, sourceBuilding2_2),
                    wait(400),
                    addArrow(sourceBuilding2_2),
                    tweenGame(sourceBuilding2_2, targetBuilding2),
                    wait(400),
                    mouseUp,
                    removeArrows,
                    wait(400)
                ]),
                waitForEvent(GameTutorEvents.ARROWS_SENDED)
            ]),
            hideCursor,
            clear,
            waitForEvent(GameTutorEvents.BUILDING_CAPTURED),

            // fireball
            showMagicItems,

            exec(server.startTutorFireball),

            wait(4000),
            itemClick(ItemType.FIREBALL),
            cast(ItemType.FIREBALL, fireballBuilding),
            wait(5000),

            // volcano

            itemClick(ItemType.VOLCANO),
            cast(ItemType.VOLCANO, volcanoBuilding),
            wait(5000),

            // tornado

            itemClick(ItemType.TORNADO),
            playTornado(),
            wait(8000),

            // assistance

            itemClick(ItemType.ASSISTANCE),
            castAssistance,
            wait(3000),

            // strengthening

            itemClick(ItemType.STRENGTHENING),
            castStrengthening,
            wait(500),

            magicItemsEnableMouse,

            // capture big tower

            // Захвати большую башню усиленными отрядами
            enableCaptureBigTower,
            addText(view.tutor.locale.tutorBigTower),
            wait(500),
            captureBigTowerTutor,
            hideCursor,
            wait(1000),

            // win

            addText(view.tutor.locale.tutorWin),
            wait(3000),
            exec(server.startTutorGame)
        ]);
    }

    private function _highlightSelfBuildings():void {
        view.tutorBlur(Players.playersToIds(players.getEnemiesPlayers(players.selfId)), buildings.notPlayerId(players.selfId));
    }

    private var enemyIndex:int;

    private function _highlightNextEnemyBuildings():void {
        const player:PlayerDTO = players.getEnemiesPlayers(players.selfId)[enemyIndex];
        view.tutorUnblur();
        view.tutorBlur(Players.playersToIds(players.getEnemiesPlayers(player.id)), buildings.notPlayerId(player.id));
        enemyIndex++;
        if (enemyIndex == 3) enemyIndex = 0;
    }

    public function arrowTutor(from:Point, to:Point):ITutorCommand {
        return sequence(new <ITutorCommand>[
            showCursor,
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tweenFromCorner(from),
                    mouseDown,
                    wait(400),
                    addArrow(from),
                    tweenGame(from, to),
                    wait(400),
                    mouseUp,
                    removeArrows,
                    wait(400)
                ]),
                waitForEvent(GameTutorEvents.ARROW_SENDED)
            ]),
            hideCursor,
            clear,
            waitForEvent(GameTutorEvents.BUILDING_CAPTURED)
        ]);
    }

    private function itemClick(itemType:ItemType):ITutorCommand {
        return sequence(new <ITutorCommand>[
            unlockItem(itemType),
            addText(view.tutor.locale.tutorItemClick(itemType)),
            magicItemsEnableMouse,
            wait(500),
            showCursor,
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tweenFromCornerToItem(itemType),
                    wait(500),
                    click,
                    wait(500)
                ]),
                waitForEvent(GameTutorEvents.selected(itemType))
            ]),
            magicItemsDisableMouse,
            clear
        ]);
    }

    private function cast(itemType:ItemType, buildingPos:Point):ITutorCommand {
        return sequence(new <ITutorCommand>[
            addText(view.tutor.locale.tutorItemCast(itemType)),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    wait(500),
                    showCursor,
                    tweenFromItem(itemType, buildingPos),
                    wait(500),
                    click,
                    wait(500)
                ]),
                waitForEvent(GameTutorEvents.casted(itemType))
            ]),
            hideCursor,
            clear
        ]);
    }

    private function get tornadoPoints():Vector.<Point> {
        const points:Vector.<Point> = new <Point>[];
        const deltaX:int = 200;
        const deltaY:int = 50;
        const corner:Point = tornadoBuilding;
        const pos:Point = new Point(corner.x - deltaX / 2, corner.y - deltaY / 2);
        const angle:Number = Math.PI / 4;
        for (var x:int = 0; x < deltaX; x++) {
            const y:Number = -deltaY / 2 + Math.sin(x * 2 * Math.PI / deltaX) * deltaY / 2;

            const ax:Number = x * Math.cos(angle) - y * Math.sin(angle);
            const ay:Number = x * Math.sin(angle) + y * Math.cos(angle);

            points.push(new Point(pos.x + ax + 39, pos.y + ay));
        }
        return points;
    }

    private function playTornado():ITutorCommand {
        const points:Vector.<Point> = tornadoPoints;

        return sequence(new <ITutorCommand>[
            addText(view.tutor.locale.tutorItemCast(ItemType.TORNADO)),
            wait(500),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    wait(400),
                    showCursor,
                    tweenFromItem(ItemType.TORNADO, points[0]),
                    mouseDown,
                    wait(400),
                    startDrawTornado(points),
                    tweenPath(points),
                    wait(400),
                    mouseUp,
                    endDrawTornado,
                    wait(400)
                ]),
                waitForEvent(GameTutorEvents.casted(ItemType.TORNADO))
            ]),
            hideCursor,
            clear
        ]);
    }

    private function _lockAllItems():void {
        for each(var itemType:ItemType in ItemType.values)
            view.magicItems.tutorLock(itemType, true);
    }

    private function _unlockItem(itemType:ItemType):void {
        view.magicItems.tutorLock(itemType, false)
    }

    private function get myTower():Point {
        return buildings.byId(buildings.myTower(players.selfId)).pos;
    }

    private function get bigTower():Point {
        return buildings.byId(buildings.bigTower(players.selfId)).pos;
    }

    private var _canCaptureBigTower:Boolean;

    public function get canCaptureBigTower():Boolean {
        return _canCaptureBigTower;
    }

    private var _canArrows:Boolean;

    public function get canArrows():Boolean {
        return _canArrows;
    }

    // COMMANDS

    private function get enableCaptureBigTower():ITutorCommand {
        return exec(function ():void {
            _canCaptureBigTower = true
        })
    }

    private function get enableArrows():ITutorCommand {
        return exec(function ():void {
            _canArrows = true
        })
    }

    private function get castAssistance():ITutorCommand {
        return cast(ItemType.ASSISTANCE, myTower);
    }

    private function get castStrengthening():ITutorCommand {
        return cast(ItemType.STRENGTHENING, myTower);
    }

    private function get captureBigTowerTutor():ITutorCommand {
        return arrowTutor(myTower, bigTower);
    }

    private function get lockAllItems():ITutorCommand {
        return exec(_lockAllItems)
    }

    private function unlockItem(itemType:ItemType):ITutorCommand {
        return exec(function ():void {
            _unlockItem(itemType)
        })
    }

    private function get highlightSelfBuildings():ITutorCommand {
        return exec(_highlightSelfBuildings);
    }

    private function get highlightNextEnemyBuildings():ITutorCommand {
        return exec(_highlightNextEnemyBuildings);
    }

    private function get unhighlightBuildings():ITutorCommand {
        return exec(view.tutorUnblur);
    }

    private function get disableMouse():ITutorCommand {
        return new Exec(function ():void {
            view.mouseEnabled = false;
        })
    }

    private function get enableMouse():ITutorCommand {
        return new Exec(function ():void {
            view.mouseEnabled = true;
        })
    }

    private function get magicItemsDisableMouse():ITutorCommand {
        return new Exec(function ():void {
            view.magicItems.mouseChildren = view.magicItems.mouseEnabled = false;
        })
    }

    private function get magicItemsEnableMouse():ITutorCommand {
        return new Exec(function ():void {
            view.magicItems.mouseChildren = view.magicItems.mouseEnabled = true;
        })
    }

    private function get hideMagicItems():ITutorCommand {
        return new Exec(function ():void {
            view.magicItems.visible = false;
        })
    }

    private function get showMagicItems():ITutorCommand {
        return new Exec(function ():void {
            view.magicItems.visible = true;
        })
    }

    private function tweenGame(a:Point, b:Point):ITutorCommand {
        return view.tutor.tweenGame(a, b);
    }

    private function tweenFromCorner(b:Point):ITutorCommand {
        return view.tutor.tweenFromCorner(b);
    }

    private function tweenFromItem(itemType:ItemType, b:Point):ITutorCommand {
        return view.tutor.tweenFromItem(itemType, b);
    }

    private function tweenFromCornerToItem(itemType:ItemType):ITutorCommand {
        return view.tutor.tweenFromCornerToItem(itemType);
    }

    private function tweenPath(points:Vector.<Point>):ITutorCommand {
        return view.tutor.tweenPath(points);
    }

    private function addText(text:String):ITutorCommand {
        return exec(function ():void {
            view.tutor.addText(text)
        });
    }

    private function get addNextButton():ITutorCommand {
        return exec(view.tutor.addNextButton);
    }

    private function addArrow(startPos:Point):ITutorCommand {
        return exec(function ():void {
            view.tutor.arrows.addArrow(startPos)
        })
    }

    private function get removeArrows():ITutorCommand {
        return exec(view.tutor.arrows.removeArrows);
    }

    private function startDrawTornado(points:Vector.<Point>):ITutorCommand {
        return exec(function ():void {
            view.tutor.startDrawTornado(points)
        });
    }

    private function get endDrawTornado():ITutorCommand {
        return exec(view.tutor.endDrawTornado);
    }

    private function get waitForClick():ITutorCommand {
        return new WaitForClick(view.stage);
    }

    private function waitForEvent(eventName:String):ITutorCommand {
        return new WaitForEvent(dispatcher, eventName)
    }

    // COORDS

    private static function ij(i:int, j:int):Point {
        return new Point((i + 0.5) * CellSize.SIZE.id(), (j + 0.5) * CellSize.SIZE.id())
    }

    private function get sourceBuilding1():Point {
        return players.isBigGame ? ij(2, 0) : ij(3, 0);
    }

    private function get targetBuilding1():Point {
        return players.isBigGame ? ij(4, 3) : ij(6, 3);
    }

    private function get sourceBuilding2_1():Point {
        return players.isBigGame ? ij(0, 0) : ij(1, 0);
    }

    private function get sourceBuilding2_2():Point {
        return players.isBigGame ? ij(4, 0) : ij(5, 0);
    }

    private function get targetBuilding2():Point {
        return players.isBigGame ? ij(2, 5) : ij(4, 3);
    }

    private function get fireballBuilding():Point {
        return players.isBigGame ? ij(10, 11) : ij(10, 11);
    }

    private function get volcanoBuilding():Point {
        return players.isBigGame ? ij(8, 8) : ij(8, 8);
    }

    private function get tornadoBuilding():Point {
        return players.isBigGame ? ij(10, 11) : ij(10, 11);
    }
}
}
