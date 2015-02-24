//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.system.Security;

import ru.rknrl.asocial.ISocial;
import ru.rknrl.asocial.SocialMock;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticationSecretDTO;

[SWF(width="1024", height="768", frameRate="60", quality="high")]
public class MainWebStandalone extends MainWebBase {
//    private static const host:String = "castles.rknrl.ru";
    private static const host:String = "127.0.0.1";
    private static const gamePort:int = 2335;
    private static const policyPort:int = 2336;
    private static const httpPort:int = 8080;

    public function MainWebStandalone() {
        Security.allowDomain("*");

        const accountType:AccountType = AccountType.DEV;
        const social:ISocial = new SocialMock();

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = "1";
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = "body";

        super(host, gamePort, policyPort, httpPort, accountId, authenticationSecret, social);
    }
}
}
