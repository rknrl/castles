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
import ru.rknrl.asocial.ISocialMobile;
import ru.rknrl.asocial.Social;
import ru.rknrl.asocial.platforms.Facebook;
import ru.rknrl.asocial.platforms.MoiMir;
import ru.rknrl.asocial.platforms.Odnoklassniki;
import ru.rknrl.asocial.platforms.SocialMock;
import ru.rknrl.asocial.platforms.Vkontakte;
import ru.rknrl.asocial.userInfo.Sex;
import ru.rknrl.asocial.userInfo.UserInfo;
import ru.rknrl.castles.view.LoginEvent;
import ru.rknrl.castles.view.LoginScreen;
import ru.rknrl.castles.view.WebViewBackground;
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

    private static const vkontakteAppId:String = "4662212";
    private static const vkontakteAppIdDev:String = "4628723";

    private static const odniklassnikiAppIdDev:String = "1108376832";

    private static const moiMirAppIdDev:String = "726774";
    private static const moiMirPrivateKeyDev:String = "05ba20802b21da64c5daab3ad3ceb05d";

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
    private var myUserInfo:UserInfo;
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
        loginScreen.addEventListener(LoginEvent.LOGIN, onLogin);
    }

    private function removeLoginScreen():void {
        loginScreen.removeEventListener(LoginEvent.LOGIN, onLogin);
        removeChild(loginScreen);
    }

    private function onLogin(e:LoginEvent):void {
        removeLoginScreen();

        switch (e.socialName) {
            case LoginScreen.DEVICE_ID:
                const deviceId:String = new DeviceId().get();
                social = new SocialMock(deviceId, new PaymentsBridge(openIab));
                myUserInfo = new UserInfo({}, deviceId, null, null, Sex.UNDEFINED);
                start(AccountType.DEVICE_ID, deviceId, "");
                break;
            case LoginScreen.FACEBOOK:
                social = new Facebook(facebookAppIdDev, new PaymentsBridge(openIab), stage);
                webViewLogin();
                break;
            case LoginScreen.VKONTAKTE:
                social = new Vkontakte(vkontakteAppIdDev, new PaymentsBridge(openIab), stage);
                webViewLogin();
                break;
            case LoginScreen.ODNOKLASSNIKI:
                social = new Odnoklassniki(odniklassnikiAppIdDev, new PaymentsBridge(openIab), stage);
                webViewLogin();
                break;
            case LoginScreen.MOI_MIR:
                social = new MoiMir(moiMirAppIdDev, moiMirPrivateKeyDev, new PaymentsBridge(openIab), stage);
                webViewLogin();
                break;
            default :
                throw new Error("unknown social name: " + e.socialName);
        }
    }

    private function webViewLogin():void {
        webViewBackground = new WebViewBackground(layout);
        webViewBackground.addEventListener(WebViewBackground.LOGIN_CANCEL, onLoginCancel);
        addChild(webViewBackground);

        social.addEventListener(Social.LOGIN_SUCCESS, onFacebookLoginSuccess);
        social.addEventListener(Social.LOGIN_FAIL, onFacebookLoginFail);
        social.login();
    }

    private function onLoginCancel(event:Event):void {
        social.closeWebView();
        onFacebookLoginFail();
    }

    private function onFacebookLoginSuccess(e:Event):void {
        social.removeEventListener(Social.LOGIN_SUCCESS, onFacebookLoginSuccess);
        social.removeEventListener(Social.LOGIN_FAIL, onFacebookLoginFail);
        removeChild(webViewBackground);

        social.api.me(onGetMyUserInfo);
    }

    private function onGetMyUserInfo(userInfo:UserInfo):void {
        if (userInfo) {
            myUserInfo = userInfo;
            Log.info("myUserInfo: " + myUserInfo);
            start(AccountType.DEVICE_ID, myUserInfo.uid, social.accessToken);
        } else {
            addLoginScreen();
            Log.info("myUserInfo fail");
        }
    }

    private function onFacebookLoginFail(e:Event = null):void {
        social.removeEventListener(Social.LOGIN_SUCCESS, onFacebookLoginSuccess);
        social.removeEventListener(Social.LOGIN_FAIL, onFacebookLoginFail);
        removeChild(webViewBackground);

        addLoginScreen();
    }

    private function start(accountType:AccountType, id:String, accessToken:String):void {
        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = id;
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = accessToken;
        authenticationSecret.params = null;
        authenticationSecret.accessToken = accessToken;

        Log.info("accountId=" + accountId.id);
        Log.info("accountType=" + accountId.type.name());
        Log.info("authenticationSecret=" + authenticationSecret.body);
        Log.info("authenticationParams=" + authenticationSecret.params);
        Log.info("authenticationAccessToken=" + authenticationSecret.accessToken);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        addChild(main = new Main(host, gamePort, policyPort, accountId, authenticationSecret, deviceType, platformType, localesUrl, defaultLocale, social, layout, new MobileFactory(), myUserInfo));
    }

    private var hasError:Boolean;

    private function onUncaughtError(event:UncaughtErrorEvent):void {
        if (!hasError) {
            hasError = true;

            const error:Error = event.error as Error;
            const stackTrace:String = error ? error.getStackTrace() : "";
            Log.error(event.error, stackTrace);
            Log.send(bugsLogUrl);
            if (main) main.addErrorScreen();
        }
    }
}
}

import org.onepf.OpenIAB;

import ru.rknrl.asocial.IPayments;
import ru.rknrl.asocial.PaymentDialogData;

class PaymentsBridge implements IPayments {
    private var openIab:OpenIAB;

    public function PaymentsBridge(openIab:OpenIAB) {
        this.openIab = openIab;
    }

    public function purchase(paymentsDialogData:PaymentDialogData):void {
        openIab.purchase(paymentsDialogData.id.toString());
    }
}