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

public class PeriodicTest {
    [Test("non positive duration", expects="Error")]
    public function t1():void {
        new Periodic(1, 0)
    }

    [Test("non positive duration", expects="Error")]
    public function t2():void {
        new Periodic(1, -1)
    }

    [Test("millisFromStart")]
    public function t3():void {
        const p:Periodic = new Periodic(1, 10);
        assertEquals(0, p.millisFromStart(1));
        assertEquals(2, p.millisFromStart(3));
    }

    [Test("millisTillEnd")]
    public function t4():void {
        const p:Periodic = new Periodic(1, 10);
        assertEquals(10, p.millisTillEnd(1));
        assertEquals(8, p.millisTillEnd(3));
    }

    [Test("isFinish")]
    public function t5():void {
        const p:Periodic = new Periodic(1, 10);
        assertFalse(p.isFinish(10));
        assertTrue(p.isFinish(11));
        assertTrue(p.isFinish(12));
    }

    [Test("progress")]
    public function t6():void {
        const p:Periodic = new Periodic(1, 10);
        assertEquals(0, p.progress(1));
        assertEquals(0.5, p.progress(6));
        assertEquals(1, p.progress(11));
    }
}
}

