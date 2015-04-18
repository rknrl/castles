//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.shop {
import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.dto.ItemDTO;
import ru.rknrl.dto.ItemType;

public class ItemsCountTest {
    private const itemsCount:ItemsCount = new ItemsCount(new <ItemDTO>[
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
