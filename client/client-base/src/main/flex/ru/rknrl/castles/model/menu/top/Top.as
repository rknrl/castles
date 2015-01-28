package ru.rknrl.castles.model.menu.top {
import ru.rknrl.castles.model.userInfo.CastlesUserInfo;
import ru.rknrl.castles.model.userInfo.TopUserInfo;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.TopUserInfoDTO;

public class Top {
    private var dto:Vector.<TopUserInfoDTO>;

    public function Top(dto:Vector.<TopUserInfoDTO>) {
        this.dto = dto;
    }

    public function getPlace(place:int):TopUserInfo {
        for each(var userInfo:TopUserInfoDTO in dto) {
            if (userInfo.place == place) return TopUserInfo.fromDto(userInfo);
        }
        return new TopUserInfo(place, new CastlesUserInfo(place.toString(), AccountType.DEV, "Somebody", null, null, null));
    }
}
}
