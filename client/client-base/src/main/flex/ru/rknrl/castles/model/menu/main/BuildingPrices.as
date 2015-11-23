//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.main {
import protos.BuildingLevel;
import protos.BuildingPrice;

public class BuildingPrices {
    private var prices:Vector.<BuildingPrice>;

    function BuildingPrices(prices:Vector.<BuildingPrice>) {
        this.prices = prices;
    }

    public function get buildPrice():int {
        return getPrice(BuildingLevel.LEVEL_1);
    }

    public function getPrice(level:BuildingLevel):int {
        for each(var price:BuildingPrice in prices) {
            if (price.level == level) return price.price;
        }
        throw new Error("can't find building price for level " + level)
    }
}
}