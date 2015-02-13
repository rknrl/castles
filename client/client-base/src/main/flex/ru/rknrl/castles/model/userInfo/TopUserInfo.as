//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.userInfo {
import ru.rknrl.dto.TopUserInfoDTO;

public class TopUserInfo {
    private var _place:int;

    public function get place():int {
        return _place;
    }

    private var _info:CastlesUserInfo;

    public function get info():CastlesUserInfo {
        return _info;
    }

    public function TopUserInfo(place:int, info:CastlesUserInfo) {
        _place = place;
        _info = info;
    }

    public static function fromDto(dto:TopUserInfoDTO):TopUserInfo {
        return new TopUserInfo(dto.place, CastlesUserInfo.fromDto(dto.info));
    }
}
}
