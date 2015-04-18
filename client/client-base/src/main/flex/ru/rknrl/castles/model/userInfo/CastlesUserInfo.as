//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.userInfo {
import ru.rknrl.asocial.userInfo.Sex;
import ru.rknrl.asocial.userInfo.UserInfo;
import ru.rknrl.asocial.userInfo.UserInfoAvatarUrl;
import ru.rknrl.dto.AccountId;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.UserInfoDTO;

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

    public static function fromDto(dto:UserInfoDTO):CastlesUserInfo {
        return new CastlesUserInfo(dto.accountId.id, dto.accountId.accountType, dto.firstName, dto.lastName, dto.photo96, dto.photo256);
    }

    public static function userInfoDto(userInfo:UserInfo, accountType:AccountType):UserInfoDTO {
        const dto:UserInfoDTO = new UserInfoDTO();

        dto.accountId = new AccountId();
        dto.accountId.id = userInfo.uid;
        dto.accountId.accountType = accountType;

        const firstName:String = userInfo.firstName;
        if (firstName) dto.firstName = firstName;

        const lastName:String = userInfo.lastName;
        if (lastName) dto.lastName = lastName;

        const photo96:String = userInfo.getPhotoUrl(96, 96);
        if (photo96) dto.photo96 = photo96;

        const photo256:String = userInfo.getPhotoUrl(256, 256);
        if (photo256) dto.photo256 = photo256;

        return dto;
    }
}
}
