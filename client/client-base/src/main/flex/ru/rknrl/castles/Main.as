package ru.rknrl.castles {
import flash.display.Sprite;
import flash.display.StageAlign;
import flash.display.StageQuality;
import flash.display.StageScaleMode;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.system.Security;

import ru.rknrl.castles.controller.Controller;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.rmi.AccountFacadeReceiver;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.rmi.AuthFacadeReceiver;
import ru.rknrl.castles.rmi.AuthFacadeSender;
import ru.rknrl.castles.rmi.IAuthFacade;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.core.rmi.Connection;
import ru.rknrl.core.social.Sex;
import ru.rknrl.core.social.Social;
import ru.rknrl.core.social.UserInfo;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.dto.AuthenticationSuccessDTO;
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

    private var localeLoader:TextLoader;
    private var locale:CastlesLocale;

    private var view:View;
    private var loadImageManager:LoadImageManager;

    private var controller:Controller;

    public function Main(host:String, gamePort:int, policyPort:int, authenticate:AuthenticateDTO, localesUrl:String, defaultLocale:String, log:Log, social:Social, layout:Layout) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.authenticate = authenticate;
        this.localesUrl = localesUrl;
        this.defaultLocale = defaultLocale;
        this.log = log;
        this.social = social;
        _layout = layout;
        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        if (view) view.layout = value;
    }

    private function onAddedToStage(event:Event):void {
        removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        stage.scaleMode = StageScaleMode.NO_SCALE;
        stage.align = StageAlign.TOP_LEFT;
        stage.quality = StageQuality.BEST;

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

        loadImageManager = new LoadImageManager();
        addChild(view = new View(_layout, locale, loadImageManager));
        view.addEventListener(ViewEvents.TRY_CONNECT, onTryConnect);
        view.addLoadingScreen();

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

    public function onAuthenticationSuccess(authenticationSuccess:AuthenticationSuccessDTO):void {
        log.add("onAuthenticationResult");

        connection.unregisterReceiver(authFacadeReceiver);

        view.removeLoadingScreen();
        controller = new Controller(view, authenticationSuccess, connection, policyPort, new AccountFacadeSender(connection), log, social);

        accountFacadeReceiver = new AccountFacadeReceiver(controller);
        connection.registerReceiver(accountFacadeReceiver);
    }

    private function destroyConnection():void {
        connection.removeEventListener(Event.CONNECT, onConnect);
        connection.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onConnectionError);
        connection.removeEventListener(IOErrorEvent.IO_ERROR, onConnectionError);
        connection.removeEventListener(Event.CLOSE, onConnectionError);
        connection = null;

        if (controller) {
            view.removeMenu();
            controller.destroy();
            controller = null;
        }
    }

    private function onConnectionError(event:Event):void {
        log.add("onConnectionError");

        destroyConnection();

        view.removeLoadingScreenIfExists();
        view.addNoConnectionScreen();
    }

    private function onTryConnect(event:Event = null):void {
        view.removeNoConnectionScreen();
        view.addLoadingScreen();
        tryConnect();
    }
}
}
