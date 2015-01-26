package ru.rknrl.castles.model.menu.skills {
import ru.rknrl.dto.SkillUpgradePriceDTO;

public class SkillUpgradePrices {
    private var prices:Vector.<SkillUpgradePriceDTO>;

    function SkillUpgradePrices(prices:Vector.<SkillUpgradePriceDTO>) {
        this.prices = prices;
    }

    public function getPrice(totalLevel:int):int {
        for each(var price:SkillUpgradePriceDTO in prices) {
            if (price.totalLevel == totalLevel) return price.price;
        }
        throw new Error("can't find skill upgrade price for level " + totalLevel)
    }

    public function get firstUpgradePrice():int {
        return getPrice(1);
    }
}
}