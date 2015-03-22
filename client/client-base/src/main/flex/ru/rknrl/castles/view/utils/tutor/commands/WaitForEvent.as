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

public class WaitForEvent extends EventDispatcher implements ITutorCommand {
    private var dispatcher:EventDispatcher;
    private var eventName:String;

    public function WaitForEvent(dispatcher:EventDispatcher, eventName:String) {
        this.dispatcher = dispatcher;
        this.eventName = eventName;
    }

    public function execute():void {
        dispatcher.addEventListener(eventName, onEvent);
    }

    private function onEvent(event:Event):void {
        dispatcher.removeEventListener(eventName, onEvent);

        dispatchEvent(new Event(Event.COMPLETE));
    }

    public function enterFrame():void {
    }
}
}
