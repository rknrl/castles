//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.userInfo {
import org.flexunit.asserts.assertEquals;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.TopUserInfoDTO;
import ru.rknrl.dto.UserInfoDTO;

public class TopUserInfoTest {
    [Test("fromDto")]
    public function t0():void {
        const userInfoDto:UserInfoDTO = new UserInfoDTO();
        userInfoDto.accountId = DtoMock.accountId(AccountType.FACEBOOK, "123");
        userInfoDto.firstName = "Tolya";
        userInfoDto.lastName = "Yanot";
        userInfoDto.photo96 = "http://small_photo";
        userInfoDto.photo256 = "http://big_photo";

        const dto:TopUserInfoDTO = new TopUserInfoDTO();
        dto.place = 7;
        dto.info = userInfoDto;

        const topUserInfo:TopUserInfo = TopUserInfo.fromDto(dto);
        assertEquals(7, topUserInfo.place);

        const userInfo:CastlesUserInfo = topUserInfo.info;
        assertEquals(AccountType.FACEBOOK, userInfo.accountType);
        assertEquals("123", userInfo.uid);
        assertEquals("Tolya", userInfo.firstName);
        assertEquals("Yanot", userInfo.lastName);
        assertEquals("http://small_photo", userInfo.getPhotoUrl(96, 96));
        assertEquals("http://big_photo", userInfo.getPhotoUrl(256, 256));
    }
}
}
