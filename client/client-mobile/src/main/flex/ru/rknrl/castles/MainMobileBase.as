//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import com.freshplanet.ane.AirFacebook.Facebook;

import flash.display.Sprite;
import flash.display.StageAspectRatio;
import flash.events.Event;
import flash.system.Capabilities;
import flash.utils.ByteArray;

import org.onepf.OpenIAB;

import ru.rknrl.DeviceId;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.menu.factory.MobileFactory;
import ru.rknrl.core.social.SocialMobile;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.dto.PlatformType;
import ru.rknrl.log.Log;

public class MainMobileBase extends Sprite {
    [Embed(source="/castles - RU.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private static function isTablet(fullScreenWidth:int, fullScreenHeight:int):Boolean {
        const dpi:Number = Capabilities.screenDPI;
        const max:int = Math.max(fullScreenWidth, fullScreenHeight);
        const inch:Number = max / dpi;
        const maxPhoneInch:Number = 50.1; // todo
        return inch > maxPhoneInch;
    }

    private var host:String;
    private var gamePort:int;
    private var policyPort:int;
    private var httpPort:int;

    private var facebook:Facebook;
    private var loginScreen:LoginScreen;
    private var main:Main;

    public function MainMobileBase(host:String, gamePort:int, policyPort:int, httpPort:int) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.httpPort = httpPort;

        Log.info(stage.fullScreenWidth + "x" + stage.fullScreenHeight);
        stage.autoOrients = false;

        facebook = Facebook.getInstance();
//        facebook.init();

//        if (facebook.isLogin) {
//            start(AccountType.FACEBOOK, facebook.uid, facebook.accessToken);
//        } else {
        addChild(loginScreen = new LoginScreen());
        loginScreen.addEventListener(LoginScreen.LOGIN_FACEBOOK, onLoginFacebook);
        loginScreen.addEventListener(LoginScreen.LOGIN_CANCEL, onLoginCancel);
//        }
    }

    private function onLoginFacebook(e:Event):void {
        removeChild(loginScreen);
//        facebook.openSessionWithPublishPermissions();
    }

    private function onLoginCancel(e:Event):void {
        removeChild(loginScreen);
        const deviceId:String = new DeviceId().get();
        start(AccountType.DEVICE_ID, deviceId, "");
    }

    private function start(accountType:AccountType, id:String, secret:String):void {
        const tablet:Boolean = isTablet(stage.fullScreenWidth, stage.fullScreenHeight);
        stage.setAspectRatio(tablet ? StageAspectRatio.LANDSCAPE : StageAspectRatio.PORTRAIT);
        const deviceType:DeviceType = tablet ? DeviceType.TABLET : DeviceType.PHONE;
        Log.info("deviceType=" + deviceType.name());

        const platformType:PlatformType = Capabilities.manufacturer.match(/android/) ? PlatformType.ANDROID : PlatformType.IOS;
        Log.info("platformType=" + platformType.name());

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = id;
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = secret;
        authenticationSecret.params = null;

        Log.info("accountId=" + accountId.id);
        Log.info("accountType=" + accountId.type.name());
        Log.info("authenticationSecret=" + authenticationSecret.body);
        Log.info("authenticationParams=" + authenticationSecret.params);

        const layout:Layout = tablet ? new LayoutLandscape(stage.fullScreenWidth, stage.fullScreenHeight, stage.contentsScaleFactor) : new LayoutPortrait(stage.fullScreenWidth, stage.fullScreenHeight, stage.contentsScaleFactor);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        const paymentsAne:OpenIAB = new OpenIAB();
        Log.info("PaymentsANE:" + paymentsAne.init());
        const social:SocialMobile = new SocialMobile(accountId.id, facebook, paymentsAne);

        addChild(main = new Main(host, gamePort, policyPort, httpPort, accountId, authenticationSecret, deviceType, platformType, localesUrl, defaultLocale, social, layout, new MobileFactory(), loaderInfo));
    }
}
}
