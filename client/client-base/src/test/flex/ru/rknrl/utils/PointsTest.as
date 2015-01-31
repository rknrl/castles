package ru.rknrl.utils {
import flash.display.Sprite;

import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;

public class PointsTest extends Sprite {

    [Test(expects="Error")]
    public function emptyPoints():void {
        new Points(new <Point>[])
    }

    [Test(expects="Error")]
    public function onePoint():void {
        new Points(new <Point>[new Point(0, 0)])
    }

    [Test]
    public function distances2():void {
        const distances:Vector.<Number> = Points.getDistances(new <Point>[new Point(1, 0), new Point(3, 4)]);

        assertEquals(2, distances.length);
        assertEquals(0, distances[0]);
        assertEquals(Math.sqrt(2 * 2 + 4 * 4), distances[1]);
    }

    [Test]
    public function distances3():void {
        const distances:Vector.<Number> = Points.getDistances(new <Point>[new Point(1, 0), new Point(3, 4), new Point(7, 6)]);

        assertEquals(3, distances.length);
        assertEquals(0, distances[0]);
        assertEquals(Math.sqrt(2 * 2 + 4 * 4), distances[1]);
        assertEquals(Math.sqrt(2 * 2 + 4 * 4) + Math.sqrt(4 * 4 + 2 * 2), distances[2]);
    }

    [Test(expects="Error")]
    public function negative():void {
        const distances:Vector.<Number> = new <Number>[0, 1];
        Points.getIndex(-0.1, distances);
    }

    [Test]
    public function index2():void {
        const distances:Vector.<Number> = new <Number>[0, 1];

        assertEquals(0, Points.getIndex(0, distances));
        assertEquals(0, Points.getIndex(1, distances));
        assertEquals(0, Points.getIndex(999, distances));
    }

    [Test]
    public function index3():void {
        const distances:Vector.<Number> = new <Number>[0, 1, 2];

        assertEquals(0, Points.getIndex(0, distances));
        assertEquals(0, Points.getIndex(1, distances));
        assertEquals(1, Points.getIndex(2, distances));
        assertEquals(1, Points.getIndex(2.1, distances));
        assertEquals(1, Points.getIndex(999, distances));
    }

    [Test]
    public function index5():void {
        const distances:Vector.<Number> = new <Number>[0, 1, 2, 4, 5];

        assertEquals(0, Points.getIndex(0, distances));
        assertEquals(0, Points.getIndex(1, distances));
        assertEquals(1, Points.getIndex(2, distances));
        assertEquals(2, Points.getIndex(2.1, distances));
        assertEquals(2, Points.getIndex(4, distances));
        assertEquals(3, Points.getIndex(4.1, distances));
        assertEquals(3, Points.getIndex(5, distances));
        assertEquals(3, Points.getIndex(999, distances));
    }

    [Test]
    public function zeroDIstance():void {
        const distances:Vector.<Number> = new <Number>[0, 1, 1, 1, 2, 2];

        assertEquals(0, Points.getIndex(0, distances));
        assertEquals(0, Points.getIndex(1, distances));
        assertEquals(3, Points.getIndex(2, distances));
        assertEquals(4, Points.getIndex(2.1, distances));
        assertEquals(4, Points.getIndex(999, distances));
    }

    [Test]
    public function pos2():void {
        const points:Points = new Points(new <Point>[new Point(0, 0), new Point(1, 1)]);

        const p1:Point = points.getPos(0);
        assertEquals(0, p1.x);
        assertEquals(0, p1.y);

        const p2:Point = points.getPos(Math.sqrt(1 + 1) / 2);
        assertEquals(0.5, p2.x);
        assertEquals(0.5, p2.y);

        const p3:Point = points.getPos(Math.sqrt(1 + 1));
        assertEquals(1, p3.x);
        assertEquals(1, p3.y);
    }

    [Test]
    public function pos3():void {
        const points:Points = new Points(new <Point>[new Point(0, 0), new Point(0, 1), new Point(1, 1)]);

        const p1:Point = points.getPos(0);
        assertEquals(0, p1.x);
        assertEquals(0, p1.y);

        const p2:Point = points.getPos(0.5);
        assertEquals(0, p2.x);
        assertEquals(0.5, p2.y);

        const p3:Point = points.getPos(1);
        assertEquals(0, p3.x);
        assertEquals(1, p3.y);

        const p4:Point = points.getPos(1.5);
        assertEquals(0.5, p4.x);
        assertEquals(1, p4.y);

        const p5:Point = points.getPos(2);
        assertEquals(1, p5.x);
        assertEquals(1, p5.y);

        const p6:Point = points.getPos(999);
        assertEquals(1, p6.x);
        assertEquals(1, p6.y);
    }
}
}