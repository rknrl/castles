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
    }

    private static const tutorInterval:int = 5000;
    private var lastTutorTime:int;

    private function get mousePos():Point {
        return new Point(view.mouseX, view.mouseY);
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        if (!down && !move && time - lastTutorTime > tutorInterval) {
            view.playArrow();
            lastTutorTime = time;
        }

        view.arrows.orientArrows(mousePos);

        if (unit) {
            view.units.setPos(unit.id.id, unit.pos(time));
            if (unit.needRemove(time)) {
                view.units.remove(unit.id.id);
                unit = null;
                view.tower2.owner = new BuildingOwner(true, DtoMock.playerId(0));
                setTimeout(function ():void {
                    dispatchEvent(new Event(GAME_SPLASH_COMPLETE))
                }, 1000)
            }
        }
    }

    private var down:Boolean;
    private var move:Boolean;

    private function onMouseDown(event:MouseEvent):void {
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
                dto.count = 3;
                dto.pos = DtoMock.point(view.tower1.x, view.tower1.y);
                dto.owner = DtoMock.playerId(0);
                dto.speed = 0.04;
                dto.strengthened = false;

                const startPos:Point = view.tower1Pos;
                const endPos:Point = view.tower2Pos;

                unit = new Unit(dto.id, startPos, endPos, getTimer(), dto.speed, dto.count);
                view.units.addUnit(dto.id, dto.type, BuildingLevel.LEVEL_1, dto.owner, dto.count, dto.strengthened, endPos);
            }
        }
    }
}
}
