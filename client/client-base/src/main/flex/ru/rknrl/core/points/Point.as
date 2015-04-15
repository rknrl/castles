//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.points {
import ru.rknrl.dto.PointDTO;

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

    public function distance(that:Point):Number {
        const dx:Number = that.x - this.x;
        const dy:Number = that.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public function lerp(that:Point, progress:Number):Point {
        const p:Number = Math.max(0, Math.min(1, progress));
        return new Point(this.x + (that.x - this.x) * p, this.y + (that.y - this.y) * p);
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

    public function toString():String {
        return "{" + x + "," + y + "}";
    }

    public static function fromDto(dto:PointDTO):Point {
        return new Point(dto.x, dto.y);
    }
}
}
