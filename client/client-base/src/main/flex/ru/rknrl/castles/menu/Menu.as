package ru.rknrl.castles.menu {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;

import ru.rknrl.castles.game.Game;
import ru.rknrl.castles.menu.screens.LoadingScreen;
import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.menu.screens.bank.BankScreen;
import ru.rknrl.castles.menu.screens.main.BuildingPrices;
import ru.rknrl.castles.menu.screens.main.MainScreen;
import ru.rknrl.castles.menu.screens.shop.ItemsCount;
import ru.rknrl.castles.menu.screens.shop.ShopScreen;
import ru.rknrl.castles.menu.screens.skills.SkillLevels;
import ru.rknrl.castles.menu.screens.skills.SkillsScreen;
import ru.rknrl.castles.menu.slider.ScreenSlider;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.core.rmi.Connection;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.ItemsDTO;
import ru.rknrl.dto.NodeLocator;
import ru.rknrl.dto.PricesDTO;
import ru.rknrl.dto.SkillsDTO;
import ru.rknrl.dto.StartLocationDTO;
import ru.rknrl.jnb.rmi.AccountFacadeSender;
import ru.rknrl.jnb.rmi.EnterGameFacadeReceiver;
import ru.rknrl.jnb.rmi.EnterGameFacadeSender;
import ru.rknrl.jnb.rmi.GameFacadeReceiver;
import ru.rknrl.jnb.rmi.GameFacadeSender;
import ru.rknrl.jnb.rmi.IAccountFacade;
import ru.rknrl.jnb.rmi.IEnterGameFacade;
import ru.rknrl.log.Log;

public class Menu extends Sprite implements IAccountFacade, IEnterGameFacade {
    private var connection:Connection;
    private var sender:AccountFacadeSender;
    private var log:Log;
    private var layout:Layout;
    private var locale:CastlesLocale;

    private var mainScreen:MainScreen;
    private var skillsScreen:SkillsScreen;
    private var bankScreen:BankScreen;
    private var shopScreen:ShopScreen;
    private var screens:Vector.<MenuScreen>;

    private var popups:PopupManager;
    private var header:Header;

    private var screenSlider:ScreenSlider;

    private var enterGameScreen:LoadingScreen;

    private var game:Game;
    private var gameConnection:Connection;
    private var gameFacadeReceiver:GameFacadeReceiver;

    public function Menu(accountState:AccountStateDTO, connection:Connection, sender:AccountFacadeSender, log:Log, layout:Layout, social:Social, locale:CastlesLocale) {
        this.connection = connection;
        this.sender = sender;
        this.log = log;
        this.locale = locale;
        this.layout = layout;

        popups = new PopupManager(layout);

        mainScreen = new MainScreen(Utils.SCREEN_CASTLE, accountState.startLocation, new BuildingPrices(accountState.prices.buildings), sender, layout, locale, popups);

        skillsScreen = new SkillsScreen(Utils.SCREEN_SKILLS, new SkillLevels(accountState.skills), accountState.prices.skillsUpgradePrice, sender, layout, locale);

        bankScreen = new BankScreen(Utils.SCREEN_BANK, accountState.prices.goldByDollar, sender, layout, social, locale);

        shopScreen = new ShopScreen(Utils.SCREEN_SHOP, new ItemsCount(accountState.items), accountState.prices.itemPrice, sender, layout, locale);

        screens = new <MenuScreen>[mainScreen, skillsScreen, bankScreen, shopScreen];

        for each(var screen:Screen in screens) {
            screen.addEventListener(Utils.NOT_ENOUGH_GOLD, onNotEnoughGold);
        }

        header = new Header(accountState.gold, layout);

        screenSlider = layout.createSlider(screens, locale);

        addChild(screenSlider);
        addChild(popups);
        addChild(header);

        screenSlider.visible = !accountState.game;
        header.visible = !accountState.game;

        gold = accountState.gold;

        screenSlider.addEventListener(Utils.PLAY, onPlayButtonClick);

        if (accountState.game) {
            onEnteredGame(accountState.game);
        }
    }

    public function updateLayout(layout:Layout):void {
        this.layout = layout;
        mainScreen.updateLayout(layout);
        skillsScreen.updateLayout(layout);
        bankScreen.updateLayout(layout);
        shopScreen.updateLayout(layout);
        header.updateLayout(layout);
        popups.updateLayout(layout);
        layout.updateSlider(screenSlider);
        if (enterGameScreen) enterGameScreen.updateLayout(layout, layout.enterGameTextFormat);
        if (game) game.updateLayout(layout);
    }

    public function removeListeners():void {
        mainScreen.removeListeners();
        if (game) game.removeListeners();
    }

    private var _gold:int;

    public function set gold(value:int):void {
        _gold = value;
        header.gold = _gold;
        for each(var screen:Screen in screens) {
            screen.gold = _gold;
        }
    }

    private function onPlayButtonClick(event:Event = null):void {
        header.visible = false;
        screenSlider.visible = false;

        addChild(enterGameScreen = new LoadingScreen(locale.enterGame, layout.enterGameTextFormat, layout));

        sender.enterGame();
    }

    // ENTER GAME

    public function onEnteredGame(nodeLocator:NodeLocator):void {
        log.add("onEnteredGame");

        if (connection.host == nodeLocator.host && connection.port == nodeLocator.port) {
            gameConnection = connection;
            onGameConnect()
        } else {
            gameConnection = new Connection();
            gameConnection.addEventListener(Event.CONNECT, onGameConnect);
            gameConnection.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onGameConnectionError);
            gameConnection.addEventListener(IOErrorEvent.IO_ERROR, onGameConnectionError);
            gameConnection.addEventListener(Event.CLOSE, onGameConnectionError);
            gameConnection.connect(nodeLocator.host, nodeLocator.port);
        }
    }

    private var enterGameFacadeSender:EnterGameFacadeSender;
    private var enterGameFacadeReceiver:EnterGameFacadeReceiver;

    private function onGameConnect(event:Event = null):void {
        log.add("onGameConnect");

        enterGameFacadeSender = new EnterGameFacadeSender(gameConnection);
        enterGameFacadeReceiver = new EnterGameFacadeReceiver(this);
        gameConnection.registerReceiver(enterGameFacadeReceiver);

        enterGameFacadeSender.join();
    }

    public function onJoinGame(gameState:GameStateDTO):void {
        log.add("onJoinGame");

        if (enterGameScreen) { // Его не будет если мы получили игру на старте
            removeChild(enterGameScreen);
            enterGameScreen = null;
        }

        game = new Game(new GameFacadeSender(connection), gameState, layout, locale);
        addChild(game);
        gameFacadeReceiver = new GameFacadeReceiver(game);
        gameConnection.registerReceiver(gameFacadeReceiver);
    }

    public function onLeaveGame():void {
        log.add("onLeaveGame");

        const wantPlayAgain:Boolean = game.wantPlayAgain;

        game.removeListeners();
        removeChild(game);
        game = null;

        screenSlider.visible = true;
        header.visible = true;

        gameConnection.unregisterReceiver(gameFacadeReceiver);
        gameConnection.unregisterReceiver(enterGameFacadeReceiver);

        gameConnection.removeEventListener(Event.CONNECT, onGameConnect);
        gameConnection.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onGameConnectionError);
        gameConnection.removeEventListener(IOErrorEvent.IO_ERROR, onGameConnectionError);
        gameConnection.removeEventListener(Event.CLOSE, onGameConnectionError);

        if (gameConnection.host != connection.host || gameConnection.port != connection.port) {
            gameConnection.close();
        }

        gameConnection = null;

        if (wantPlayAgain) onPlayButtonClick()
    }

    private function onGameConnectionError(event:Event):void {
        throw new Error(event.toString);
    }

    public function onGoldUpdated(value:int):void {
        gold = value;
    }

    public function onPricesUpdated(pricesDTO:PricesDTO):void {
        mainScreen.buildingsPrices = new BuildingPrices(pricesDTO.buildings);
        skillsScreen.skillsUpgradePrice = pricesDTO.skillsUpgradePrice;
        bankScreen.goldByDollar = pricesDTO.goldByDollar;
        shopScreen.itemPrice = pricesDTO.itemPrice;
    }

    public function onStartLocationUpdated(dto:StartLocationDTO):void {
        mainScreen.startLocation = dto;
    }

    public function onSkillsUpdated(skills:SkillsDTO):void {
        skillsScreen.skillLevels = new SkillLevels(skills);
    }

    public function onItemsUpdated(items:ItemsDTO):void {
        shopScreen.itemsCount = new ItemsCount(items);
    }

    private function onNotEnoughGold(event:Event):void {
        header.animateGold();
    }
}
}