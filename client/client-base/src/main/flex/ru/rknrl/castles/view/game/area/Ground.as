//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.core.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.dto.CellSize;

public class Ground extends Sprite {
    private static const cellSize:int = CellSize.SIZE.id();

    private const grounds:Dictionary = new Dictionary();

    private function getGround(i:int, j:int):Bitmap {
        const bitmap:Bitmap = grounds[i + "_" + j];
        if (!bitmap) throw new Error("can't find ground " + i + " " + j);
        return bitmap;
    }

    public function Ground(h:int, v:int) {
        const gap:int = 2;

        for (var i:int = 0; i < h; i++) {
            for (var j:int = 0; j < v; j++) {
                const bitmap:Bitmap = new Bitmap(Colors.grassBitmapData);
                bitmap.width = cellSize - gap;
                bitmap.height = cellSize - gap;
                bitmap.x = i * cellSize + gap / 2;
                bitmap.y = j * cellSize + gap / 2;
                grounds[i + "_" + j] = bitmap;
                addChild(bitmap);
            }
        }
    }

    public function updateGroundColor(pos:Point, owner:BuildingOwner):void {
        const i:int = pos.x / cellSize;
        const j:int = pos.y / cellSize;
        getGround(i, j).bitmapData = Colors.groundBitmapData(owner);
    }
}
}
