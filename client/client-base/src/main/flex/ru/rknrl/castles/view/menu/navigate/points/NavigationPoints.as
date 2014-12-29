package ru.rknrl.castles.view.menu.navigate.points {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.navigate.*;
import ru.rknrl.castles.view.utils.Align;

public class NavigationPoints extends Sprite {
    private const points:Vector.<NavigationPoint> = new <NavigationPoint>[];

    public function NavigationPoints(screens:Vector.<Screen>) {
        for each(var screen:Screen in screens) {
            const point:NavigationPoint = new NavigationPoint(screen);
            addChild(point);
            points.push(point);
        }
        Align.horizontal(Vector.<DisplayObject>(points), Layout.navigationPointSize, Layout.navigationPointGap);
    }

    private function getPoint(screen:Screen):NavigationPoint {
        for each(var point:NavigationPoint in points) {
            if (point.screen == screen) return point;
        }
        throw new Error("can't find point for screen " + screen);
    }

    public function set selected(screen:Screen):void {
        for each(var point:NavigationPoint in points) {
            point.selected = false;
        }
        getPoint(screen).selected = true;
    }
}
}
