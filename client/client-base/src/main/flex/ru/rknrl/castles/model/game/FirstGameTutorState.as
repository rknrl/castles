//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.game {
public class FirstGameTutorState {

    /** Это твои домики, Это домики противников */
    public var intro:Boolean;

    /** Отправили отряд из одного домика */
    public var arrowSended:Boolean;

    /** Захватили домик отрядом */
    public var arrowCapture:Boolean;

    /** Отправили отряд из нескольких домиков */
    public var arrowsSended:Boolean;

    /** Захватили домик несколькими отрядами */
    public var arrowsCapture:Boolean;

    /** Захвати все домики противников, чтобы выиграть */
    public var win:Boolean;

    public var fireballClick:Boolean;
    public var fireballCast:Boolean;
    public var strengtheningClick:Boolean;
    public var strengtheningCast:Boolean;
    public var volcanoClick:Boolean;
    public var volcanoCast:Boolean;
    public var tornadoClick:Boolean;
    public var tornadoCast:Boolean;
    public var assistanceClick:Boolean;
    public var assistanceCast:Boolean;

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
        state.fireballCast = true;
        state.strengtheningCast = true;
        state.volcanoCast = true;
        state.tornadoCast = true;
        state.assistanceCast = true;
        return state;
    }
}
}
