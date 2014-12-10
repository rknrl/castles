package ru.rknrl.castles {
import flash.events.Event;
import flash.system.Security;
import flash.utils.ByteArray;

import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.log.Log;

[SWF(width="1024", height="768", frameRate="60", quality="high")]
public class MainWebBase extends Main {
    [Embed(source="/castles - EN.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private var log:Log;

    public function MainWebBase(log:Log, host:String, port:int, accountId:AccountIdDTO, authenticationSecret:AuthenticationSecretDTO, social:Social) {
        this.log = log;

        Security.allowDomain("*");

        log.add("authenticationSecret=" + authenticationSecret.body);
        log.add("authenticationParams=" + authenticationSecret.params);

        const authenticate:AuthenticateDTO = new AuthenticateDTO();
        authenticate.accountId = accountId;
        authenticate.deviceType = DeviceType.CANVAS;
        authenticate.secret = authenticationSecret;

        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        super(host, port, authenticate, localesUrl, defaultLocale, log, social, layout);

        stage.addEventListener(Event.RESIZE, onResize);
    }

    private function onResize(event:Event):void {
        log.add("resize " + stage.stageWidth + "x" + stage.stageHeight);
        updateLayout(new LayoutLandscape(stage.stageWidth, stage.stageHeight));
    }
}
}
