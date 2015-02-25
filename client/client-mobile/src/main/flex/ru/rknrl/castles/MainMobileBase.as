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
import flash.display.StageAspectRatio;
import flash.display.StageScaleMode;
import flash.events.Event;
import flash.events.UncaughtErrorEvent;
import flash.system.Capabilities;
import flash.utils.ByteArray;

import org.onepf.OpenIAB;

import ru.rknrl.DeviceId;
import ru.rknrl.Log;
import ru.rknrl.Warning;
import ru.rknrl.asocial.mobile.FB;
import ru.rknrl.asocial.mobile.ISocialMobile;
import ru.rknrl.asocial.mobile.NO;
import ru.rknrl.asocial.mobile.Social;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.menu.factory.MobileFactory;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.dto.PlatformType;

public class MainMobileBase extends Sprite {
    [Embed(source="/castles - RU.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private static const facebookAppId:String = "370172643168842";
    private static const facebookAppIdDev:String = "370173203168786";

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
    private var bugsLogUrl:String;
    private var deviceType:DeviceType;
    private var platformType:PlatformType;

    private var layout:Layout;
    private var loginScreen:LoginScreen;
    private var webViewBackground:WebViewBackground;
    private var openIab:OpenIAB;
    private var social:ISocialMobile;
    private var main:Main;

    public function MainMobileBase(host:String, gamePort:int, policyPort:int, httpPort:int) {
        this.host = host;
        this.gamePort = gamePort;
        this.policyPort = policyPort;
        this.httpPort = httpPort;

        this.bugsLogUrl = "http://" + host + ":" + httpPort + "/bug";
        Log.info("bugsLogUrl=" + bugsLogUrl);

        loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, onUncaughtError);

        Log.info(stage.fullScreenWidth + "x" + stage.fullScreenHeight);
        stage.autoOrients = false;
        stage.align = StageAlign.TOP_LEFT;
        stage.scaleMode = StageScaleMode.NO_SCALE;

        const tablet:Boolean = isTablet(stage.fullScreenWidth, stage.fullScreenHeight);
        stage.setAspectRatio(tablet ? StageAspectRatio.LANDSCAPE : StageAspectRatio.PORTRAIT);
        deviceType = tablet ? DeviceType.TABLET : DeviceType.PHONE;
        Log.info("deviceType=" + deviceType.name());

        platformType = Capabilities.manufacturer.match(/android/i) ? PlatformType.ANDROID : PlatformType.IOS;
        Log.info("platformType=" + platformType.name());

        layout = tablet ? new LayoutLandscape(stage.fullScreenWidth, stage.fullScreenHeight, stage.contentsScaleFactor) : new LayoutPortrait(stage.fullScreenWidth, stage.fullScreenHeight, stage.contentsScaleFactor);

        openIab = new OpenIAB();
        Log.info("PaymentsANE:" + openIab.init());

        addLoginScreen();
    }

    private function addLoginScreen():void {
        addChild(loginScreen = new LoginScreen(layout));
        loginScreen.addEventListener(LoginScreen.LOGIN_VIA_DEVICE_ID, onLoginViaDeviceId);
        loginScreen.addEventListener(LoginScreen.LOGIN_VIA_FACEBOOK, onLoginViaFacebook);
    }

    private function removeLoginScreen():void {
        loginScreen.removeEventListener(LoginScreen.LOGIN_VIA_DEVICE_ID, onLoginViaDeviceId);
        loginScreen.removeEventListener(LoginScreen.LOGIN_VIA_FACEBOOK, onLoginViaFacebook);
        removeChild(loginScreen);
    }

    private function onLoginViaDeviceId(e:Event):void {
        removeLoginScreen();

        const deviceId:String = new DeviceId().get();
        social = new NO(deviceId, new PaymentsBridge(openIab));
        start(AccountType.DEVICE_ID, deviceId, "");
    }

    private function onLoginViaFacebook(e:Event):void {
        removeLoginScreen();

        webViewBackground = new WebViewBackground(layout);
        addChild(webViewBackground);

        social = new FB(facebookAppIdDev, new PaymentsBridge(openIab), stage);
        social.addEventListener(Social.LOGIN_SUCCESS, onFacebookLoginSuccess);
        social.addEventListener(Social.LOGIN_FAIL, onFacebookLoginFail);
        social.login();
    }

    private function onFacebookLoginSuccess(e:Event):void {
        social.removeEventListener(Social.LOGIN_SUCCESS, onFacebookLoginSuccess);
        social.removeEventListener(Social.LOGIN_FAIL, onFacebookLoginFail);
        removeChild(webViewBackground);

        start(AccountType.FACEBOOK, "uid", "secret"); // todo uid, secret
    }

    private function onFacebookLoginFail(e:Event):void {
        social.removeEventListener(Social.LOGIN_SUCCESS, onFacebookLoginSuccess);
        social.removeEventListener(Social.LOGIN_FAIL, onFacebookLoginFail);
        removeChild(webViewBackground);

        addLoginScreen();
    }

    private function start(accountType:AccountType, id:String, secret:String):void {
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


        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        addChild(main = new Main(host, gamePort, policyPort, accountId, authenticationSecret, deviceType, platformType, localesUrl, defaultLocale, social, layout, new MobileFactory(), loaderInfo));
    }

    private function onUncaughtError(event:UncaughtErrorEvent):void {
        const error:Error = event.error as Error;
        const stackTrace:String = error ? error.getStackTrace() : "";
        Log.error(event.error, stackTrace);
        Log.send(bugsLogUrl);
        if (!(error is Warning) && main) main.addErrorScreen();
    }
}
}

import org.onepf.OpenIAB;

import ru.rknrl.asocial.PaymentDialogData;
import ru.rknrl.asocial.mobile.IPayments;

class PaymentsBridge implements IPayments {
    private var openIab:OpenIAB;

    public function PaymentsBridge(openIab:OpenIAB) {
        this.openIab = openIab;
    }

    public function purchase(paymentsDialogData:PaymentDialogData):void {
        openIab.purchase(paymentsDialogData.id.toString());
    }
}