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
import flash.events.MouseEvent;

public class WaitForClick extends EventDispatcher implements ITutorCommand {
    private var dispatcher:EventDispatcher;

    public function WaitForClick(dispatcher:EventDispatcher) {
        this.dispatcher = dispatcher;
    }

    public function execute():void {
        dispatcher.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
    }

    private function onMouseDown(event:MouseEvent):void {
        dispatcher.removeEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);

        dispatchEvent(new Event(Event.COMPLETE));
    }

    public function enterFrame():void {
    }
}
}
