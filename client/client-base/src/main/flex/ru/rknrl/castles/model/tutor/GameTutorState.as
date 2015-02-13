//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.tutor {
public class GameTutorState {
    public var arrow:Boolean;
    public var arrows:Boolean;
    public var fireball:Boolean;
    public var volcano:Boolean;
    public var tornado:Boolean;
    public var assistance:Boolean;
    public var strengthened:Boolean;

    public static function parse(data:Object):GameTutorState {
        const tutorState:GameTutorState = new GameTutorState();
        tutorState.arrow = data["arrow"];
        tutorState.arrows = data["arrows"];
        tutorState.fireball = data["fireball"];
        tutorState.volcano = data["volcano"];
        tutorState.tornado = data["tornado"];
        tutorState.assistance = data["assistance"];
        tutorState.strengthened = data["strengthened"];
        return tutorState;
    }

    public static function write(tutorState:GameTutorState):Object {
        return {
            arrow: tutorState.arrow,
            arrows: tutorState.arrows,
            fireball: tutorState.fireball,
            volcano: tutorState.volcano,
            tornado: tutorState.tornado,
            assistance: tutorState.assistance,
            strengthened: tutorState.strengthened
        };
    }
}
}
