package ru.rknrl.castles.model.game {
import org.flexunit.asserts.assertFalse;
import org.flexunit.asserts.assertTrue;

import ru.rknrl.dto.PlayerIdDTO;

public class BuildingOwnerTest {
    private static function playerId(id:int):PlayerIdDTO {
        const dto:PlayerIdDTO = new PlayerIdDTO();
        dto.id = id;
        return dto;
    }

    [Test(description="Пустой BuildingOwner должен быть равен другому пустому BuildingOwner")]
    public function t1():void {
        const owner:BuildingOwner = new BuildingOwner(false);
        assertTrue(owner.equals(new BuildingOwner(false)))
    }

    [Test(description="Пустой BuildingOwner должен быть НЕ равен непустому BuildingOwner")]
    public function t2():void {
        const owner:BuildingOwner = new BuildingOwner(false);
        assertFalse(owner.equals(new BuildingOwner(true, playerId(0))));
        assertFalse(owner.equalsId(playerId(0)));
    }

    [Test(description="Непустой BuildingOwner должен быть равен другому BuildingOwner с таким же playerId")]
    public function t3():void {
        const owner:BuildingOwner = new BuildingOwner(true, playerId(0));
        assertTrue(owner.equals(new BuildingOwner(true, playerId(0))));
        assertTrue(owner.equalsId(playerId(0)));
    }

    [Test(description="Непустой BuildingOwner должен быть НЕ равен другому BuildingOwner с различным playerId")]
    public function t4():void {
        const owner:BuildingOwner = new BuildingOwner(true, playerId(0));
        assertFalse(owner.equals(new BuildingOwner(true, playerId(1))));
        assertFalse(owner.equalsId(playerId(2)));
    }

    [Test(description="Непустой BuildingOwner должен быть НЕ равен пустому BuildingOwner")]
    public function t5():void {
        const owner:BuildingOwner = new BuildingOwner(true, playerId(7));
        assertFalse(owner.equals(new BuildingOwner(false)))
    }
}
}
