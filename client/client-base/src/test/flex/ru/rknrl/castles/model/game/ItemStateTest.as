//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertFalse;
import org.flexunit.asserts.assertTrue;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.dto.ItemStateDTO;
import ru.rknrl.dto.ItemType;

public class ItemStateTest {
    [Test("progress")]
    public function t0():void {
        const dto:ItemStateDTO = DtoMock.itemState(ItemType.FIREBALL, 33, 10, 1);
        const itemState:ItemState = new ItemState(dto, 5);
        assertEquals(33, itemState.count);
        assertEquals(10, itemState.cooldown.duration);
        assertEquals(4, itemState.cooldown.startTime);

        assertEquals(0.0, itemState.cooldown.progressInRange(0));
        assertEquals(0.1, itemState.cooldown.progressInRange(5));
        assertEquals(1.0, itemState.cooldown.progressInRange(999));
    }

    [Test("canUse if count==0")]
    public function t1():void {
        const dto:ItemStateDTO = DtoMock.itemState(ItemType.FIREBALL, 0, 10, 1);
        const itemState:ItemState = new ItemState(dto, 5);
        assertFalse(itemState.canUse(999));
    }

    [Test("canUse if cooldown")]
    public function t2():void {
        const dto:ItemStateDTO = DtoMock.itemState(ItemType.FIREBALL, 33, 10, 1);
        const itemState:ItemState = new ItemState(dto, 5);
        assertFalse(itemState.canUse(5));
        assertTrue(itemState.canUse(19));
    }
}
}
