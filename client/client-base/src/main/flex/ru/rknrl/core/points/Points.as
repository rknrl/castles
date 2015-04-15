//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.points {

import ru.rknrl.dto.PointDTO;

public class Points {
    private var points:Vector.<Point>;
    private var distances:Vector.<Number>;

    public function Points(points:Vector.<Point>) {
        if (points.length < 2) throw new Error("points.length < 2");
        this.points = points;
        distances = getDistances();
        _totalDistance = distances[distances.length - 1];
    }

    private function getDistances():Vector.<Number> {
        var d:Number = 0;
        const ds:Vector.<Number> = new <Number>[0];
        for (var i:int = 1; i < points.length; i++) {
            d += points[i - 1].distance(points[i]);
            ds.push(d);
        }
        return ds
    }

    private var _totalDistance:Number;

    public function get totalDistance():Number {
        return _totalDistance;
    }

    private function getIndex(distance:Number):int {
        var i:int = 0;
        while (i < points.length - 1 && distances[i + 1] < distance) i++;
        return i;
    }

    public function pos(progress:Number):Point {
        const x:Number = Math.max(0, Math.min(1, progress));
        const distance:Number = _totalDistance * x;
        const i1:int = getIndex(distance);
        const i2:int = i1 + 1;
        const p1:Point = points[i1];
        const p2:Point = points[i2];
        if (p1.equals(p2))
            return p1;
        else {
            const lerp:Number = (distance - distances[i1]) / p1.distance(p2);
            return p1.lerp(p2, lerp)
        }
    }

    public function dto():Vector.<PointDTO> {
        return pointsToDto(points);
    }

    public static function pointsToDto(points:Vector.<Point>):Vector.<PointDTO> {
        const result:Vector.<PointDTO> = new Vector.<PointDTO>(points.length, true);
        for (var i:int = 0; i < points.length; i++) {
            result[i] = points[i].dto();
        }
        return result;
    }

    public static function fromDto(points:Vector.<PointDTO>):Points {
        const result:Vector.<Point> = new Vector.<Point>(points.length, true);
        for (var i:int = 0; i < points.length; i++) {
            result[i] = Point.fromDto(points[i]);
        }
        return new Points(result);
    }

    public static function two(a:Point, b:Point):Points {
        return new Points(new <Point>[a, b]);
    }
}
}

