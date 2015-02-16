//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

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

import ru.rknrl.Warning;
import ru.rknrl.castles.controller.Controller;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.userInfo.CastlesUserInfo;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.core.social.Sex;
import ru.rknrl.core.social.Social;
import ru.rknrl.core.social.UserInfo;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.loaders.LoadImageManager;
import ru.rknrl.loaders.TextLoader;
import ru.rknrl.log.Log;
import ru.rknrl.rmi.AuthenticatedEvent;
import ru.rknrl.rmi.Server;

public class Main extends Sprite {
    private static const cachedAvatarsLimit:int = 10;

    private var bugsLogUrl:String;

    private var host:String;
    private var gamePort:int;
    private var policyPort:int;

    private var accountId:AccountIdDTO;
    private var secret:AuthenticationSecretDTO;
    private var deviceType:DeviceType;

    private var localesUrl:String;
    private var defaultLocale:String;
    private var social:Social;

    private var myUserInfo:UserInfo;

    private var server:Server;

    private var localeLoader:TextLoader;
    private var locale:CastlesLocale;

    private var view:View;
    private var loadImageManager:ILoadImageManager;
    private var deviceFactory:DeviceFactory;

    private var controller:Controller;

    public function Main(host:String, gamePort:int, policyPort:int, httpPort:int, accountId:AccountIdDTO, secret:AuthenticationSecretDTO, deviceType:DeviceType, localesUrl:String, defaultLocale:String, social:Social, layout:Layout, deviceFactory:DeviceFactory) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.bugsLogUrl = "http://" + host + ":" + httpPort + "/bug";
        Log.add("bugsLogUrl=" + bugsLogUrl);
        this.accountId = accountId;
        this.secret = secret;
        this.deviceType = deviceType;
        this.localesUrl = localesUrl;
        this.defaultLocale = defaultLocale;
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

        localeLoader = new TextLoader(localesUrl + "castles - RU.tsv");
        localeLoader.addEventListener(Event.COMPLETE, onLocaleComplete);
        localeLoader.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onLocaleError);
        localeLoader.addEventListener(IOErrorEvent.IO_ERROR, onLocaleError);
        localeLoader.load();
    }

    private function onLocaleError(event:Event):void {
        Log.add("locale error " + event);
        Log.add("use default locale");

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

        loadImageManager = new LoadImageManager(cachedAvatarsLimit);
        if (view) throw new Error("view already on stage");
        addChild(view = new View(_layout, locale, loadImageManager, deviceFactory));
        view.addEventListener(ViewEvents.TRY_CONNECT, onTryConnect);
        view.addLoadingScreen();

        social.getMyUserInfo(onGetMyUserInfo);
    }

    private function onGetMyUserInfo(userInfo:UserInfo):void {
        if (userInfo) {
            myUserInfo = userInfo;
            Log.add("myUserInfo: " + myUserInfo)
        } else {
            myUserInfo = new UserInfo(accountId.id, locale.defaultName, null, Sex.UNDEFINED);
            Log.add("myUserInfo fail");
        }

        tryConnect();
    }

    private function tryConnect():void {
        createConnection(host, gamePort)
    }

    private function createConnection(host:String, port:int):void {
        if (server) throw new Error("already connected");

        Security.loadPolicyFile("xmlsocket://" + host + ":" + policyPort);

        server = new Server(new Socket());
        server.addEventListener(Event.CONNECT, onConnect);
        server.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onConnectionError);
        server.addEventListener(IOErrorEvent.IO_ERROR, onConnectionError);
        server.addEventListener(Event.CLOSE, onConnectionError);
        server.connect(host, port);
    }

    private function onConnect(event:Event):void {
        Log.add("onConnect");
        const authenticate:AuthenticateDTO = new AuthenticateDTO();
        authenticate.userInfo = CastlesUserInfo.userInfoDto(myUserInfo, accountId.type);
        authenticate.secret = secret;
        authenticate.deviceType = deviceType;
        server.addEventListener(AuthenticatedEvent.AUTHENTICATED, onAuthenticated);
        server.authenticate(authenticate);
    }

    private function onAuthenticated(e:AuthenticatedEvent):void {
        server.removeEventListener(AuthenticatedEvent.AUTHENTICATED, onAuthenticated);
        Log.add("onAuthenticationResult");

        view.removeLoadingScreen();
        controller = new Controller(view, e.success, server, social);
    }

    private function destroyConnection():void {
        server.removeEventListener(AuthenticatedEvent.AUTHENTICATED, onAuthenticated);
        server.removeEventListener(Event.CONNECT, onConnect);
        server.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onConnectionError);
        server.removeEventListener(IOErrorEvent.IO_ERROR, onConnectionError);
        server.removeEventListener(Event.CLOSE, onConnectionError);
        server = null;

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
        Log.add("onConnectionError");
        destroy();
    }

    private function onUncaughtError(event:UncaughtErrorEvent):void {
        const error:Error = event.error as Error;
        const stackTrace:String = error ? error.getStackTrace() : "";
        Log.error(event.error, stackTrace);
        Log.send(bugsLogUrl);
        if (!(error is Warning)) addErrorScreen();
    }

    private function addErrorScreen():void {
        destroyConnection();
        view.removeLoadingScreenIfExists();
        view.addErrorScreen();
    }
}
}
