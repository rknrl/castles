//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.tutor.commands {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.getTimer;

public class Wait extends EventDispatcher implements ITutorCommand {
    private var startTime:int;
    private var duration:int;

    public function Wait(duration:int) {
        this.duration = duration;
    }

    public function execute():void {
        startTime = getTimer();
    }

    public function enterFrame():void {
        if (getTimer() - startTime > duration) {
            dispatchEvent(new Event(Event.COMPLETE));
        }
    }
}
}
