package ru.rknrl.castles {
import flash.display.StageAspectRatio;
import flash.display.StageOrientation;
import flash.system.Capabilities;
import flash.utils.ByteArray;

import ru.rknrl.castles.utils.layout.LayoutPortrait;
import ru.rknrl.core.social.SocialMobile;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.log.Log;

public class MainMobile extends Main {
    [Embed(source="/castles - EN.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private static const host:String = "178.62.255.28";
    private static const port:int = 2335;

    private static function isTablet(fullScreenWidth:int, fullScreenHeight:int):Boolean {
        const dpi:Number = Capabilities.screenDPI;
        const max:int = Math.max(fullScreenWidth, fullScreenHeight);
        const inch:Number = max / dpi;
        const maxPhoneInch:Number = 5.1; // todo
        return inch > maxPhoneInch;
    }

    public function MainMobile() {
        const log:Log = new Log();
        log.add(stage.fullScreenWidth + "x" + stage.fullScreenHeight);

        stage.autoOrients = false;

        if (isTablet(stage.fullScreenWidth, stage.fullScreenHeight)) {
            stage.setAspectRatio(StageAspectRatio.LANDSCAPE);
        } else {
            stage.setAspectRatio(StageAspectRatio.PORTRAIT);
        }

        const accountType:AccountType = AccountType.DEV;
        const social:SocialMobile = new SocialMobile(log);

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = "1";
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = "secret";
        authenticationSecret.params = null;

        log.add("authenticationSecret=" + authenticationSecret.body);
        log.add("authenticationParams=" + authenticationSecret.params);

        const authenticate:AuthenticateDTO = new AuthenticateDTO();
        authenticate.accountId = accountId;
        authenticate.deviceType = DeviceType.PHONE;
        authenticate.secret = authenticationSecret;

        const layout:LayoutPortrait = new LayoutPortrait(stage.fullScreenWidth, stage.fullScreenHeight);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        /*
         var ane:AndroidANE = new AndroidANE();
         const deviceLocale:String = ane.getDeviceLocale();
         trace("deviceLocale:" + deviceLocale);
         */

        super(host, port, authenticate, localesUrl, defaultLocale, log, social, layout);
    }

    private static function rotateOrientation(currentOrientation:String):String {
        switch (currentOrientation) {
            case StageOrientation.DEFAULT:
                return StageOrientation.DEFAULT;
            case StageOrientation.ROTATED_LEFT:
                return StageOrientation.DEFAULT;
            case StageOrientation.ROTATED_RIGHT:
                return StageOrientation.DEFAULT;
            case StageOrientation.UPSIDE_DOWN:
                return StageOrientation.DEFAULT;
        }
        return StageOrientation.DEFAULT;
    }
}
}
