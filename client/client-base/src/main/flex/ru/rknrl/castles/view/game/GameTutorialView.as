//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game {
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.game.area.TornadoPathView;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.TutorialView;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.dto.ItemType;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;

public class GameTutorialView extends TutorialView {
    private var arrows:ArrowsView;
    private var tornadoPath:TornadoPathView;

    public function GameTutorialView(layout:Layout, deviceFactory:DeviceFactory) {
        super(layout, deviceFactory);
        addChild(arrows = new ArrowsView());
        addChild(tornadoPath = new TornadoPathView());
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private static function indexOf(itemType:ItemType):int {
        return ItemType.values.indexOf(itemType);
    }

    private var _areaPos:Point;

    public function set areaPos(areaPos:Point):void {
        _areaPos = areaPos;
        arrows.scaleX = arrows.scaleY = layout.scale;
        tornadoPath.scaleX = tornadoPath.scaleY = layout.scale;
        arrows.x = tornadoPath.x = _areaPos.x;
        arrows.y = tornadoPath.y = _areaPos.y;
    }

    private function toGlobal(buildingPos:Point):Point {
        return new Point(_areaPos.x + buildingPos.x * layout.scale, _areaPos.y + buildingPos.y * layout.scale)
    }

    private function toGlobalPoints(points:Vector.<Point>):Vector.<Point> {
        const result:Vector.<Point> = new <Point>[];
        for each(var point:Point in points) result.push(toGlobal(point));
        return result;
    }

    public function clickItemAndCast(itemType:ItemType, buildingPos:Point):void {
        const pos:Point = layout.gameMagicItem(indexOf(itemType));

        play(new <ITutorCommand>[
            open,
            tween(screenCorner, pos),
            click,
            wait(400),
            tween(pos, toGlobal(buildingPos)),
            click,
            wait(400)
        ]);
    }

    public function playFireball(buildingPos:Point):void {
        clickItemAndCast(ItemType.FIREBALL, buildingPos);
    }

    public function playVolcano(buildingPos:Point):void {
        clickItemAndCast(ItemType.VOLCANO, buildingPos);
    }

    public function playStrengthening(buildingPos:Point):void {
        clickItemAndCast(ItemType.STRENGTHENING, buildingPos);
    }

    public function playAssistance(buildingPos:Point):void {
        clickItemAndCast(ItemType.ASSISTANCE, buildingPos);
    }

    public function playArrow(startBuildingPos:Point, endBuildingPos:Point):void {
        play(new <ITutorCommand>[
            open,
            tween(screenCorner, toGlobal(startBuildingPos)),
            mouseDown,
            wait(400),
            exec(function ():void {
                arrows.addArrow(startBuildingPos);
            }),
            tween(toGlobal(startBuildingPos), toGlobal(endBuildingPos)),
            wait(400),
            mouseUp,
            exec(arrows.removeArrows),
            wait(400)
        ]);
    }

    public function playArrows(startBuildingPos1:Point, startBuildingPos2:Point, endBuildingPos:Point):void {
        play(new <ITutorCommand>[
            open,
            tween(screenCorner, toGlobal(startBuildingPos1)),
            mouseDown,
            wait(400),
            exec(function ():void {
                arrows.addArrow(startBuildingPos1);
            }),
            tween(toGlobal(startBuildingPos1), toGlobal(startBuildingPos2)),
            wait(400),
            exec(function ():void {
                arrows.addArrow(startBuildingPos2);
            }),
            tween(toGlobal(startBuildingPos2), toGlobal(endBuildingPos)),
            wait(400),
            mouseUp,
            exec(arrows.removeArrows),
            wait(400)
        ]);
    }

    private var tornado:Boolean;
    private var tornadoStartTime:int;
    private var tornadoPoints:Points;

    public function playTornado(points:Vector.<Point>):void {
        tornadoPoints = new Points(points);

        const pos:Point = layout.gameMagicItem(indexOf(ItemType.TORNADO));

        function addTornadoPath():void {
            tornadoStartTime = getTimer();
            tornado = true;
        }

        function removeTornadoPath():void {
            tornado = false;
            tornadoPath.clear();
        }

        play(new <ITutorCommand>[
            open,
            tween(screenCorner, pos),
            click,
            wait(400),
            tween(pos, toGlobal(points[0])),
            mouseDown,
            wait(400),
            exec(addTornadoPath),
            tweenPath(new Points(toGlobalPoints(points))),
            wait(400),
            mouseUp,
            exec(removeTornadoPath),
            wait(400)
        ]);
    }

    private static const easer:IEaser = new Linear(0, 1);

    private function onEnterFrame(event:Event):void {
        arrows.orientArrows(new Point((cursor.x - _areaPos.x) / layout.scale, (cursor.y - _areaPos.y) / layout.scale));
        if (tornado) {
            const progress:Number = interpolate(0, 1, getTimer(), tornadoStartTime, 500, easer);
            tornadoPath.drawPath(tornadoPoints, tornadoPoints.totalDistance * progress);
        }
    }
}
}
