//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.menu.main {
import org.flexunit.asserts.assertEquals;
import org.flexunit.asserts.assertFalse;
import org.flexunit.asserts.assertNull;
import org.flexunit.asserts.assertTrue;

import protos.BuildingLevel;
import protos.BuildingPrototype;
import protos.BuildingType;
import protos.Slot;
import protos.SlotId;

import ru.rknrl.castles.model.DtoMock;

public class SlotsTest {
    private const slot1:Slot = DtoMock.slot(SlotId.SLOT_1, DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2));
    private const slot2:Slot = DtoMock.slot(SlotId.SLOT_2, null);
    private const slot3:Slot = DtoMock.slot(SlotId.SLOT_3, DtoMock.buildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1));
    private const slot4:Slot = DtoMock.slot(SlotId.SLOT_4, null);
    private const slot5:Slot = DtoMock.slot(SlotId.SLOT_5, DtoMock.buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1));

    private const slots:Vector.<Slot> = new <Slot>[slot1, slot2, slot3, slot4, slot5];

    [Test("buildingsCount")]
    public function t0():void {
        assertEquals(3, new Slots(slots).buildingsCount)
    }

    [Test("getSlot")]
    public function t1():void {
        const s:Slots = new Slots(slots);
        assertEquals(slot1, s.getSlot(SlotId.SLOT_1));
        assertEquals(slot2, s.getSlot(SlotId.SLOT_2));
        assertEquals(slot3, s.getSlot(SlotId.SLOT_3));
    }

    [Test("getSlot invalid", expects="Error")]
    public function t2():void {
        new Slots(new <Slot>[]).getSlot(SlotId.SLOT_1);
    }

    [Test("getEmptySlot")]
    public function t3():void {
        assertEquals(SlotId.SLOT_2, new Slots(slots).getEmptySlot());
    }

    [Test("getEmptySlot in full slots")]
    public function t3a():void {
        const prototype:BuildingPrototype = DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2);

        const slots:Vector.<Slot> = new <Slot>[
            DtoMock.slot(SlotId.SLOT_1, prototype),
            DtoMock.slot(SlotId.SLOT_2, prototype),
            DtoMock.slot(SlotId.SLOT_3, prototype),
            DtoMock.slot(SlotId.SLOT_4, prototype),
            DtoMock.slot(SlotId.SLOT_5, prototype)
        ];

        assertNull(new Slots(slots).getEmptySlot());
    }

    [Test("getNotEmptySlot")]
    public function t4():void {
        assertEquals(SlotId.SLOT_1, new Slots(slots).getNotEmptySlot());
    }

    [Test('getNotEmprtSlot invalid', expects="Error")]
    public function t4a():void {
        const slots:Vector.<Slot> = new <Slot>[
            DtoMock.slot(SlotId.SLOT_1, null),
            DtoMock.slot(SlotId.SLOT_2, null),
            DtoMock.slot(SlotId.SLOT_3, null),
            DtoMock.slot(SlotId.SLOT_4, null),
            DtoMock.slot(SlotId.SLOT_5, null)
        ];

        new Slots(slots).getNotEmptySlot()
    }

    [Test("equalsPrototype")]
    public function t5():void {
        assertTrue(Slots.equalsPrototype(
                DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2),
                DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2)
        ));
        assertFalse(Slots.equalsPrototype(
                DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2),
                DtoMock.buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_2)
        ));
        assertFalse(Slots.equalsPrototype(
                DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2),
                DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1)
        ))
    }

    [Test("equals")]
    public function t6():void {
        const a:Slot = DtoMock.slot(SlotId.SLOT_1, DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1));
        const b:Slot = DtoMock.slot(SlotId.SLOT_1, DtoMock.buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_2));
        const c:Slot = DtoMock.slot(SlotId.SLOT_1, DtoMock.buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1));
        const empty:Slot = DtoMock.slot(SlotId.SLOT_1, null);

        assertTrue(Slots.equals(a, a));
        assertTrue(Slots.equals(empty, empty));

        assertFalse(Slots.equals(a, b));
        assertFalse(Slots.equals(a, c));
        assertFalse(Slots.equals(a, empty));
    }
}
}
