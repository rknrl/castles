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

[Event(name="complete", type="flash.events.Event")]
public class TutorParallelCommands extends EventDispatcher implements ITutorCommand {
    private var commands:Vector.<ITutorCommand>;
    private var running:Boolean;
    private var completed:int;

    public function TutorParallelCommands(commands:Vector.<ITutorCommand>) {
        this.commands = commands;
    }

    public function execute():void {
        if (running) throw new Error("already in running");
        running = true;
        completed = 0;

        for each(var command:ITutorCommand in commands) {
            command.addEventListener(Event.COMPLETE, onComplete);
            command.execute();
        }
    }

    private function onComplete(event:Event):void {
        const command:ITutorCommand = ITutorCommand(event.target);
        command.removeEventListener(Event.COMPLETE, onComplete);

        completed++;

        if (completed == commands.length) {
            running = false;
            dispatchEvent(new Event(Event.COMPLETE));
        }
    }

    public function enterFrame():void {
        if (running) {
            for each(var command:ITutorCommand in commands) {
                command.enterFrame();
            }
        }
    }
}
}
