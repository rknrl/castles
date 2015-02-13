//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area.volcanoes {
import flash.display.Sprite;

public class VolcanoView extends Sprite {
    private var originalSize:Number;

    public function VolcanoView(radius:Number) {
        var volcanoMC:VolcanoMC = new VolcanoMC();
        originalSize = volcanoMC.width;
        addChild(volcanoMC);
        this.radius = radius;
    }

    public function set radius(value:Number):void {
        scaleX = scaleY = value / originalSize;
    }
}
}
