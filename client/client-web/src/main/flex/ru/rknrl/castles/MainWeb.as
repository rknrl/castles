//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import ru.rknrl.Log;
import ru.rknrl.asocial.ISocial;
import ru.rknrl.asocial.web.FB;
import ru.rknrl.asocial.web.MM;
import ru.rknrl.asocial.web.OK;
import ru.rknrl.asocial.web.Social;
import ru.rknrl.asocial.web.VK;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.print;

[SWF(frameRate="60", quality="high")]
public class MainWeb extends MainWebBase {
    public function MainWeb() {
        const flashVars:Object = loaderInfo.parameters;
        Log.info("flashVars:\n" + print(flashVars));

        const host:String = flashVars.rknrlHost;
        const gamePort:int = flashVars.rknrlGamePort;
        const policyPort:int = flashVars.rknrlPolicyPort;
        const httpPort:int = flashVars.rknrlHttpPort;

        const accountType:AccountType = getAccountType(flashVars.rknrlAccountType);
        const social:Social = createSocial(accountType, flashVars);

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = social.flashVars.uid;
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = social.flashVars.authenticationSecret;
        authenticationSecret.params = social.flashVars.authenticationParams;

        super(host, gamePort, policyPort, httpPort, accountId, authenticationSecret, ISocial(social));
    }

    private static function getAccountType(name:String):AccountType {
        for each(var accountType:AccountType in AccountType.values) {
            if (accountType.name() == name) return accountType;
        }
        throw new Error("unknown account type " + name);
    }

    private static function createSocial(accountType:AccountType, flashVars:Object):Social {
        switch (accountType) {
            case AccountType.ODNOKLASSNIKI:
                return new OK(flashVars);
            case AccountType.VKONTAKTE:
                return new VK(flashVars);
            case AccountType.MOIMIR:
                return new MM(flashVars);
            case AccountType.FACEBOOK:
                return new FB(flashVars);
        }
        throw new Error("unknown account type " + accountType);
    }
}
}
