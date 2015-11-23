//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.skills {
import protos.SkillUpgradePrice;

public class SkillUpgradePrices {
    private var prices:Vector.<SkillUpgradePrice>;

    function SkillUpgradePrices(prices:Vector.<SkillUpgradePrice>) {
        this.prices = prices;
    }

    public function getPrice(totalLevel:int):int {
        for each(var price:SkillUpgradePrice in prices) {
            if (price.totalLevel == totalLevel) return price.price;
        }
        throw new Error("can't find skill upgrade price for level " + totalLevel)
    }

    public function get firstUpgradePrice():int {
        return getPrice(1);
    }
}
}