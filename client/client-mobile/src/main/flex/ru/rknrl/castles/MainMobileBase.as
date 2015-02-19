//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.display.StageAspectRatio;
import flash.system.Capabilities;
import flash.utils.ByteArray;

import org.onepf.OpenIAB;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.menu.factory.MobileFactory;
import ru.rknrl.core.social.SocialMobile;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.log.Log;

public class MainMobileBase extends Main {
    [Embed(source="/castles - RU.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private static function isTablet(fullScreenWidth:int, fullScreenHeight:int):Boolean {
        const dpi:Number = Capabilities.screenDPI;
        const max:int = Math.max(fullScreenWidth, fullScreenHeight);
        const inch:Number = max / dpi;
        const maxPhoneInch:Number = 50.1; // todo
        return inch > maxPhoneInch;
    }

    public function MainMobileBase(host:String, gamePort:int, policyPort:int, httpPort:int) {
        Log.info(stage.fullScreenWidth + "x" + stage.fullScreenHeight);

        stage.autoOrients = false;

        const tablet:Boolean = isTablet(stage.fullScreenWidth, stage.fullScreenHeight);
        stage.setAspectRatio(tablet ? StageAspectRatio.LANDSCAPE : StageAspectRatio.PORTRAIT);
        const deviceType:DeviceType = tablet ? DeviceType.TABLET : DeviceType.PHONE;

        const accountType:AccountType = AccountType.DEV;

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = "1";
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = "secret";
        authenticationSecret.params = null;

        Log.info("authenticationSecret=" + authenticationSecret.body);
        Log.info("authenticationParams=" + authenticationSecret.params);

        const layout:Layout = tablet ? new LayoutLandscape(stage.fullScreenWidth, stage.fullScreenHeight, stage.contentsScaleFactor) : new LayoutPortrait(stage.fullScreenWidth, stage.fullScreenHeight, stage.contentsScaleFactor);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        const ane:OpenIAB = new OpenIAB();
        Log.info("ANE:" + ane.init());
        const social:SocialMobile = new SocialMobile(ane);

        super(host, gamePort, policyPort, httpPort, accountId, authenticationSecret, deviceType, localesUrl, defaultLocale, social, layout, new MobileFactory());
    }
}
}
