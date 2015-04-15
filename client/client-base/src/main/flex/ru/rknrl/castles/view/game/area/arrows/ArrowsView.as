//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.arrows {
import flash.display.Sprite;

import ru.rknrl.core.points.Point;

public class ArrowsView extends Sprite {
    private const arrows:Vector.<ArrowView> = new <ArrowView>[];

    public function addArrow(startPos:Point):ArrowView {
        const arrow:ArrowView = new ArrowView(startPos);
        addChild(arrow);
        arrows.push(arrow);
        return arrow;
    }

    public function orientArrows(endPos:Point):void {
        for each(var arrow:ArrowView in arrows) arrow.orient(endPos);
    }

    public function removeArrow(arrow:ArrowView):void {
        arrows.splice(arrows.indexOf(arrow), 1);
        removeChild(arrow);
    }

    public function removeArrows():void {
        for each(var arrow:ArrowView in arrows) removeChild(arrow);
        arrows.length = 0;
    }
}
}
