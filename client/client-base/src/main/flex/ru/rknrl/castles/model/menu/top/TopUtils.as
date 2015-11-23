//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.top {
import protos.AccountType;
import protos.Top;
import protos.TopUserInfo;

import ru.rknrl.castles.model.userInfo.CastlesUserInfo;

public class TopUtils {
    public static function getPlace(top:Top, place:int):CastlesUserInfo {
        for each(var userInfo:TopUserInfo in top.users) {
            if (userInfo.place == place) return CastlesUserInfo.fromDto(userInfo.info);
        }
        return new CastlesUserInfo(place.toString(), AccountType.DEV, "Somebody", null, null, null);
    }
}
}
