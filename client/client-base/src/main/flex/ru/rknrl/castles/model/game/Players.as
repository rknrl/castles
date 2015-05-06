//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import ru.rknrl.dto.PlayerDTO;
import ru.rknrl.dto.PlayerId;

public class Players {
    private var players:Vector.<PlayerDTO>;

    public function Players(players:Vector.<PlayerDTO>, selfId:PlayerId) {
        this.players = players;
        _selfId = selfId;
    }

    private var _selfId:PlayerId;

    public function get selfId():PlayerId {
        return _selfId;
    }

    public function get isBigGame():Boolean {
        return players.length == 4;
    }

    public function getPlayer(playerId:PlayerId):PlayerDTO {
        for each(var player:PlayerDTO in players) {
            if (player.id.id == playerId.id) return player;
        }
        throw new Error("can't find player " + playerId.id);
    }

    public function getSelfPlayer():PlayerDTO {
        return getPlayer(_selfId);
    }

    public function getPlayersWithout(playerId:PlayerId):Vector.<PlayerDTO> {
        const result:Vector.<PlayerDTO> = new <PlayerDTO>[];
        for each(var player:PlayerDTO in players) {
            if (player.id.id != playerId.id) result.push(player);
        }
        return result;
    }

    public function getEnemiesPlayers():Vector.<PlayerDTO> {
        return getPlayersWithout(selfId);
    }

    public function getAll():Vector.<PlayerDTO> {
        return players;
    }

    public static function playersToIds(players:Vector.<PlayerDTO>):Vector.<PlayerId> {
        const result:Vector.<PlayerId> = new <PlayerId>[];
        for each(var player:PlayerDTO in players) result.push(player.id);
        return result;
    }
}
}
