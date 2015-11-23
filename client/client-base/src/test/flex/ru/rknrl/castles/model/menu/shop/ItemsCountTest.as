//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.shop {
import org.flexunit.asserts.assertEquals;

import protos.Item;
import protos.ItemType;

import ru.rknrl.castles.model.DtoMock;

public class ItemsCountTest {
    private const itemsCount:ItemsCount = new ItemsCount(new <Item>[
        DtoMock.item(ItemType.ASSISTANCE, 7)
    ]);

    [Test("getCount")]
    public function t0():void {
        assertEquals(7, itemsCount.getCount(ItemType.ASSISTANCE))
    }

    [Test("getCount invaid", expects="Error")]
    public function t1():void {
        itemsCount.getCount(ItemType.FIREBALL);
    }
}
}
