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
import ru.rknrl.castles.view.utils.tutor.commands.TutorParallelCommands;
import ru.rknrl.castles.view.utils.tutor.commands.TutorSequenceCommands;
import ru.rknrl.castles.view.utils.tutor.commands.Wait;
import ru.rknrl.castles.view.utils.tutor.commands.WaitForClick;
import ru.rknrl.castles.view.utils.tutor.commands.WaitForEvent;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.PlayerDTO;

public class GameTutorController {
    private var view:GameView;
    private var dispatcher:EventDispatcher;
    private var players:Players;
    private var buildings:Buildings;

    public function GameTutorController(view:GameView, dispatcher:EventDispatcher, players:Players, buildings:Buildings) {
        this.view = view;
        this.dispatcher = dispatcher;
        this.players = players;
        this.buildings = buildings;
    }

    public function firstGame():ITutorCommand {
        return sequence(new <ITutorCommand>[
            disableMouse,
            hideMagicItems,
            wait(2000),
            hideCursor,

            // self buildings

            addSelfBuildingsText,
            highlightSelfBuildings,
            wait(1500),
            addNextButton,
            waitForClick,
            unhighlightBuildings,
            clear,

            // enemies buildings

            addEnemyBuildingsText(players.isBigGame),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    highlightNextEnemyBuildings,
                    wait(1000)
                ]),
                sequence(new <ITutorCommand>[
                    wait(2000),
                    addNextButton,
                    waitForClick
                ])
            ]),
            unhighlightBuildings,
            clear,

            // arrow

            enableMouse,
            showCursor,
            addArrowText,
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tweenFromCorner(sourceBuilding1),
                    mouseDown,
                    wait(400),
                    addArrow(sourceBuilding1),
                    tweenGame(sourceBuilding1, targetBuilding1),
                    wait(400),
                    mouseUp,
                    removeArrows,
                    wait(400)
                ]),
                waitForEvent(GameTutorEvents.ARROW_SENDED)
            ]),
            hideCursor,
            clear,
            waitForEvent(GameTutorEvents.BUILDING_CAPTURED),

            // arrows

            showCursor,
            addArrowsText,
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

            itemClick(ItemType.FIREBALL),
            cast(ItemType.FIREBALL, buildings.byId(buildings.getEnemyBuildingId(players.selfId)).pos),

            // strengthening

            itemClick(ItemType.STRENGTHENING),
            cast(ItemType.STRENGTHENING, buildings.byId(buildings.getBuildingId(players.selfId)).pos),

            // volcano

            itemClick(ItemType.VOLCANO),
            cast(ItemType.VOLCANO, buildings.byId(buildings.getEnemyBuildingId(players.selfId)).pos),

            // tornado

            itemClick(ItemType.TORNADO),
            playTornado(),

            // assistance

            itemClick(ItemType.ASSISTANCE),
            cast(ItemType.ASSISTANCE, buildings.byId(buildings.getBuildingId(players.selfId)).pos),

            // win

            hideCursor,
            addWinText
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

    private function itemClick(itemType:ItemType):ITutorCommand {
        return sequence(new <ITutorCommand>[
            showCursor,
            addMagicItemText(itemType),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tweenFromCornerToItem(itemType),
                    wait(500),
                    click,
                    wait(500)
                ]),
                waitForEvent(GameTutorEvents.selected(itemType))
            ]),
            clear
        ]);
    }

    private function cast(itemType:ItemType, buildingPos:Point):ITutorCommand {
        return sequence(new <ITutorCommand>[
            addMagicItemText(itemType),
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
            clear
        ]);
    }

    private function get tornadoPoints():Vector.<Point> {
        const points:Vector.<Point> = new <Point>[];
        const deltaX:int = 200;
        const deltaY:int = 50;
        const corner:Point = view.tutor.screenCorner;
        const pos:Point = new Point(corner.x - deltaX, corner.y - deltaY);
        for (var x:int = 0; x < deltaX; x++) {
            points.push(new Point(pos.x + x, pos.y + Math.sin(x * 2 * Math.PI / deltaX) * deltaY))
        }
        return points;
    }

    private function playTornado():ITutorCommand {
        const points:Vector.<Point> = tornadoPoints;

        return sequence(new <ITutorCommand>[
            addMagicItemText(ItemType.TORNADO),
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
            clear
        ]);
    }

    // COMMANDS

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

    private function get showCursor():ITutorCommand {
        return exec(view.tutor.showCursor);
    }

    private function get hideCursor():ITutorCommand {
        return exec(view.tutor.hideCursor);
    }

    private function get mouseDown():ITutorCommand {
        return exec(view.tutor.mouseDown);
    }

    private function get mouseUp():ITutorCommand {
        return exec(view.tutor.mouseUp);
    }

    private function get click():ITutorCommand {
        return exec(view.tutor.click);
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

    private function get addSelfBuildingsText():ITutorCommand {
        return exec(view.tutor.addSelfBuildingsText);
    }

    private function addEnemyBuildingsText(isBigGame:Boolean):ITutorCommand {
        return exec(function ():void {
            view.tutor.addEnemyBuildingsText(isBigGame)
        });
    }

    private function get addArrowText():ITutorCommand {
        return exec(view.tutor.addArrowText);
    }

    private function get addArrowsText():ITutorCommand {
        return exec(view.tutor.addArrowsText);
    }

    private function addMagicItemText(itemType:ItemType):ITutorCommand {
        return exec(function ():void {
            view.tutor.addMagicItemText(itemType)
        });
    }

    private function get addWinText():ITutorCommand {
        return exec(view.tutor.addWinText);
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

    private function get clear():ITutorCommand {
        return exec(view.tutor.clear);
    }

    private function startDrawTornado(points:Vector.<Point>):ITutorCommand {
        return exec(function ():void {
            view.tutor.startDrawTornado(points)
        });
    }

    private function get endDrawTornado():ITutorCommand {
        return exec(view.tutor.endDrawTornado);
    }

    private static function parallel(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorParallelCommands(commands);
    }

    private static function sequence(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, false);
    }

    private static function loop(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, true);
    }

    private static function wait(duration:int):ITutorCommand {
        return new Wait(duration);
    }

    private function get waitForClick():ITutorCommand {
        return new WaitForClick(view.stage);
    }

    private function waitForEvent(eventName:String):ITutorCommand {
        return new WaitForEvent(dispatcher, eventName)
    }

    private static function exec(func:Function):ITutorCommand {
        return new Exec(func);
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
}
}
