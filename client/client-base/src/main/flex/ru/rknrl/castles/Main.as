package ru.rknrl.castles {
import flash.display.Sprite;
import flash.display.StageAlign;
import flash.display.StageQuality;
import flash.display.StageScaleMode;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.system.Security;

import ru.rknrl.castles.menu.Menu;
import ru.rknrl.castles.menu.screens.LoadingScreen;
import ru.rknrl.castles.menu.screens.noConnection.NoConnectionScreen;
import ru.rknrl.castles.rmi.AccountFacadeReceiver;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.rmi.AuthFacadeReceiver;
import ru.rknrl.castles.rmi.AuthFacadeSender;
import ru.rknrl.castles.rmi.IAuthFacade;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.core.rmi.Connection;
import ru.rknrl.core.social.Sex;
import ru.rknrl.core.social.Social;
import ru.rknrl.core.social.UserInfo;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.loaders.TextLoader;
import ru.rknrl.log.Log;

public class Main extends Sprite implements IAuthFacade {
    private static const defaultName:String = "Гость";

    private var host:String;
    private var gamePort:int;
    private var policyPort:int;

    private var authenticate:AuthenticateDTO;
    private var localesUrl:String;
    private var defaultLocale:String;
    private var log:Log;
    private var social:Social;

    private var myUserInfo:UserInfo;

    private var connection:Connection;
    private var authFacadeReceiver:AuthFacadeReceiver;
    private var authFacadeSender:AuthFacadeSender;
    private var accountFacadeReceiver:AccountFacadeReceiver;
    private var menu:Menu;
    private var noConnectionScreen:NoConnectionScreen;
    private var loadingScreen:LoadingScreen;

    private var localeLoader:TextLoader;
    private var locale:CastlesLocale;
    private var layout:Layout;

    public function Main(host:String, gamePort:int, policyPort:int, authenticate:AuthenticateDTO, localesUrl:String, defaultLocale:String, log:Log, social:Social, layout:Layout) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.authenticate = authenticate;
        this.localesUrl = localesUrl;
        this.defaultLocale = defaultLocale;
        this.log = log;
        this.social = social;
        this.layout = layout;
        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
    }

    private function onAddedToStage(event:Event):void {
        removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        stage.scaleMode = StageScaleMode.NO_SCALE;
        stage.align = StageAlign.TOP_LEFT;
        stage.quality = StageQuality.BEST;

        addLoadingScreen();

        localeLoader = new TextLoader(localesUrl + "castles - EN.tsv");
        localeLoader.addEventListener(Event.COMPLETE, onLocaleComplete);
        localeLoader.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onLocaleError);
        localeLoader.addEventListener(IOErrorEvent.IO_ERROR, onLocaleError);
        localeLoader.load();
    }

    private function onLocaleError(event:Event):void {
        log.add("locale error " + event);
        log.add("use default locale");

        setupLocale(defaultLocale);
    }

    private function onLocaleComplete(event:Event):void {
        setupLocale(localeLoader.text);
    }

    private function setupLocale(data:String):void {
        localeLoader.removeEventListener(Event.COMPLETE, onLocaleComplete);
        localeLoader.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onLocaleError);
        localeLoader.removeEventListener(IOErrorEvent.IO_ERROR, onLocaleError);
        locale = new CastlesLocale(data);

        social.getMyUserInfo(onGetMyUserInfo);
    }

    private function onGetMyUserInfo(userInfo:UserInfo):void {
        if (userInfo) {
            myUserInfo = userInfo;
            log.add("myUserInfo: " + myUserInfo)
        } else {
            myUserInfo = new UserInfo(authenticate.accountId.id, defaultName, "", Sex.UNDEFINED);
            log.add("myUserInfo fail");
        }

        tryConnect();
    }

    private function tryConnect():void {
        createConnection(host, gamePort)
    }

    private function createConnection(host:String, port:int):void {
        if (connection) throw new Error("already connected");

        Security.loadPolicyFile("xmlsocket://" + host + ":" + policyPort);

        connection = new Connection();
        connection.addEventListener(Event.CONNECT, onConnect);
        connection.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onConnectionError);
        connection.addEventListener(IOErrorEvent.IO_ERROR, onConnectionError);
        connection.addEventListener(Event.CLOSE, onConnectionError);
        connection.connect(host, port);
    }

    private function onConnect(event:Event):void {
        log.add("onConnect");

        authFacadeReceiver = new AuthFacadeReceiver(this);
        connection.registerReceiver(authFacadeReceiver);

        authFacadeSender = new AuthFacadeSender(connection);
    }

    public function onAuthReady():void {
        authFacadeSender.authenticate(authenticate);
    }

    public function onAuthenticationResult(accountState:AccountStateDTO):void {
        log.add("onAuthenticationResult");

        connection.unregisterReceiver(authFacadeReceiver);

        removeLoadingScreen();

        menu = new Menu(accountState, connection, policyPort, new AccountFacadeSender(connection), log, layout, social, locale);
        addChild(menu);
        accountFacadeReceiver = new AccountFacadeReceiver(menu);
        connection.registerReceiver(accountFacadeReceiver);
    }

    private function destroyConnection():void {
        connection.removeEventListener(Event.CONNECT, onConnect);
        connection.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onConnectionError);
        connection.removeEventListener(IOErrorEvent.IO_ERROR, onConnectionError);
        connection.removeEventListener(Event.CLOSE, onConnectionError);
        connection = null;

        if (loadingScreen) removeLoadingScreen();

        if (menu) {
            menu.removeListeners();
            removeChild(menu);
            menu = null;
        }
    }

    private function onConnectionError(event:Event):void {
        log.add("onConnectionError");

        destroyConnection();

        noConnectionScreen = new NoConnectionScreen(layout, locale);
        noConnectionScreen.addEventListener(NoConnectionScreen.TRY_CONNECT, onTryConnect);
        addChild(noConnectionScreen);
    }

    private function onTryConnect(event:Event = null):void {
        noConnectionScreen.removeEventListener(NoConnectionScreen.TRY_CONNECT, onTryConnect);
        removeChild(noConnectionScreen);

        addLoadingScreen();
        tryConnect();
    }

    public function addLoadingScreen():void {
        if (loadingScreen) throw new Error("loading screen already created");
        addChild(loadingScreen = new LoadingScreen("Loading..", layout.loadingTextFormat, layout));
    }

    public function removeLoadingScreen():void {
        removeChild(loadingScreen);
        loadingScreen = null;
    }

    public function updateLayout(layout:Layout):void {
        this.layout = layout;
        if (menu) menu.updateLayout(layout);
        if (loadingScreen) loadingScreen.updateLayout(layout, layout.loadingTextFormat);
        if (noConnectionScreen) noConnectionScreen.updateLayout(layout);
    }
}
}
