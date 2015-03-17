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
public class TutorSequenceCommands extends EventDispatcher implements ITutorCommand {
    private var commands:Vector.<ITutorCommand>;
    private var running:Boolean;
    private var completed:int;
    private var loop:Boolean;

    public function TutorSequenceCommands(commands:Vector.<ITutorCommand>, loop:Boolean) {
        this.commands = commands;
        this.loop = loop;
    }

    public function execute():void {
        if (running) throw new Error("already in running");
        running = true;
        completed = 0;

        if (loop) dispatchEvent(new Event(Event.COMPLETE));

        next();
    }

    private function next():void {
        if (isComplete) {
            if (loop) {
                completed = 0;
                executeNext();
            } else {
                running = false;
                dispatchEvent(new Event(Event.COMPLETE));
            }
        } else {
            executeNext();
        }
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
