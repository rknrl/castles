//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.userInfo {
import org.flexunit.asserts.assertEquals;

import protos.AccountType;
import protos.Player;
import protos.UserInfo;

import ru.rknrl.castles.model.DtoMock;

public class PlayerInfoTest {
    [Test("fromDto")]
    public function t0():void {
        const userInfoDto:UserInfo = new UserInfo();
        userInfoDto.accountId = DtoMock.accountId(AccountType.FACEBOOK, "123");
        userInfoDto.firstName = "Tolya";
        userInfoDto.lastName = "Yanot";
        userInfoDto.photo96 = "http://small_photo";
        userInfoDto.photo256 = "http://big_photo";

        const playerDto:Player = new Player();
        playerDto.id = DtoMock.playerId(7);
        playerDto.info = userInfoDto;

        const playerInfo:PlayerInfo = PlayerInfo.fromDto(playerDto);
        assertEquals(7, playerInfo.playerId.id);

        const userInfo:CastlesUserInfo = playerInfo.info;
        assertEquals(AccountType.FACEBOOK, userInfo.accountType);
        assertEquals("123", userInfo.uid);
        assertEquals("Tolya", userInfo.firstName);
        assertEquals("Yanot", userInfo.lastName);
        assertEquals("http://small_photo", userInfo.getPhotoUrl(96, 96));
        assertEquals("http://big_photo", userInfo.getPhotoUrl(256, 256));
    }

    [Test("fromDtoVector")]
    public function t1():void {
        const userInfoDto1:UserInfo = new UserInfo();
        userInfoDto1.accountId = DtoMock.accountId(AccountType.FACEBOOK, "0");

        const player1:Player = new Player();
        player1.id = DtoMock.playerId(0);
        player1.info = userInfoDto1;

        const userInfoDto2:UserInfo = new UserInfo();
        userInfoDto2.accountId = DtoMock.accountId(AccountType.VKONTAKTE, "1");

        const player2:Player = new Player();
        player2.id = DtoMock.playerId(1);
        player2.info = userInfoDto2;

        const playerInfos:Vector.<PlayerInfo> = PlayerInfo.fromDtoVector(new <Player>[player1, player2]);
        assertEquals(0, playerInfos[0].playerId.id);
        assertEquals(1, playerInfos[1].playerId.id);
    }
}
}
