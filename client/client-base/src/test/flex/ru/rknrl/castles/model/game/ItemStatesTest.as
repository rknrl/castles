//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import org.flexunit.asserts.assertEquals;

import protos.ItemStateDTO;
import protos.ItemStatesDTO;
import protos.ItemType;

import ru.rknrl.castles.model.DtoMock;

public class ItemStatesTest {
    private const itemStates:ItemStatesDTO =
            DtoMock.itemStates(new <ItemStateDTO>[
                DtoMock.itemState(ItemType.FIREBALL, 11, 10, 1),
                DtoMock.itemState(ItemType.VOLCANO, 7, 10, 2),
                DtoMock.itemState(ItemType.TORNADO, 0, 10, 3)
            ]);

    [Test("get")]
    public function t0():void {
        const items:ItemStates = new ItemStates(itemStates, 5);
        assertEquals(11, items.get(ItemType.FIREBALL).count);
        assertEquals(2, items.get(ItemType.TORNADO).cooldown.startTime);
    }

    [Test("get unknown item", expects="Error")]
    public function t1():void {
        const items:ItemStates = new ItemStates(itemStates, 0);
        items.get(ItemType.ASSISTANCE);
    }
}
}
