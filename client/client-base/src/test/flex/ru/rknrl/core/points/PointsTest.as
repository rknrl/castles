//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core.points {
import org.flexunit.asserts.assertEquals;

import protos.PointDTO;

import ru.rknrl.core.kit.assertVectors;
import ru.rknrl.core.kit.plusMinus;

public class PointsTest {
    [Test("no points", expects='Error')]
    public function t1():void {
        new Points(new <Point>[])
    }

    [Test("one point", expects='Error')]
    public function t2():void {
        new Points(new <Point>[new Point(0, 0)])
    }

    [Test("dto")]
    public function t3():void {
        const dto:Vector.<PointDTO> = Points.two(new Point(1.1, 2.2), new Point(3.3, 4.4)).dto();
        assertEquals(2, dto.length);
        assertEquals(1.1, dto[0].x);
        assertEquals(2.2, dto[0].y);
        assertEquals(3.3, dto[1].x);
        assertEquals(4.4, dto[1].y);
    }

    [Test("from dto")]
    public function t4():void {
        const p1:PointDTO = new PointDTO();
        p1.x = 1.1;
        p1.y = 2.2;
        const p2:PointDTO = new PointDTO();
        p2.x = 3.3;
        p2.y = 4.4;
        const dto:Vector.<PointDTO> = new <PointDTO>[p1, p2];

        const points:Points = Points.fromDto(dto);
        assertPoints(points.points[0], new Point(1.1, 2.2));
        assertPoints(points.points[1], new Point(3.3, 4.4));
    }

    private function testPoints():Points {
        return new Points(new <Point>[
            new Point(0, 0),
            new Point(3, 4), // distance 5
            new Point(5, 4) // distance 2
        ]);
    }

    [Test("distances")]
    public function t5():void {
        const points:Points = testPoints();
        assertVectors(Vector.<*>(points.distances), Vector.<*>(new <Number>[0, 5, 7]));
    }

    private static function assertPoints(a:Point, b:Point):void {
        plusMinus(a.x, b.x, 0.001);
        plusMinus(a.y, b.y, 0.001);
    }

    [Test("totalDistance")]
    public function t6():void {
        assertEquals(testPoints().totalDistance, 7)
    }

    [Test("getIndex")]
    public function t7():void {
        const points:Points = testPoints();
        assertEquals(0, points.getIndex(0));
        assertEquals(0, points.getIndex(2));
        assertEquals(0, points.getIndex(5));
        assertEquals(1, points.getIndex(6));
        assertEquals(1, points.getIndex(7));
    }

    [Test("pos")]
    public function t8():void {
        const points:Points = testPoints();
        assertPoints(new Point(0, 0), points.pos(-1));
        assertPoints(new Point(0, 0), points.pos(0));

        assertPoints(new Point(0, 0).lerp(new Point(3, 4), 3.5 / 5), points.pos(0.5));

        assertPoints(new Point(3, 4).lerp(new Point(5, 4), 0.6 / 2), points.pos(0.8));

        assertPoints(new Point(5, 4), points.pos(1));
        assertPoints(new Point(5, 4), points.pos(2));
    }

    [Test("same points")]
    public function t9():void {
        const points:Points = Points.two(new Point(0, 0), new Point(0, 0));
        assertPoints(points.pos(0.5), new Point(0, 0))
    }
}
}
