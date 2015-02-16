//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import ru.rknrl.core.social.MM;
import ru.rknrl.core.social.OK;
import ru.rknrl.core.social.SocialWeb;
import ru.rknrl.core.social.VK;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticationSecretDTO;
import ru.rknrl.log.Log;
import ru.rknrl.utils.print;

[SWF(frameRate="60", quality="high")]
public class MainWeb extends MainWebBase {
    public function MainWeb() {
        const flashVars:Object = loaderInfo.parameters;
        Log.add("flashVars:\n" + print(flashVars));

        const host:String = flashVars.rknrlHost;
        const gamePort:int = flashVars.rknrlGamePort;
        const policyPort:int = flashVars.rknrlPolicyPort;
        const httpPort:int = flashVars.rknrlHttpPort;

        const accountType:AccountType = getAccountType(flashVars.rknrlAccountType);
        const social:SocialWeb = createSocial(accountType, flashVars);

        const accountId:AccountIdDTO = new AccountIdDTO();
        accountId.id = social.flashVars.uid;
        accountId.type = accountType;

        const authenticationSecret:AuthenticationSecretDTO = new AuthenticationSecretDTO();
        authenticationSecret.body = social.flashVars.authenticationSecret;
        authenticationSecret.params = social.flashVars.authenticationParams;

        super(host, gamePort, policyPort, httpPort, accountId, authenticationSecret, social);
    }

    private static function getAccountType(name:String):AccountType {
        for each(var accountType:AccountType in AccountType.values) {
            if (accountType.name() == name) return accountType;
        }
        throw new Error("unknown account type " + name);
    }

    private static function createSocial(accountType:AccountType, flashVars:Object):SocialWeb {
        switch (accountType) {
            case AccountType.ODNOKLASSNIKI:
                return new OK(flashVars);
            case AccountType.VKONTAKTE:
                return new VK(flashVars);
            case AccountType.MOIMIR:
                return new MM(flashVars);
        }
        throw new Error("unknown account type " + accountType);
    }
}
}
