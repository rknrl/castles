//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.UncaughtErrorEvent;
import flash.system.Security;
import flash.utils.ByteArray;

import ru.rknrl.Log;
import ru.rknrl.Warning;
import ru.rknrl.asocial.ISocial;
import ru.rknrl.asocial.userInfo.Sex;
import ru.rknrl.asocial.userInfo.UserInfo;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.menu.factory.CanvasFactory;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.dto.PlatformType;

public class MainWebBase extends Sprite {
    [Embed(source="/castles - RU.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private var host:String;
    private var gamePort:int;
    private var policyPort:int;
    private var httpPort:int;
    private var accountId:AccountIdDTO;
    private var authenticationSecret:AuthenticationSecretDTO;
    private var social:ISocial;
    private var bugsLogUrl:String;

    private var myUserInfo:UserInfo;
    private var main:Main;

    public function MainWebBase(host:String, gamePort:int, policyPort:int, httpPort:int, accountId:AccountIdDTO, authenticationSecret:AuthenticationSecretDTO, social:ISocial) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.httpPort = httpPort;
        this.accountId = accountId;
        this.authenticationSecret = authenticationSecret;
        this.social = social;
        this.bugsLogUrl = "http://" + host + ":" + httpPort + "/bug";
        Log.info("bugsLogUrl=" + bugsLogUrl);

        loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, onUncaughtError);

        Security.allowDomain("*");

        Log.info("authenticationSecret=" + authenticationSecret.body);
        Log.info("authenticationParams=" + authenticationSecret.params);


        stage.addEventListener(Event.RESIZE, onResize);

        social.api.me(onGetMyUserInfo);
    }

    private function onGetMyUserInfo(userInfo:UserInfo):void {
        if (userInfo) {
            myUserInfo = userInfo;
            Log.info("myUserInfo: " + myUserInfo)
        } else {
            myUserInfo = new UserInfo({}, accountId.id, null, null, Sex.UNDEFINED);
            Log.info("myUserInfo fail");
        }
        start();
    }

    private function start():void {
        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();
        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);

        addChild(main = new Main(host, gamePort, policyPort, accountId, authenticationSecret, DeviceType.PC, PlatformType.CANVAS, localesUrl, defaultLocale, social, layout, new CanvasFactory(), myUserInfo));
    }

    private function onResize(event:Event):void {
        Log.info("resize " + stage.stageWidth + "x" + stage.stageHeight);
        if (main) main.layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);
    }

    private function get contentsScaleFactor():Number {
        return stage.hasOwnProperty("contentsScaleFactor") ? stage["contentsScaleFactor"] : 1;
    }

    private function onUncaughtError(event:UncaughtErrorEvent):void {
        const error:Error = event.error as Error;
        const stackTrace:String = error ? error.getStackTrace() : "";
        Log.error(event.error, stackTrace);
        Log.send(bugsLogUrl);
        if (main && !(error is Warning)) main.addErrorScreen();
    }
}
}
