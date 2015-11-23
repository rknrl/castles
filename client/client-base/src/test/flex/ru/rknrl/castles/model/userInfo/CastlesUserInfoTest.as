//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.userInfo {
import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertNull;

import protos.AccountType;
import protos.UserInfo;

import ru.rknrl.castles.model.DtoMock;

public class CastlesUserInfoTest {

    private static function checkEquals(a:UserInfo, b:UserInfo):void {
        assertEquals(a.accountId.accountType, b.accountId.accountType);
        assertEquals(a.accountId.id, b.accountId.id);
        assertEquals(a.firstName, b.firstName);
        assertEquals(a.lastName, b.lastName);
        assertEquals(a.photo96, b.photo96);
        assertEquals(a.photo256, b.photo256);
    }

    [Test("full info")]
    public function t0():void {
        const dto:UserInfo = new UserInfo();
        dto.accountId = DtoMock.accountId(AccountType.FACEBOOK, "123");
        dto.firstName = "Tolya";
        dto.lastName = "Yanot";
        dto.photo96 = "http://small_photo";
        dto.photo256 = "http://big_photo";

        const userInfo:CastlesUserInfo = CastlesUserInfo.fromDto(dto);
        assertEquals(AccountType.FACEBOOK, userInfo.accountType);
        assertEquals("123", userInfo.uid);
        assertEquals("Tolya", userInfo.firstName);
        assertEquals("Yanot", userInfo.lastName);
        assertEquals("http://small_photo", userInfo.getPhotoUrl(96, 96));
        assertEquals("http://big_photo", userInfo.getPhotoUrl(256, 256));

        const newDto:UserInfo = CastlesUserInfo.userInfoDto(userInfo, userInfo.accountType);
        checkEquals(dto, newDto);
    }

    [Test("empty info")]
    public function t1():void {
        const dto:UserInfo = new UserInfo();
        dto.accountId = DtoMock.accountId(AccountType.FACEBOOK, "123");

        const userInfo:CastlesUserInfo = CastlesUserInfo.fromDto(dto);
        assertEquals(AccountType.FACEBOOK, userInfo.accountType);
        assertEquals("123", userInfo.uid);
        assertNull(userInfo.firstName);
        assertNull(userInfo.lastName);
        assertNull(userInfo.getPhotoUrl(96, 96));
        assertNull(userInfo.getPhotoUrl(256, 256));

        const newDto:UserInfo = CastlesUserInfo.userInfoDto(userInfo, userInfo.accountType);
        checkEquals(dto, newDto);
    }
}
}
