//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.navigate.points {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.navigate.*;
import ru.rknrl.display.Align;

public class NavigationPoints extends Sprite {
    private const points:Vector.<NavigationPoint> = new <NavigationPoint>[];

    public function NavigationPoints(screens:Vector.<Screen>) {
        for each(var screen:Screen in screens) {
            const point:NavigationPoint = new NavigationPoint(screen);
            addChild(point);
            points.push(point);
        }
        _pointsWidth = Align.horizontal(Vector.<DisplayObject>(points), Layout.navigationPointSize, Layout.navigationPointGap);
    }

    private var _pointsWidth:Number;

    public function get pointsWidth():Number {
        return _pointsWidth * scaleX;
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
