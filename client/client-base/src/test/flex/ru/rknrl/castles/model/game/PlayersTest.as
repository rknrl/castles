//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertFalse;
import org.flexunit.asserts.assertTrue;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.core.kit.assertVectors;
import ru.rknrl.dto.PlayerId;

public class PlayersTest {
    private const players2:Players = new Players(DtoMock.playerInfosPortrait(), DtoMock.playerId(0));
    private const players4:Players = new Players(DtoMock.playerInfosLandscape(), DtoMock.playerId(0));

    [Test("isBigGame")]
    public function t0():void {
        assertFalse(players2.isBigGame);
        assertTrue(players4.isBigGame);
    }

    [Test("getPlayer")]
    public function t1():void {
        assertEquals(DtoMock.playerInfo1, players4.getPlayer(DtoMock.playerId(1)));
        assertEquals(DtoMock.playerInfo2, players4.getPlayer(DtoMock.playerId(2)));
    }

    [Test("get unknown player", expects="Error")]
    public function t2():void {
        players2.getPlayer(DtoMock.playerId(3));
    }

    [Test("getSelfPlayer")]
    public function t3():void {
        assertEquals(DtoMock.playerInfo0, players4.getSelfPlayer());
    }

    [Test("getEnemiesPlayers")]
    public function t4():void {
        assertVectors(new <*>[DtoMock.playerInfo1, DtoMock.playerInfo2, DtoMock.playerInfo3], Vector.<*>(players4.getEnemiesPlayers()));
    }

    [Test("getAll")]
    public function t5():void {
        assertVectors(new <*>[DtoMock.playerInfo0, DtoMock.playerInfo1, DtoMock.playerInfo2, DtoMock.playerInfo3], Vector.<*>(players4.getAll()));
    }

    [Test("playersToIds2")]
    public function t6():void {
        const ids:Vector.<PlayerId> = Players.playersToIds(players2.getAll());
        assertEquals(2, ids.length);
        assertEquals(0, ids[0].id);
        assertEquals(1, ids[1].id);
    }

    [Test("playersToIds3")]
    public function t7():void {
        const ids:Vector.<PlayerId> = Players.playersToIds(players4.getEnemiesPlayers());
        assertEquals(3, ids.length);
        assertEquals(1, ids[0].id);
        assertEquals(2, ids[1].id);
        assertEquals(3, ids[2].id);
    }
}
}
