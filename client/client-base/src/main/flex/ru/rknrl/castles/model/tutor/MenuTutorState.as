//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.tutor {
public class MenuTutorState {
    public var slot:Boolean;
    public var emptySlot:Boolean;
    public var navigate:Boolean;
    public var magicItem:Boolean;
    public var skills:Boolean;

    public static function parse(data:Object):MenuTutorState {
        const tutorState:MenuTutorState = new MenuTutorState();
        tutorState.slot = data["slot"];
        tutorState.emptySlot = data["emptySlot"];
        tutorState.navigate = data["navigate"];
        tutorState.magicItem = data["magicItem"];
        tutorState.skills = data["skills"];
        return tutorState;
    }

    public static function write(tutorState:MenuTutorState):Object {
        return {
            slot: tutorState.slot,
            emptySlot: tutorState.emptySlot,
            navigate: tutorState.navigate,
            magicItem: tutorState.magicItem,
            skills: tutorState.skills
        };
    }
}
}
