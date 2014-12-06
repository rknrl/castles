package ru.rknrl.castles.menu.screens.main {
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingPriceDTO;

public class BuildingPrices {
    private var prices:Vector.<BuildingPriceDTO>;

    function BuildingPrices(prices:Vector.<BuildingPriceDTO>) {
        this.prices = prices;
    }

    public function getBuildPrice():int {
        return getPrice(BuildingLevel.LEVEL_1);
    }

    public function getPrice(level:BuildingLevel):int {
        for each(var price:BuildingPriceDTO in prices) {
            if (price.level == level) return price.price;
        }
        throw new Error("can't find building price for level " + level)
    }
}
}