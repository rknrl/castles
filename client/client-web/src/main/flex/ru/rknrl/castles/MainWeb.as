package ru.rknrl.castles {
import flash.events.Event;
import flash.system.Security;
import flash.text.Font;
import flash.utils.ByteArray;

import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.core.social.MM;
import ru.rknrl.core.social.OK;
import ru.rknrl.core.social.SocialWeb;
import ru.rknrl.core.social.VK;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.log.Log;
import ru.rknrl.utils.print;

[SWF(width="1024", height="768", frameRate="60", quality="high")]
public class MainWeb extends Main {
    [Embed(source="/castles - EN.tsv", mimeType="application/octet-stream")]
    public static const DefaultLocaleByteArray:Class;

    private var log:Log;

    public function MainWeb() {
        Security.allowDomain("*");

        log = new Log();

        const flashVars:Object = loaderInfo.parameters;
        log.add("flashVars:\n" + print(flashVars));

        const accountType:AccountType = getAccountType(flashVars.rknrlAccountType);
        const social:SocialWeb = createSocial(accountType, log, flashVars);

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = social.flashVars.uid;
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = social.flashVars.authenticationSecret;
        authenticationSecret.params = social.flashVars.authenticationParams;

        log.add("authenticationSecret=" + authenticationSecret.body);
        log.add("authenticationParams=" + authenticationSecret.params);

        const authenticate:AuthenticateDTO = new AuthenticateDTO();
        authenticate.accountId = accountId;
        authenticate.deviceType = DeviceType.CANVAS;
        authenticate.secret = authenticationSecret;

        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight);

        const localesUrl:String = "";
        const defaultLocale:String = ByteArray(new DefaultLocaleByteArray()).toString();

        super(authenticate, localesUrl, defaultLocale, log, social, layout);

        stage.addEventListener(Event.RESIZE, onResize);
    }

    private static function getAccountType(name:String):AccountType {
        for each(var accountType:AccountType in AccountType.values) {
            if (accountType.name() == name) return accountType;
        }
        throw new Error("unknown account type " + name);
    }

    private static function createSocial(accountType:AccountType, log:Log, flashVars:Object):SocialWeb {
        switch (accountType) {
            case AccountType.ODNOKLASSNIKI:
                return new OK(log, flashVars);
            case AccountType.VKONTAKTE:
                return new VK(log, flashVars);
            case AccountType.MOIMIR:
                return new MM(log, flashVars);
        }
        throw new Error("unknown account type " + accountType);
    }

    private function onResize(event:Event):void {
        log.add("resize " + stage.stageWidth + "x" + stage.stageHeight);
        updateLayout(new LayoutLandscape(stage.stageWidth, stage.stageHeight));
    }
}
}
