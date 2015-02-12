package ru.rknrl.castles.model.userInfo {
import ru.rknrl.dto.PlayerDTO;
import ru.rknrl.dto.PlayerIdDTO;

public class PlayerInfo {
    private var _playerId:PlayerIdDTO;

    public function get playerId():PlayerIdDTO {
        return _playerId;
    }

    private var _info:CastlesUserInfo;

    public function get info():CastlesUserInfo {
        return _info;
    }

    public function PlayerInfo(playerId:PlayerIdDTO, info:CastlesUserInfo) {
        _playerId = playerId;
        _info = info;
    }

    public static function fromDto(dto:PlayerDTO):PlayerInfo {
        return new PlayerInfo(dto.id, CastlesUserInfo.fromDto(dto.info));
    }

    public static function fromDtoVector(vector:Vector.<PlayerDTO>):Vector.<PlayerInfo> {
        const result:Vector.<PlayerInfo> = new <PlayerInfo>[];
        for each(var dto:PlayerDTO in vector) {
            result.push(fromDto(dto))
        }
        return result;
    }
}
}
