//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.events.Event;
import flash.events.UncaughtErrorEvent;
import flash.system.Security;
import flash.utils.ByteArray;

import ru.rknrl.Log;
import ru.rknrl.Warning;
import ru.rknrl.asocial.ISocial;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.menu.factory.CanvasFactory;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.dto.PlatformType;

public class MainWebBase extends Main {
    [Embed(source="/castles - RU.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private var bugsLogUrl:String;

    public function MainWebBase(host:String, gamePort:int, policyPort:int, httpPort:int, accountId:AccountIdDTO, authenticationSecret:AuthenticationSecretDTO, social:ISocial) {
        Security.allowDomain("*");

        this.bugsLogUrl = "http://" + host + ":" + httpPort + "/bug";
        Log.info("bugsLogUrl=" + bugsLogUrl);

        loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, onUncaughtError);

        Log.info("authenticationSecret=" + authenticationSecret.body);
        Log.info("authenticationParams=" + authenticationSecret.params);

        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        super(host, gamePort, policyPort, accountId, authenticationSecret, DeviceType.PC, PlatformType.CANVAS, localesUrl, defaultLocale, social, layout, new CanvasFactory(), loaderInfo);

        stage.addEventListener(Event.RESIZE, onResize);
    }

    private function onResize(event:Event):void {
        Log.info("resize " + stage.stageWidth + "x" + stage.stageHeight);
        layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);
    }

    private function get contentsScaleFactor():Number {
        return stage.hasOwnProperty("contentsScaleFactor") ? stage["contentsScaleFactor"] : 1;
    }

    private function onUncaughtError(event:UncaughtErrorEvent):void {
        const error:Error = event.error as Error;
        const stackTrace:String = error ? error.getStackTrace() : "";
        Log.error(event.error, stackTrace);
        Log.send(bugsLogUrl);
        if (!(error is Warning)) addErrorScreen();
    }
}
}
