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
import flash.events.MouseEvent;
import flash.utils.getTimer;
import flash.utils.setTimeout;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.GameSplashView;
import ru.rknrl.castles.view.utils.tutor.commands.Exec;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.InfinityWait;
import ru.rknrl.castles.view.utils.tutor.commands.TutorParallelCommands;
import ru.rknrl.castles.view.utils.tutor.commands.TutorSequenceCommands;
import ru.rknrl.castles.view.utils.tutor.commands.Wait;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitIdDTO;

public class GameSplash extends EventDispatcher {
    public static const GAME_SPLASH_COMPLETE:String = "gameSplashComplete";
    private static const buildingRadius:int = 48;

    private var view:GameSplashView;
    private var unit:Unit;

    public function GameSplash(view:GameSplashView) {
        this.view = view;

        view.addEventListener(Event.ENTER_FRAME, onEnterFrame);
        view.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        view.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);

        view.tutor.play(new <ITutorCommand>[
            new TutorParallelCommands(new <ITutorCommand>[
                new TutorSequenceCommands(new <ITutorCommand>[
                    new Exec(view.tutor.showCursor),
                    view.tutor.tween(view.tutor.screenCorner, view.tower1Pos),
                    new Exec(view.tutor.mouseDown),
                    new Wait(400),
                    new Exec(function ():void {
                        view.tutor.arrows.addArrow(view.tower1Pos);
                    }),
                    view.tutor.tween(view.tower1Pos, view.tower2Pos),
                    new Wait(400),
                    new Exec(view.tutor.mouseUp),
                    new Exec(view.tutor.arrows.removeArrows),
                    new Wait(400)
                ], true),
                new InfinityWait()
            ])
        ]);
    }

    private function get mousePos():Point {
        return new Point(view.mouseX, view.mouseY);
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();

        view.arrows.orientArrows(mousePos);

        if (unit) {
            view.units.setPos(unit.id.id, unit.pos(time));
            if (unit.needRemove(time)) {
                view.units.remove(unit.id.id);
                unit = null;
                view.tower2.owner = new BuildingOwner(true, DtoMock.playerId(0));
                view.tower2.count = 1;
                setTimeout(addLoadingScreen, 1000);
            }
        }
    }

    private function addLoadingScreen():void {
        view.addLoadingScreen();
        setTimeout(function ():void {
            dispatchEvent(new Event(GAME_SPLASH_COMPLETE))
        }, 3000)
    }

    private var down:Boolean;
    private var move:Boolean;

    private function onMouseDown(event:MouseEvent):void {
        view.tutor.visible = false;
        if (mousePos.distance(view.tower1Pos) < buildingRadius) {
            down = true;
            view.arrows.addArrow(view.tower1Pos);
        }
    }

    private function onMouseUp(event:MouseEvent):void {
        if (down) {
            down = false;
            view.arrows.removeArrows();

            if (mousePos.distance(view.tower2Pos) < buildingRadius) {
                move = true;
                view.mouseEnabled = false;

                const dto:UnitDTO = new UnitDTO();
                dto.id = new UnitIdDTO();
                dto.id.id = 0;
                dto.type = BuildingType.TOWER;
                dto.count = 4;
                dto.pos = DtoMock.point(view.tower1.x, view.tower1.y);
                dto.owner = DtoMock.playerId(0);
                dto.speed = 0.04;
                dto.strengthened = false;

                const startPos:Point = view.tower1Pos;
                const endPos:Point = view.tower2Pos;

                unit = new Unit(dto.id, dto.owner, startPos, endPos, getTimer(), dto.speed, dto.count);
                view.units.addUnit(dto.id, dto.type, BuildingLevel.LEVEL_1, dto.owner, dto.count, dto.strengthened, endPos);
                view.tower1.count = 4;
            } else {
                view.tutor.visible = true;
            }
        }
    }
}
}
