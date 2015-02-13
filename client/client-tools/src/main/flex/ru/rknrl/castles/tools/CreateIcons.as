//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.tools {
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.display.StageQuality;
import flash.geom.Matrix;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

[SWF(backgroundColor="#dddddd")]
public class CreateIcons extends Sprite {
    public function CreateIcons() {
        stage.quality = StageQuality.BEST;
        createIcon(16);
        createIcon(150);
        createIcon(256);
    }

    private function createIcon(size:int):void {
        const bitmapData:BitmapData = new BitmapData(size, size, false, 0xffffff);

        const x:int = size / 2;
        const y:int = size * 2 / 3;
        const scale:Number = size / 128;

        const shadow:DisplayObject = new Shadow();
        const shadowMatrix:Matrix = new Matrix();
        shadowMatrix.scale(scale, scale);
        shadowMatrix.translate(x, y);
        bitmapData.drawWithQuality(shadow, shadowMatrix, null, null, null, true, StageQuality.BEST);

        const tower:DisplayObject = Fla.createBuilding(BuildingType.TOWER, BuildingLevel.LEVEL_3);
        const matrix:Matrix = new Matrix();
        matrix.scale(scale, scale);
        matrix.translate(x, y);
        bitmapData.drawWithQuality(tower, matrix, Colors.transform(Colors.yellow), null, null, true, StageQuality.BEST);

        savePng("icon" + size, bitmapData);
    }
}
}
