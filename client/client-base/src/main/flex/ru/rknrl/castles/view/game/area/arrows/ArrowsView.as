package ru.rknrl.castles.view.game.area.arrows {
import flash.display.Sprite;

import ru.rknrl.castles.utils.points.Point;

public class ArrowsView extends Sprite {
    private const arrows:Vector.<ArrowView> = new <ArrowView>[];

    public function addArrow(startPos:Point):void {
        const arrow:ArrowView = new ArrowView(startPos);
        addChild(arrow);
        arrows.push(arrow);
    }

    public function orientArrows(endPos:Point):void {
        for each(var arrow:ArrowView in arrows) arrow.orient(endPos);
    }

    public function removeArrows():void {
        for each(var arrow:ArrowView in arrows) removeChild(arrow);
        arrows.length = 0;
    }
}
}
