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
    private var _points:Vector.<Point>;

    public function get points():Vector.<Point> {
        return _points;
    }

    private var _distances:Vector.<Number>;

    public function get distances():Vector.<Number> {
        return _distances;
    }

    public function Points(points:Vector.<Point>) {
        if (points.length < 2) throw new Error("_points.length < 2");
        _points = points;
        _distances = getDistances();
        _totalDistance = _distances[_distances.length - 1];
    }

    private function getDistances():Vector.<Number> {
        var d:Number = 0;
        const ds:Vector.<Number> = new <Number>[0];
        for (var i:int = 1; i < _points.length; i++) {
            d += _points[i - 1].distance(_points[i]);
            ds.push(d);
        }
        return ds
    }

    private var _totalDistance:Number;

    public function get totalDistance():Number {
        return _totalDistance;
    }

    public function getIndex(distance:Number):int {
        var i:int = 0;
        while (i < _points.length - 1 && _distances[i + 1] < distance) i++;
        return i;
    }

    public function pos(progress:Number):Point {
        const x:Number = Math.max(0, Math.min(1, progress));
        const distance:Number = _totalDistance * x;
        const i1:int = getIndex(distance);
        const i2:int = i1 + 1;
        const p1:Point = _points[i1];
        const p2:Point = _points[i2];
        if (p1.equals(p2))
            return p1;
        else {
            const lerp:Number = (distance - _distances[i1]) / p1.distance(p2);
            return p1.lerp(p2, lerp)
        }
    }

    public function dto():Vector.<PointDTO> {
        return pointsToDto(_points);
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

