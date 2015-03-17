//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
public class FirstGameTutorState {
    /** Это твои домики, У тебя 3 противника */
    public var intro:Boolean;

    /** Отправляй отряды и захватывай чужие домики */
    public var arrowSended:Boolean;
    public var arrowCapture:Boolean;

    /** Можно отправлять сразу из нескольких домиков */
    public var arrowsSended:Boolean;
    public var arrowsCapture:Boolean;

    /** Захвати все домики противников, чтобы выиграть */
    public var win:Boolean;

    public static function empty():FirstGameTutorState {
        return new FirstGameTutorState();
    }

    public static function completed():FirstGameTutorState {
        const state:FirstGameTutorState = new FirstGameTutorState();
        state.intro = true;
        state.arrowSended = true;
        state.arrowCapture = true;
        state.arrowsSended = true;
        state.arrowsCapture = true;
        state.win = true;
        return state;
    }
}
}
