//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu {
import protos.AccountConfig;
import protos.AccountState;
import protos.Authenticated;
import protos.Place;
import protos.Product;
import protos.Top;

import ru.rknrl.castles.model.menu.bank.Products;
import ru.rknrl.castles.model.menu.main.BuildingPrices;
import ru.rknrl.castles.model.menu.main.Slots;
import ru.rknrl.castles.model.menu.shop.ItemsCount;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.model.menu.top.TopUtils;

public class MenuModel {
    private var _slots:Slots;

    public function get slots():Slots {
        return _slots;
    }

    private var _gold:int;

    public function get gold():int {
        return _gold;
    }

    private var _top:Top;

    public function get top():Top {
        return _top;
    }

    private var _place:Place;

    public function get place():Place {
        return _place;
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

    private var _gamesCount:int;

    public function get gamesCount():int {
        return _gamesCount;
    }

    private var _lastGamesCountAdvert:int;

    public function get lastGamesCountAdvert():int {
        return _lastGamesCountAdvert;
    }

    private var _advertGamesInterval:int;

    public function get advertGamesInterval():int {
        return _advertGamesInterval;
    }

    public function get canShowAdvert():Boolean {
        return gamesCount - lastGamesCountAdvert >= advertGamesInterval;
    }

    public function MenuModel(authenticated:Authenticated) {
        mergeAccountStateDto(authenticated.accountState);
        mergeConfigDto(authenticated.config);
        mergeProductsDto(authenticated.products);
        mergeTopDto(authenticated.top);
        mergePlaceDto(authenticated.place);
    }

    public function mergeAccountStateDto(dto:AccountState):void {
        _slots = new Slots(dto.slots);
        _gold = dto.gold;
        _itemsCount = new ItemsCount(dto.items);
        _skillLevels = new SkillLevels(dto.skills);
        _gamesCount = dto.gamesCount;
        _lastGamesCountAdvert = dto.lastGamesCountAdvert;
    }

    public function mergeTopDto(dto:Top):void {
        _top = dto;
    }

    public function mergePlaceDto(dto:Place):void {
        _place = dto;
    }

    public function mergeConfigDto(dto:AccountConfig):void {
        _itemPrice = dto.itemPrice;
        _upgradePrices = new SkillUpgradePrices(dto.skillUpgradePrices);
        _buildingPrices = new BuildingPrices(dto.buildings);
        _advertGamesInterval = dto.advertGamesInterval;
    }

    public function mergeProductsDto(dto:Vector.<Product>):void {
        _products = new Products(dto);
    }
}
}
