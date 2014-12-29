package ru.rknrl.castles.utils.points {

import ru.rknrl.dto.PointDTO;

/**
 * todo equals with Points.scala
 */
public class Points {
    private var points:Vector.<Point>;
    private var distances:Vector.<Number>;

    public function get totalDistance():Number {
        return distances[distances.length - 1];
    }

    public function Points(points:Vector.<Point>) {
        if (points.length < 2) throw new Error("points.length < 2");
        this.points = points;
        distances = getDistances(points);
    }

    public static function getDistances(points:Vector.<Point>):Vector.<Number> {
        const distances:Vector.<Number> = new Vector.<Number>(points.length, true);
        var total:Number = 0;
        for (var i:int = 0; i < points.length - 1; i++) {
            distances[i] = total;
            total += points[i].distance(points[i + 1]);
        }
        distances[points.length - 1] = total;
        return distances;
    }

    public static function getIndex(d:Number, distances:Vector.<Number>):int {
        if (d < 0) throw new Error("d < 0");
        var i:int = 1;
        while (i < distances.length - 1 && distances[i] < d) i++;
        return i - 1;
    }

    public function getPos(distance:Number):Point {
        const index:int = getIndex(distance, distances);
        const p1:Point = points[index];
        const p2:Point = points[index + 1];
        const progress:Number = (distance - distances[index]) / (distances[index + 1] - distances[index]);
        return p1.lerp(p2, progress)
    }

    public static function fromDto(points:Vector.<PointDTO>):Points {
        const result:Vector.<Point> = new Vector.<Point>(points.length, true);
        for (var i:int = 0; i < points.length; i++) {
            result[i] = Point.fromDto(points[i]);
        }
        return new Points(result);
    }
}
}

