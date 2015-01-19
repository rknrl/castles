package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.game.area.TornadoPathView;

public class TornadoPath {
    private static const tornadoMaxPoints:int = 10;
    private static const tornadoInterval:int = 100;

    private var view:TornadoPathView;

    public function TornadoPath(view:TornadoPathView) {
        this.view = view;
    }

    private var _drawing:Boolean;

    public function get drawing():Boolean {
        return _drawing;
    }

    public const points:Vector.<Point> = new <Point>[];

    public function startDraw(mousePos:Point):void {
        _drawing = true;
        mouseMove(mousePos);
    }

    private var lastTime:int;

    public function mouseMove(mousePos:Point):void {
        if (_drawing) {
            const time:int = getTimer();
            if (time - lastTime > tornadoInterval) {
                points.push(mousePos);
                if (points.length > tornadoMaxPoints) {
                    points.shift();
                }
                lastTime = time;
                if (points.length >= 2) {
                    const p:Points = new Points(points);
                    view.drawPath(p, p.totalDistance);
                }
            }
        }
    }

    public function endDraw():void {
        _drawing = false;
        view.clear();
        points.length = 0;
    }
}
}
