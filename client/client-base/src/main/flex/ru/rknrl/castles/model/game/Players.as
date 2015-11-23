//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import protos.Player;
import protos.PlayerId;

public class Players {
    private var players:Vector.<Player>;

    public function Players(players:Vector.<Player>, selfId:PlayerId) {
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

    public function getPlayer(playerId:PlayerId):Player {
        for each(var player:Player in players) {
            if (player.id.id == playerId.id) return player;
        }
        throw new Error("can't find player " + playerId.id);
    }

    public function getSelfPlayer():Player {
        return getPlayer(_selfId);
    }

    public function getPlayersWithout(playerId:PlayerId):Vector.<Player> {
        const result:Vector.<Player> = new <Player>[];
        for each(var player:Player in players) {
            if (player.id.id != playerId.id) result.push(player);
        }
        return result;
    }

    public function getEnemiesPlayers():Vector.<Player> {
        return getPlayersWithout(selfId);
    }

    public function getAll():Vector.<Player> {
        return players;
    }

    public static function playersToIds(players:Vector.<Player>):Vector.<PlayerId> {
        const result:Vector.<PlayerId> = new <PlayerId>[];
        for each(var player:Player in players) result.push(player.id);
        return result;
    }
}
}
