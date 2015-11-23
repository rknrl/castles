//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import protos.AccountId;
import protos.AccountType;
import protos.AuthenticationSecret;

import ru.rknrl.asocial.ISocialWeb;
import ru.rknrl.asocial.platforms.Facebook;
import ru.rknrl.asocial.platforms.MoiMir;
import ru.rknrl.asocial.platforms.Odnoklassniki;
import ru.rknrl.asocial.platforms.Vkontakte;
import ru.rknrl.common.print;
import ru.rknrl.log.Log;

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
        const social:ISocialWeb = createSocial(accountType, flashVars);

        const accountId:AccountId = new AccountId(accountType, social.flashVars.uid);

        const authenticationSecret:AuthenticationSecret = new AuthenticationSecret(
                social.flashVars.authenticationSecret,
                social.flashVars.authenticationParams,
                null
        );

        super(host, gamePort, policyPort, httpPort, accountId, authenticationSecret, social);
    }

    private static function getAccountType(name:String):AccountType {
        for each(var accountType:AccountType in AccountType.values) {
            if (accountType.name() == name) return accountType;
        }
        throw new Error("unknown account type " + name);
    }

    private static function createSocial(accountType:AccountType, flashVars:Object):ISocialWeb {
        switch (accountType) {
            case AccountType.ODNOKLASSNIKI:
                return new Odnoklassniki(flashVars);
            case AccountType.VKONTAKTE:
                return new Vkontakte(flashVars);
            case AccountType.MOIMIR:
                return new MoiMir(flashVars, "05ba20802b21da64c5daab3ad3ceb05d");
            case AccountType.FACEBOOK:
                return new Facebook(flashVars);
        }
        throw new Error("unknown account type " + accountType);
    }
}
}
