//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;
import flash.utils.setTimeout;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.game.Unit;
import ru.rknrl.castles.view.game.GameSplashView;
import ru.rknrl.castles.view.game.area.units.UnitView;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.UnitDTO;
import ru.rknrl.dto.UnitId;

public class GameSplash extends TutorControllerBase {
    public static const GAME_SPLASH_COMPLETE:String = "gameSplashComplete";
    private static const buildingRadius:int = 48;

    private var view:GameSplashView;
    private var unit:Unit;
    private var unitView:UnitView;

    public function GameSplash(view:GameSplashView) {
        super(view.tutor);
        this.view = view;

        view.addEventListener(Event.ENTER_FRAME, onEnterFrame);
        view.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
        view.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);

        view.tutor.play(
                parallel(new <ITutorCommand>[
                    loop(new <ITutorCommand>[
                        showCursor,
                        view.tutor.tween(view.tutor.screenCorner, view.tower1Pos),
                        mouseDown,
                        wait(400),
                        exec(function ():void {
                            view.tutor.arrows.addArrow(view.tower1Pos);
                        }),
                        view.tutor.tween(view.tower1Pos, view.tower2Pos),
                        wait(400),
                        mouseUp,
                        exec(view.tutor.arrows.removeArrows),
                        wait(1000)
                    ]),
                    infinityWait
                ])
        );
    }

    private function get mousePos():Point {
        return new Point(view.mouseX, view.mouseY);
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();

        view.arrows.orientArrows(mousePos);

        if (unit) {
            unitView.x = unit.pos(time).x;
            unitView.y = unit.pos(time).y;
            if (unit.needRemove(time)) {
                view.units.removeChild(unitView);
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
                view.mouseEnabled = false;

                const dto:UnitDTO = new UnitDTO();
                dto.id = new UnitId();
                dto.id.id = 0;
                dto.buildingType = BuildingType.TOWER;
                dto.count = 4;
                dto.pos = DtoMock.point(view.tower1.x, view.tower1.y);
                dto.owner = DtoMock.playerId(0);
                dto.duration = 4000;
                dto.strengthened = false;

                const startPos:Point = view.tower1Pos;
                const endPos:Point = view.tower2Pos;
                const points:Points = Points.two(startPos, endPos);

                unit = new Unit(dto.owner, points, getTimer(), dto.duration, dto.count);
                unitView = new UnitView(dto.buildingType, BuildingLevel.LEVEL_1, dto.owner, dto.count, dto.strengthened);
                view.units.addChild(unitView);
                view.tower1.count = 4;
            } else {
                view.tutor.visible = true;
            }
        }
    }
}
}
