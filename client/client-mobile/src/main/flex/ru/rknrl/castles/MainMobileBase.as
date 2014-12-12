package ru.rknrl.castles {
import flash.display.StageAspectRatio;
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

public class MainMobileBase extends Main {
    [Embed(source="/castles - EN.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private static function isTablet(fullScreenWidth:int, fullScreenHeight:int):Boolean {
        const dpi:Number = Capabilities.screenDPI;
        const max:int = Math.max(fullScreenWidth, fullScreenHeight);
        const inch:Number = max / dpi;
        const maxPhoneInch:Number = 5.1; // todo
        return inch > maxPhoneInch;
    }

    public function MainMobileBase(host:String, gamePort:int, policyPort:int) {
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

        super(host, gamePort, policyPort, authenticate, localesUrl, defaultLocale, log, social, layout);
    }
}
}
