//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.main {
import org.flexunit.asserts.assertEquals;

import protos.BuildingLevel;
import protos.BuildingPrice;

import ru.rknrl.castles.model.DtoMock;

public class BuildingPricesTest {
    [Test("prices")]
    public function t0():void {
        const prices:BuildingPrices = new BuildingPrices(new <BuildingPrice>[
            DtoMock.buildingPrice(BuildingLevel.LEVEL_1, 10),
            DtoMock.buildingPrice(BuildingLevel.LEVEL_2, 20),
            DtoMock.buildingPrice(BuildingLevel.LEVEL_3, 30)
        ]);

        assertEquals(10, prices.buildPrice);
        assertEquals(20, prices.getPrice(BuildingLevel.LEVEL_2));
        assertEquals(30, prices.getPrice(BuildingLevel.LEVEL_3));
    }

    [Test("get invalid prices", expects="Error")]
    public function t1():void {
        new BuildingPrices(new <BuildingPrice>[]).buildPrice
    }
}
}
