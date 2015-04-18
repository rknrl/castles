//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.skills {
import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.dto.SkillUpgradePriceDTO;

public class SkillUpgradePricesTest {
    [Test("getPrice")]
    public function t0():void {
        const prices:SkillUpgradePrices = new SkillUpgradePrices(new <SkillUpgradePriceDTO>[
            DtoMock.skillUpgradePrice(1, 10),
            DtoMock.skillUpgradePrice(2, 20),
            DtoMock.skillUpgradePrice(3, 30)
        ]);
        assertEquals(10, prices.getPrice(1));
        assertEquals(20, prices.getPrice(2));
        assertEquals(30, prices.getPrice(3));
        assertEquals(10, prices.firstUpgradePrice);
    }

    [Test("getPrice invalid", expects="Error")]
    public function t1():void {
        const prices:SkillUpgradePrices = new SkillUpgradePrices(new <SkillUpgradePriceDTO>[]);
        prices.getPrice(1);
    }
}
}
