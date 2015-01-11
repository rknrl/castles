package ru.rknrl.castles {
import flash.events.Event;
import flash.system.Security;
import flash.utils.ByteArray;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.log.Log;

[SWF(width="1024", height="768", frameRate="60", quality="high")]
public class MainWebBase extends Main {
    [Embed(source="/castles - EN.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private var log:Log;

    public function MainWebBase(log:Log, host:String, gamePort:int, policyPort:int, accountId:AccountIdDTO, authenticationSecret:AuthenticationSecretDTO, social:Social) {
        this.log = log;

        Security.allowDomain("*");

        log.add("authenticationSecret=" + authenticationSecret.body);
        log.add("authenticationParams=" + authenticationSecret.params);

        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, stage.contentsScaleFactor);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        super(host, gamePort, policyPort, accountId, authenticationSecret, DeviceType.CANVAS, localesUrl, defaultLocale, log, social, layout);

        stage.addEventListener(Event.RESIZE, onResize);
    }

    private function onResize(event:Event):void {
        log.add("resize " + stage.stageWidth + "x" + stage.stageHeight);
        layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, stage.contentsScaleFactor);
    }
}
}
