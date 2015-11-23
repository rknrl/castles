//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model {
import com.netease.protobuf.Bool;

import protos.TutorState;

public class MutableTutorState {
    public var slot:Bool;
    public var emptySlot:Bool;
    public var navigate:Bool;
    public var magicItem:Bool;
    public var skills:Bool;

    public static function fromDto(dto:TutorState):MutableTutorState {
        const state:MutableTutorState = new MutableTutorState();
        state.slot = dto.getSlot();
        state.emptySlot = dto.getEmptySlot();
        state.navigate = dto.getNavigate();
        state.magicItem = dto.getMagicItem();
        state.skills = dto.getSkills();
        return state;
    }

    public function toDto():TutorState {
        return new TutorState(
                slot,
                emptySlot,
                navigate,
                magicItem,
                skills
        )
    }
}
}
