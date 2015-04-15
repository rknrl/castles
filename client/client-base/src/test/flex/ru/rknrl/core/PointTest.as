//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.core {

import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertFalse;
import org.flexunit.asserts.assertTrue;

import ru.rknrl.core.points.Point;
import ru.rknrl.dto.PointDTO;

public class PointTest {
    [Test("equals")]
    public function t1():void {
        assertTrue(new Point(1.1, 2.2).equals(new Point(1.1, 2.2)))
        assertFalse(new Point(0, 4).equals(new Point(1, 4)))
    }

    [Test("dto")]
    public function t2():void {
        const dto:PointDTO = new Point(1.1, 2.2).dto();
        assertEquals(dto.x, 1.1);
        assertEquals(dto.y, 2.2);
    }

    [Test("from dto")]
    public function t3():void {
        const dto:PointDTO = new PointDTO();
        dto.x = 1.1;
        dto.y = 2.2;
        const point:Point = Point.fromDto(dto);
        assertTrue(point.equals(new Point(1.1, 2.2)))
    }

    [Test("distance")]
    public function t4():void {
        assertEquals(5, new Point(6, 1).distance(new Point(3, 5)))
    }

    [Test("lerp")]
    public function t5():void {
        const p1:Point = new Point(1, 1);
        const p2:Point = new Point(3, 2);
        assertTrue(p1.lerp(p2, -1).equals(p1));
        assertTrue(p1.lerp(p2, 0).equals(p1));
        assertTrue(p1.lerp(p2, 0.5).equals(new Point(2, 1.5)));
        assertTrue(p1.lerp(p2, 1).equals(p2));
        assertTrue(p1.lerp(p2, 2).equals(p2));
    }
}
}
