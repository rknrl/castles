package ru.rknrl.castles {
import flash.system.Security;

import ru.rknrl.core.social.Social;
import ru.rknrl.core.social.SocialMock;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticateDTO;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.log.Log;

[SWF(width="1024", height="768", frameRate="60", quality="high")]
public class MainWebStandalone extends MainWebBase {
    private static const host:String = "127.0.0.1";
    private static const gamePort:int = 2335;
    private static const policyPort:int = 2336;

    public function MainWebStandalone() {
        Security.allowDomain("*");

        const log:Log = new Log();

        const accountType:AccountType = AccountType.DEV;
        const social:Social = new SocialMock(log);

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = "1";
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = "body";

        super(log, host, gamePort, policyPort, accountId, authenticationSecret, social);
    }
}
}
