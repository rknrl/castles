//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.points {
import ru.rknrl.dto.PointDTO;

/**
 * todo equals with Point.scala
 */
public class Point {
    private var _x:Number;

    public function get x():Number {
        return _x;
    }

    private var _y:Number;

    public function get y():Number {
        return _y;
    }

    public function Point(x:Number, y:Number) {
        _x = x;
        _y = y;
    }

    public function distance(endPos:Point):Number {
        const dx:Number = endPos.x - x;
        const dy:Number = endPos.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public function duration(endPos:Point, speed:Number):Number {
        return distance(endPos) / speed;
    }
    
    public function lerp(endPos:Point, progress:Number):Point {
        return new Point(x + (endPos.x - x) * progress, y + (endPos.y - y) * progress);
    }

    public function equals(point:Point):Boolean {
        return x == point.x && y == point.y;
    }

    public function dto():PointDTO {
        const dto:PointDTO = new PointDTO();
        dto.x = x;
        dto.y = y;
        return dto;
    }

    public static function fromDto(dto:PointDTO):Point {
        return new Point(dto.x, dto.y);
    }

    public static function pointsToDto(points:Vector.<Point>):Vector.<PointDTO> {
        const result:Vector.<PointDTO> = new Vector.<PointDTO>(points.length, true);
        for (var i:int = 0; i < points.length; i++) {
            result[i] = points[i].dto();
        }
        return result;
    }
}
}
