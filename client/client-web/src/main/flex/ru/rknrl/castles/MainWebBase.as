//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.events.Event;
import flash.system.Security;
import flash.utils.ByteArray;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.menu.factory.CanvasFactory;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.log.Log;

public class MainWebBase extends Main {
    [Embed(source="/castles - RU.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    public function MainWebBase(host:String, gamePort:int, policyPort:int, httpPort:int, accountId:AccountIdDTO, authenticationSecret:AuthenticationSecretDTO, social:Social) {
        Security.allowDomain("*");

        Log.info("authenticationSecret=" + authenticationSecret.body);
        Log.info("authenticationParams=" + authenticationSecret.params);

        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        super(host, gamePort, policyPort, httpPort, accountId, authenticationSecret, DeviceType.CANVAS, localesUrl, defaultLocale, social, layout, new CanvasFactory());

        stage.addEventListener(Event.RESIZE, onResize);
    }

    private function onResize(event:Event):void {
        Log.info("resize " + stage.stageWidth + "x" + stage.stageHeight);
        layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, contentsScaleFactor);
    }

    private function get contentsScaleFactor():Number {
        return stage.hasOwnProperty("contentsScaleFactor") ? stage["contentsScaleFactor"] : 1;
    }
}
}
