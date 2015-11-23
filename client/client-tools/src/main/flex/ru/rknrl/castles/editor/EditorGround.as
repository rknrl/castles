//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.editor {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.view.Colors;
import protos.CellSize;

public class EditorGround extends Sprite {
    private static const cellSize:int = CellSize.SIZE.id();
    private static const grass:BitmapData = Colors.grassBitmapData;
    private static const yellow:BitmapData = Colors.lightBitmapData(Colors.yellow);
    private static const magenta:BitmapData = Colors.lightBitmapData(Colors.magenta);

    private const grounds:Dictionary = new Dictionary();

    private function getGround(i:int, j:int):Bitmap {
        return grounds[i + "_" + j];
    }

    private var h:int;
    private var v:int;

    public function EditorGround(h:int, v:int) {
        const gap:int = 2;

        this.h = h;
        this.v = v;

        for (var i:int = 0; i < h; i++) {
            for (var j:int = 0; j < v; j++) {
                const bitmap:Bitmap = new Bitmap(grass);
                bitmap.width = cellSize - gap;
                bitmap.height = cellSize - gap;
                bitmap.x = i * cellSize + gap / 2;
                bitmap.y = j * cellSize + gap / 2;
                grounds[i + "_" + j] = bitmap;
                addChild(bitmap);
            }
        }
    }

    public function slot(i:int, j:int):void {
        mirror(i, j, magenta);
    }

    public function click(x:Number, y:Number):void {
        const i:int = x / cellSize;
        const j:int = y / cellSize;
        mirror(i, j, newBitmapData(getGround(i, j).bitmapData))
    }

    private static function newBitmapData(bitmapData:BitmapData):BitmapData {
        if (bitmapData == yellow)
            return grass;
        else if (bitmapData == grass)
            return yellow;
        else
            return magenta;
    }

    private function mirror(i:int, j:int, bitmapData:BitmapData):void {
        getGround(i, j).bitmapData = bitmapData;
        getGround(h - 1 - i, j).bitmapData = bitmapData;
        getGround(i, v - 1 - j).bitmapData = bitmapData;
        getGround(h - 1 - i, v - 1 - j).bitmapData = bitmapData;
    }

    public function getMap():String {
        var s:String = "";
        for (var i:int = 0; i < h / 2; i++) {
            for (var j:int = 0; j < v / 2; j++) {
                if (getGround(i, j).bitmapData == yellow) {
                    s += i + "\t" + j + "\trandom\trandom\n";
                }
            }
        }
        return s;
    }
}
}
