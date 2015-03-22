//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {

import flash.events.EventDispatcher;
import flash.utils.Dictionary;
import flash.utils.clearInterval;
import flash.utils.setInterval;

import ru.rknrl.castles.model.game.Buildings;
import ru.rknrl.castles.model.game.GameTutorEvents;
import ru.rknrl.castles.model.game.Players;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.game.GameTutorialView;
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

    public function play():void {
        view.tutor.play(new <ITutorCommand>[
            disableMouse,
            hideMagicItems,
            wait(2000),
            hideCursor,

            // self buildings

            addText("Твои домики желтого цвета"),
            highlightSelfBuildings,
            wait(1500),
            addButton("Дальше"),
            waitForClick,
            tutorUnblur,
            clear,

            // enemies buildings

            addText(players.isBigGame ? "У тебя 3 противника" : "Твой противник бирюзовый"),
            parallel(new <ITutorCommand>[
                startHighlightEnemyBuildings,
                sequence(new <ITutorCommand>[
                    wait(2000),
                    addButton("Дальше"),
                    waitForClick
                ])
            ]),
            stopHighlightEnemyBuildings,
            tutorUnblur,
            clear,

            // arrow

            enableMouse,
            showCursor,
            addText("Отправляй отряды и захватывай чужие домики"),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tween(view.tutor.screenCorner, view.tutor.toGlobal(sourceBuilding1)),
                    mouseDown,
                    wait(400),
                    addArrow(sourceBuilding1),
                    tween(view.tutor.toGlobal(sourceBuilding1), view.tutor.toGlobal(targetBuilding1)),
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
            addText("Можно отправлять отряды сразу из нескольких домиков"),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tween(view.tutor.screenCorner, view.tutor.toGlobal(sourceBuilding2_1)),
                    mouseDown,
                    wait(400),
                    addArrow(sourceBuilding2_1),
                    tween(view.tutor.toGlobal(sourceBuilding2_1), view.tutor.toGlobal(sourceBuilding2_2)),
                    wait(400),
                    addArrow(sourceBuilding2_2),
                    tween(view.tutor.toGlobal(sourceBuilding2_2), view.tutor.toGlobal(targetBuilding2)),
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
            addText("Захвати все домики противников, чтобы выиграть"),
//            exec(server.startTutorGame)
        ]);
    }

    private function _highlightSelfBuildings():void {
        view.tutorBlur(Players.playersToIds(players.getEnemiesPlayers(players.selfId)), buildings.notPlayerId(players.selfId));
    }

    private var enemyIndex:int;
    private var highlightInterval:int;

    private function _startHighlightEnemyBuildings():void {
        highlightEnemyBuildings();
        highlightInterval = setInterval(highlightEnemyBuildings, 1000);
    }

    private function highlightEnemyBuildings():void {
        const player:PlayerDTO = players.getEnemiesPlayers(players.selfId)[enemyIndex];
        view.tutorUnblur();
        view.tutorBlur(Players.playersToIds(players.getEnemiesPlayers(player.id)), buildings.notPlayerId(player.id));
        enemyIndex++;
        if (enemyIndex == 3) enemyIndex = 0;
    }

    private function _stopHighlightEnemyBuildings():void {
        clearInterval(highlightInterval);
    }

    private static const itemsText:Dictionary = createItemsText();

    private static function createItemsText():Dictionary {
        const itemsText:Dictionary = new Dictionary();
        itemsText[ItemType.FIREBALL] = "Запусти фаербол в противника";
        itemsText[ItemType.VOLCANO] = "Создай вулкан под башней противника";
        itemsText[ItemType.STRENGTHENING] = "Усилить свой домик";
        itemsText[ItemType.ASSISTANCE] = "Вызывай подмогу";
        itemsText[ItemType.TORNADO] = "Используй торнадо против противника";
        return itemsText;
    }

    public function itemClick(itemType:ItemType):ITutorCommand {
        const pos:Point = view.tutor.layout.gameMagicItem(GameTutorialView.indexOf(itemType));

        return sequence(new <ITutorCommand>[
            showCursor,
            addText(itemsText[itemType]),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tween(view.tutor.screenCorner, pos),
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
        const pos:Point = view.tutor.layout.gameMagicItem(GameTutorialView.indexOf(itemType));

        return sequence(new <ITutorCommand>[
            addText(itemsText[itemType]),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    wait(500),
                    showCursor,
                    tween(pos, view.tutor.toGlobal(buildingPos)),
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
        const pos:Point = new Point(0, 0);//new Point(width - deltaX, height - deltaY);
        for (var x:int = 0; x < deltaX; x++) {
            points.push(new Point(pos.x + x, pos.y + Math.sin(x * 2 * Math.PI / deltaX) * deltaY))
        }
        return points;
    }

    public function playTornado():ITutorCommand {
        const points:Vector.<Point> = tornadoPoints;
        view.tutor.tornadoPoints = new Points(points);

        const pos:Point = view.tutor.layout.gameMagicItem(GameTutorialView.indexOf(ItemType.TORNADO));

        return sequence(new <ITutorCommand>[
            addText(itemsText[ItemType.TORNADO]),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    wait(400),
                    showCursor,
                    tween(pos, view.tutor.toGlobal(points[0])),
                    mouseDown,
                    wait(400),
                    addTornadoPath,
                    tweenPath(new Points(view.tutor.toGlobalPoints(points))),
                    wait(400),
                    mouseUp,
                    removeTornadoPath,
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

    private function get startHighlightEnemyBuildings():ITutorCommand {
        return exec(_startHighlightEnemyBuildings);
    }

    private function get stopHighlightEnemyBuildings():ITutorCommand {
        return exec(_stopHighlightEnemyBuildings);
    }

    private function get tutorUnblur():ITutorCommand {
        return exec(view.tutorUnblur);
    }

    public function get disableMouse():ITutorCommand {
        return new Exec(function ():void {
            view.mouseEnabled = false;
        })
    }

    public function get enableMouse():ITutorCommand {
        return new Exec(function ():void {
            view.mouseEnabled = true;
        })
    }

    public function get hideMagicItems():ITutorCommand {
        return new Exec(function ():void {
            view.magicItems.visible = false;
        })
    }

    public function get showMagicItems():ITutorCommand {
        return new Exec(function ():void {
            view.magicItems.visible = true;
        })
    }

    public function get showCursor():ITutorCommand {
        return exec(view.tutor.showCursor);
    }

    public function get hideCursor():ITutorCommand {
        return exec(view.tutor.hideCursor);
    }

    public function get mouseDown():ITutorCommand {
        return exec(view.tutor.mouseDown);
    }

    public function get mouseUp():ITutorCommand {
        return exec(view.tutor.mouseUp);
    }

    public function get click():ITutorCommand {
        return exec(view.tutor.click);
    }

    public function tween(a:Point, b:Point):ITutorCommand {
        return view.tutor.tween(a, b);
    }

    public function tweenPath(points:Points):ITutorCommand {
        return view.tutor.tweenPath(points);
    }

    public function addText(text:String):ITutorCommand {
        return exec(function ():void {
            view.tutor.addText(text)
        });
    }

    public function addButton(text:String):ITutorCommand {
        return exec(function ():void {
            view.tutor.addButton(text)
        });
    }

    public function addArrow(startPos:Point):ITutorCommand {
        return exec(function ():void {
            view.tutor.arrows.addArrow(startPos)
        })
    }

    private function get removeArrows():ITutorCommand {
        return exec(view.tutor.arrows.removeArrows);
    }

    public function get clear():ITutorCommand {
        return exec(view.tutor.clear);
    }

    public function get addTornadoPath():ITutorCommand {
        return exec(view.tutor.addTornadoPath);
    }

    public function get removeTornadoPath():ITutorCommand {
        return exec(view.tutor.removeTornadoPath);
    }

    public static function parallel(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorParallelCommands(commands);
    }

    public static function sequence(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, false);
    }

    public static function loop(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, true);
    }

    public static function wait(duration:int):ITutorCommand {
        return new Wait(duration);
    }

    public function get waitForClick():ITutorCommand {
        return new WaitForClick(view.stage);
    }

    public function waitForEvent(eventName:String):ITutorCommand {
        return new WaitForEvent(dispatcher, eventName)
    }

    public static function exec(func:Function):ITutorCommand {
        return new Exec(func);
    }

    // COORDS

    private static function ij(i:int, j:int):Point {
        return new Point((i + 0.5) * CellSize.SIZE.id(), (j + 0.5) * CellSize.SIZE.id())
    }

    public function get sourceBuilding1():Point {
        return players.isBigGame ? ij(2, 0) : ij(3, 0);
    }

    public function get targetBuilding1():Point {
        return players.isBigGame ? ij(4, 3) : ij(6, 3);
    }

    public function get sourceBuilding2_1():Point {
        return players.isBigGame ? ij(0, 0) : ij(1, 0);
    }

    public function get sourceBuilding2_2():Point {
        return players.isBigGame ? ij(4, 0) : ij(5, 0);
    }

    public function get targetBuilding2():Point {
        return players.isBigGame ? ij(2, 5) : ij(4, 3);
    }
}
}
