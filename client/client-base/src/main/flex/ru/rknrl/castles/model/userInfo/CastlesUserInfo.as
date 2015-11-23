//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.userInfo {
import protos.AccountId;
import protos.AccountType;

import ru.rknrl.asocial.userInfo.Sex;
import ru.rknrl.asocial.userInfo.UserInfo;
import ru.rknrl.asocial.userInfo.UserInfoAvatarUrl;

public class CastlesUserInfo extends UserInfo {

    public function CastlesUserInfo(uid:String, accountType:AccountType, firstName:String, lastName:String, photo96:String, photo256:String) {
        super({}, uid, firstName, lastName, Sex.UNDEFINED);
        _accountType = accountType;
        _photo96 = photo96;
        _photo256 = photo256;
    }

    private var _accountType:AccountType;

    public function get accountType():AccountType {
        return _accountType;
    }

    private var _photo96:String;
    private var _photo256:String;

    override protected function getAvatarUrls():Vector.<UserInfoAvatarUrl> {
        const urls:Vector.<UserInfoAvatarUrl> = new <UserInfoAvatarUrl>[];
        if (_photo96 != null) urls.push(new UserInfoAvatarUrl(96, _photo96));
        if (_photo256 != null) urls.push(new UserInfoAvatarUrl(256, _photo256));
        return urls;
    }

    public static function fromDto(dto:protos.UserInfo):CastlesUserInfo {
        return new CastlesUserInfo(dto.accountId.id, dto.accountId.accountType, dto.firstName, dto.lastName, dto.photo96, dto.photo256);
    }

    public static function userInfoDto(userInfo:UserInfo, accountType:AccountType):protos.UserInfo {
        return new protos.UserInfo(
                new AccountId(accountType, userInfo.uid),
                userInfo.firstName,
                userInfo.lastName,
                userInfo.getPhotoUrl(96, 96),
                userInfo.getPhotoUrl(256, 256)
        );
    }
}
}
