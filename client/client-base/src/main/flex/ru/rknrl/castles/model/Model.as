package ru.rknrl.castles.model {
import ru.rknrl.castles.model.menu.bank.Products;
import ru.rknrl.castles.model.menu.main.BuildingPrices;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.castles.model.menu.shop.ItemsCount;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.model.menu.top.Top;
import ru.rknrl.dto.AccountConfigDTO;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.AuthenticationSuccessDTO;
import ru.rknrl.dto.ProductDTO;

public class Model {
    private var _startLocation:StartLocation;

    public function get startLocation():StartLocation {
        return _startLocation;
    }

    private var _gold:int;

    public function get gold():int {
        return _gold;
    }

    private var _top:Top;

    public function get top():Top {
        return _top;
    }

    private var _itemsCount:ItemsCount;

    public function get itemsCount():ItemsCount {
        return _itemsCount;
    }

    private var _itemPrice:int;

    public function get itemPrice():int {
        return _itemPrice;
    }

    private var _skillLevels:SkillLevels;

    public function get skillLevels():SkillLevels {
        return _skillLevels;
    }

    private var _upgradePrices:SkillUpgradePrices;

    public function get upgradePrices():SkillUpgradePrices {
        return _upgradePrices;
    }

    private var _buildingPrices:BuildingPrices;

    public function get buildingPrices():BuildingPrices {
        return _buildingPrices;
    }

    private var _products:Products;

    public function get products():Products {
        return _products;
    }

    public function Model(authenticationSuccess:AuthenticationSuccessDTO) {
        accountStateDto = authenticationSuccess.accountState;
        configDto = authenticationSuccess.config;
        productsDto = authenticationSuccess.products;
    }

    public function set accountStateDto(dto:AccountStateDTO):void {
        _startLocation = new StartLocation(dto.startLocation);
        _gold = dto.gold;
        _itemsCount = new ItemsCount(dto.items);
        _skillLevels = new SkillLevels(dto.skills);
    }

    public function set configDto(dto:AccountConfigDTO):void {
        _top = new Top(dto.top);
        _itemPrice = dto.itemPrice;
        _upgradePrices = new SkillUpgradePrices(dto.skillUpgradePrices);
        _buildingPrices = new BuildingPrices(dto.buildings);
    }

    public function set productsDto(dto:Vector.<ProductDTO>):void {
        _products = new Products(dto);
    }
}
}
