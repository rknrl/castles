package ru.rknrl.castles.view.menu {
import flash.display.Sprite;

import ru.rknrl.castles.model.events.ScreenChangedEvent;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.model.menu.bank.Products;
import ru.rknrl.castles.model.menu.main.Slots;
import ru.rknrl.castles.model.menu.shop.ItemsCount;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.bank.BankScreen;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.menu.main.MainScreen;
import ru.rknrl.castles.view.menu.main.popups.BuildPopup;
import ru.rknrl.castles.view.menu.main.popups.UpgradePopup;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.menu.navigate.navigator.ScreenNavigator;
import ru.rknrl.castles.view.menu.shop.ShopScreen;
import ru.rknrl.castles.view.menu.skills.SkillsScreen;
import ru.rknrl.castles.view.menu.top.TopScreen;
import ru.rknrl.castles.view.popups.PopupEvent;
import ru.rknrl.castles.view.popups.PopupManager;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SlotId;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.test;

public class MenuView extends Sprite {
    private var locale:CastlesLocale;

    private var mainScreen:MainScreen;
    private var topScreen:TopScreen;
    private var shopScreen:ShopScreen;
    private var skillScreen:SkillsScreen;
    private var bankScreen:BankScreen;
    private var screenNavigator:ScreenNavigator;
    private var popupManager:PopupManager;
    private var tutor:MenuTutorView;

    public function MenuView(layout:Layout,
                             locale:CastlesLocale,
                             loadImageManager:ILoadImageManager,
                             model:MenuModel,
                             deviceFactory:DeviceFactory) {

        _layout = layout;
        this.locale = locale;

        mainScreen = new MainScreen(model.slots, layout, locale);
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
        addChild(screenNavigator = deviceFactory.screenNavigator(screens, model.gold, layout, locale));
        addChild(popupManager = new PopupManager(layout));
        addChild(tutor = new MenuTutorView(layout, deviceFactory));

        addEventListener(PopupEvent.CLOSE, popupManager.close);
    }

    override public function set visible(value:Boolean):void {
        super.visible = value;
        if (value) dispatchEvent(new ScreenChangedEvent(ScreenChangedEvent.SCREEN_MAIN)); // return to menu after game
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;
        screenNavigator.layout = value;
        popupManager.layout = value;
        tutor.layout = value;
    }

    public function set lock(value:Boolean):void {
        screenNavigator.lock = value;
    }

    public function set gold(value:int):void {
        screenNavigator.gold = value;
    }

    public function animatePrice():void {
        screenNavigator.animatePrice();
        popupManager.animatePrice();
    }

    public function animateMagicItem(itemType:ItemType):void {
        shopScreen.animate(itemType);
    }

    public function animateFlask(skillType:SkillType):void {
        skillScreen.animate(skillType);
    }

    public function set slots(value:Slots):void {
        mainScreen.slots = value;
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

    public function openUpgradePopup(slotId:SlotId, buildingType:BuildingType, canUpgrade:Boolean, canRemove:Boolean, upgradePrice:int):void {
        popupManager.open(new UpgradePopup(slotId, buildingType, canUpgrade, canRemove, upgradePrice, _layout, locale));
    }

    public function closePopup():void {
        popupManager.close();
    }

    test function setScreen(index:int):void {
        screenNavigator.currentScreenIndex = index;
    }

    test function closePopupImmediate():void {
        use namespace test;

        popupManager.closeImmediate();
    }

    test function openPopupImmediate():void {
        use namespace test;

        popupManager.openImmediate();
    }

    // tutor

    public function playSwipeTutor():void {
        tutor.playSwipe();
    }

    public function playSlotTutor(slotId:SlotId):void {
        tutor.playSlot(slotId);
    }

    public function playMagicItemTutor():void {
        tutor.playMagicItem();
    }

    public function playFlaskTutor():void {
        tutor.playFlask();
    }

    public function get tutorPlaying():Boolean {
        return tutor.playing;
    }
}
}
