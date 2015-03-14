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

[Event(name="complete", type="flash.events.Event")]
public class LoopUntilClick extends EventDispatcher implements ITutorCommand {
    private var commands:Vector.<ITutorCommand>;
    private var running:Boolean;
    private var completed:int;
    private var clickDispatcher:EventDispatcher;

    public function LoopUntilClick(commands:Vector.<ITutorCommand>, clickDispatcher:EventDispatcher) {
        this.commands = commands;
        this.clickDispatcher = clickDispatcher;
    }

    public function execute():void {
        if (running) throw new Error("already in running");
        running = true;
        completed = 0;

        clickDispatcher.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);

        next();
    }

    private function onMouseDown(event:MouseEvent):void {
        clickDispatcher.removeEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);

        dispatchEvent(new Event(Event.COMPLETE));
    }

    private function next():void {
        if (isComplete) completed = 0;
        executeNext();
    }

    private function executeNext():void {
        const command:ITutorCommand = commands[completed];
        command.addEventListener(Event.COMPLETE, onComplete);
        command.execute();
    }

    private function onComplete(event:Event):void {
        const command:ITutorCommand = ITutorCommand(event.target);
        command.removeEventListener(Event.COMPLETE, onComplete);

        completed++;
        next();
    }

    private function get isComplete():Boolean {
        return completed == commands.length;
    }

    public function enterFrame():void {
        if (running) {
            const command:ITutorCommand = commands[completed];
            command.enterFrame();
        }
    }
}
}
