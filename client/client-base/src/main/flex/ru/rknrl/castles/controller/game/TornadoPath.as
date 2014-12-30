package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.castles.model.points.Point;
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

    private const _points:Vector.<Point> = new <Point>[];

    public function get points():Vector.<Point> {
        return _points;
    }

    public function startDraw(mousePos:Point):void {
        _drawing = true;
        mouseMove(mousePos);
    }

    private var lastTime:int;

    public function mouseMove(mousePos:Point):void {
        if (_drawing) {
            const time:int = getTimer();
            if (time - lastTime > tornadoInterval) {
                _points.push(mousePos);
                if (_points.length > tornadoMaxPoints) {
                    _points.shift();
                }
                lastTime = time;
                view.drawPath(_points);
            }
        }
    }

    public function endDraw():void {
        _drawing = false;
        view.clear();
        _points.length = 0;
    }
}
}
