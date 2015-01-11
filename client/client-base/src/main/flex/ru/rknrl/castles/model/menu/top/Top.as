package ru.rknrl.castles.model.menu.top {
import ru.rknrl.castles.model.userInfo.TopUserInfo;
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
        throw new Error("can't find userInfo for place " + place);
    }
}
}
