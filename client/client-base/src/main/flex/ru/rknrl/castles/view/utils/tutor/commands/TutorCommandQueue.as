package ru.rknrl.castles.view.utils.tutor.commands {
import flash.events.Event;
import flash.events.EventDispatcher;

[Event(name="complete", type="flash.events.Event")]
public class TutorCommandQueue extends EventDispatcher implements ITutorCommand {
    private var commands:Vector.<ITutorCommand>;
    private var running:Boolean;
    private var completed:int;

    public function TutorCommandQueue(commands:Vector.<ITutorCommand>) {
        this.commands = commands;
    }

    public function execute():void {
        if (running) throw new Error("already in running");
        running = true;
        completed = 0;

        next();
    }

    private function next():void {
        if (isComplete) {
            running = false;
            dispatchEvent(new Event(Event.COMPLETE));
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
