package ru.rknrl.castles {
import flash.display.Sprite;
import flash.display.StageAlign;
import flash.display.StageQuality;
import flash.display.StageScaleMode;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.events.UncaughtErrorEvent;
import flash.net.Socket;
import flash.system.Security;

import ru.rknrl.castles.controller.Controller;
import ru.rknrl.castles.controller.mock.LoadImageManagerMock;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.userInfo.CastlesUserInfo;
import ru.rknrl.castles.rmi.AccountFacadeReceiver;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.rmi.AuthFacadeReceiver;
import ru.rknrl.castles.rmi.AuthFacadeSender;
import ru.rknrl.castles.rmi.IAuthFacade;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.core.rmi.Connection;
import ru.rknrl.core.social.Sex;
import ru.rknrl.core.social.Social;
import ru.rknrl.core.social.UserInfo;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.AuthenticationSuccessDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.loaders.TextLoader;
import ru.rknrl.log.Log;

public class Main extends Sprite implements IAuthFacade {
    private static const errorsUrl:String = "http://127.0.0.1/bugs";
    private static const avatarsLimit:int = 10;

    private var host:String;
    private var gamePort:int;
    private var policyPort:int;

    private var accountId:AccountIdDTO;
    private var secret:AuthenticationSecretDTO;
    private var deviceType:DeviceType;

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
    private var loadImageManager:ILoadImageManager;
    private var deviceFactory:DeviceFactory;

    private var controller:Controller;

    public function Main(host:String, gamePort:int, policyPort:int, accountId:AccountIdDTO, secret:AuthenticationSecretDTO, deviceType:DeviceType, localesUrl:String, defaultLocale:String, log:Log, social:Social, layout:Layout, deviceFactory:DeviceFactory) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.accountId = accountId;
        this.secret = secret;
        this.deviceType = deviceType;
        this.localesUrl = localesUrl;
        this.defaultLocale = defaultLocale;
        this.log = log;
        this.social = social;
        _layout = layout;
        this.deviceFactory = deviceFactory;
        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, onUncaughtError);
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;
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

        loadImageManager = new LoadImageManagerMock(0, false);
        addChild(view = new View(_layout, locale, loadImageManager, deviceFactory));
        view.addEventListener(ViewEvents.TRY_CONNECT, onTryConnect);
        view.addLoadingScreen();

        social.getMyUserInfo(onGetMyUserInfo);
    }

    private function onGetMyUserInfo(userInfo:UserInfo):void {
        if (userInfo) {
            myUserInfo = userInfo;
            log.add("myUserInfo: " + myUserInfo)
        } else {
            myUserInfo = new UserInfo(accountId.id, CastlesUserInfo.defaultName, null, Sex.UNDEFINED);
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

        connection = new Connection(new Socket());
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
        const authenticate:AuthenticateDTO = new AuthenticateDTO();
        authenticate.userInfo = CastlesUserInfo.userInfoDto(myUserInfo, accountId.type);
        authenticate.secret = secret;
        authenticate.deviceType = deviceType;
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

    private function destroy():void {
        destroyConnection();
        view.removeLoadingScreenIfExists();
        view.addNoConnectionScreen();
    }

    private function onTryConnect(event:Event = null):void {
        view.removeNoConnectionScreen();
        view.addLoadingScreen();
        tryConnect();
    }

    private function onConnectionError(event:Event):void {
        log.add("onConnectionError");
        destroy();
    }

    private function onUncaughtError(event:UncaughtErrorEvent):void {
        const error:Error = event.error as Error;
        const stackTrace:String = error ? error.getStackTrace() : "";
        log.error(event.error.toString(), stackTrace);
        log.send(errorsUrl);
        destroy();
    }
}
}
