//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.userInfo {
import protos.Player;
import protos.PlayerId;

public class PlayerInfo {
    private var _playerId:PlayerId;

    public function get playerId():PlayerId {
        return _playerId;
    }

    private var _info:CastlesUserInfo;

    public function get info():CastlesUserInfo {
        return _info;
    }

    public function PlayerInfo(playerId:PlayerId, info:CastlesUserInfo) {
        _playerId = playerId;
        _info = info;
    }

    public static function fromDto(dto:Player):PlayerInfo {
        return new PlayerInfo(dto.id, CastlesUserInfo.fromDto(dto.info));
    }

    public static function fromDtoVector(vector:Vector.<Player>):Vector.<PlayerInfo> {
        const result:Vector.<PlayerInfo> = new <PlayerInfo>[];
        for each(var dto:Player in vector) {
            result.push(fromDto(dto))
        }
        return result;
    }
}
}
