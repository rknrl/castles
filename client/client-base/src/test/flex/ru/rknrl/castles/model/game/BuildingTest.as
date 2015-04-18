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

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class BuildingTest {

    [Test("fromDto & update")]
    public function t0():void {
        const dto:BuildingDTO = new BuildingDTO();
        dto.id = DtoMock.buildingId(1);
        dto.building = DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2);
        dto.pos = DtoMock.point(20, 30);
        dto.population = 77;
        dto.strengthened = true;

        const building:Building = Building.fromDto(dto);
        assertEquals(1, building.id.id);
        assertEquals(BuildingType.CHURCH, building.buildingType);
        assertEquals(BuildingLevel.LEVEL_2, building.buildingLevel);
        assertEquals(20, building.pos.x);
        assertEquals(30, building.pos.y);
        assertFalse(building.owner.hasOwner);
        assertEquals(true, building.strengthened);

        building.update(new BuildingOwner(true, DtoMock.playerId(3)), 33, false);
        assertEquals(3, building.owner.ownerId.id);
        assertEquals(33, building.population);
        assertEquals(false, building.strengthened);
    }
}
}
