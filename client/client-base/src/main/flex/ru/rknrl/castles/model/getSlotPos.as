//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model {
import protos.Slot1Pos;
import protos.Slot2Pos;
import protos.Slot3Pos;
import protos.Slot4Pos;
import protos.Slot5Pos;
import protos.SlotId;

import ru.rknrl.core.points.Point;

public function getSlotPos(slotId:SlotId):Point {
    switch (slotId) {
        case SlotId.SLOT_1:
            return new Point(Slot1Pos.SLOT_1_X.id, Slot1Pos.SLOT_1_Y.id);
        case SlotId.SLOT_2:
            return new Point(Slot2Pos.SLOT_2_X.id, Slot2Pos.SLOT_2_Y.id);
        case SlotId.SLOT_3:
            return new Point(Slot3Pos.SLOT_3_X.id, Slot3Pos.SLOT_3_Y.id);
        case SlotId.SLOT_4:
            return new Point(Slot4Pos.SLOT_4_X.id, Slot4Pos.SLOT_4_Y.id);
        case SlotId.SLOT_5:
            return new Point(Slot5Pos.SLOT_5_X.id, Slot5Pos.SLOT_5_Y.id);
    }
    throw new Error("unknown slotId " + slotId);
}
}
