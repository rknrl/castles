//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
import org.flexunit.asserts.assertFalse;
import org.flexunit.asserts.assertTrue;

import ru.rknrl.castles.model.DtoMock;

public class BuildingOwnerTest {
    [Test("no owner", expects="Error")]
    public function t0():void {
        const owner:BuildingOwner = new BuildingOwner(false);
        owner.ownerId
    }

    [Test("no owner equals")]
    public function t1():void {
        const owner:BuildingOwner = new BuildingOwner(false);

        assertFalse(owner.equals(new BuildingOwner(true, DtoMock.playerId(0))));
        assertTrue(owner.equals(new BuildingOwner(false)));

        assertFalse(owner.equalsId(DtoMock.playerId(0)));
        assertFalse(owner.equalsId(DtoMock.playerId(1)));
    }

    [Test("has owner equals")]
    public function t2():void {
        const owner:BuildingOwner = new BuildingOwner(true, DtoMock.playerId(1));

        assertTrue(owner.equals(new BuildingOwner(true, DtoMock.playerId(1))));
        assertFalse(owner.equals(new BuildingOwner(true, DtoMock.playerId(2))));
        assertFalse(owner.equals(new BuildingOwner(false)));

        assertTrue(owner.equalsId(DtoMock.playerId(1)));
        assertFalse(owner.equalsId(DtoMock.playerId(2)));
    }
}
}
