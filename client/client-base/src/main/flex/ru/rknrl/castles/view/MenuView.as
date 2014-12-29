package ru.rknrl.castles.view {
import flash.display.Sprite;

import ru.rknrl.castles.model.events.BuildEvent;
import ru.rknrl.castles.model.events.RemoveBuildingEvent;
import ru.rknrl.castles.model.events.UpgradeBuildingEvent;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.model.menu.bank.Products;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.castles.model.menu.shop.ItemsCount;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.bank.BankScreen;
import ru.rknrl.castles.view.menu.main.MainScreen;
import ru.rknrl.castles.view.menu.main.popups.BuildPopup;
import ru.rknrl.castles.view.menu.main.popups.UpgradePopup;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.menu.navigate.navigator.ScreenNavigator;
import ru.rknrl.castles.view.menu.navigate.navigator.ScreenNavigatorMobile;
import ru.rknrl.castles.view.menu.shop.ShopScreen;
import ru.rknrl.castles.view.menu.skills.SkillsScreen;
import ru.rknrl.castles.view.menu.top.TopScreen;
import ru.rknrl.castles.view.popups.PopupEvent;
import ru.rknrl.castles.view.popups.PopupManager;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.dto.SlotId;

public class MenuView extends Sprite {
    private var locale:CastlesLocale;

    private var mainScreen:MainScreen;
    private var topScreen:TopScreen;
    private var shopScreen:ShopScreen;
    private var skillScreen:SkillsScreen;
    private var bankScreen:BankScreen;
    private var screenNavigator:ScreenNavigator;
    private var popupManager:PopupManager;

    public function MenuView(layout:Layout,
                             locale:CastlesLocale,
                             loadImageManager:LoadImageManager,
                             model:MenuModel) {

        _layout = layout;
        this.locale = locale;

        mainScreen = new MainScreen(model.startLocation, layout, locale);
        topScreen = new TopScreen(model.top, layout, locale, loadImageManager);
        shopScreen = new ShopScreen(model.itemsCount, model.itemPrice, layout, locale);
        skillScreen = new SkillsScreen(model.skillLevels, model.upgradePrices, layout, locale);
        bankScreen = new BankScreen(model.products, layout, locale);
        const screens:Vector.<Screen> = new <Screen>[
            mainScreen,
            topScreen,
            shopScreen,
            skillScreen,
            bankScreen
        ];
        addChild(screenNavigator = new ScreenNavigatorMobile(screens, model.gold, layout, locale));
        addChild(popupManager = new PopupManager(layout));

        addEventListener(PopupEvent.CLOSE, popupManager.close);
        addEventListener(BuildEvent.BUILD, popupManager.close);
        addEventListener(UpgradeBuildingEvent.UPGRADE_BUILDING, popupManager.close);
        addEventListener(RemoveBuildingEvent.REMOVE_BUILDING, popupManager.close);
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;
        screenNavigator.layout = value;
        popupManager.layout = value;
    }

    public function set lock(value:Boolean):void {
        screenNavigator.lock = value;
    }

    public function set gold(value:int):void {
        screenNavigator.gold = value;
    }

    public function set startLocation(value:StartLocation):void {
        mainScreen.startLocation = value;
    }

    public function set itemsCount(value:ItemsCount):void {
        shopScreen.itemsCount = value;
    }

    public function set itemPrice(value:int):void {
        shopScreen.itemPrice = value;
    }

    public function set skillLevels(value:SkillLevels):void {
        skillScreen.skillLevels = value;
    }

    public function set upgradePrices(value:SkillUpgradePrices):void {
        skillScreen.upgradePrices = value;
    }

    public function set products(value:Products):void {
        bankScreen.products = value;
    }

    public function openBuildPopup(slotId:SlotId, price:int):void {
        popupManager.open(new BuildPopup(slotId, price, _layout, locale));
    }

    public function openUpgradePopup(slotId:SlotId, canUpgrade:Boolean, canRemove:Boolean, upgradePrice:int):void {
        popupManager.open(new UpgradePopup(slotId, canUpgrade, canRemove, upgradePrice, _layout, locale));
    }
}
}
