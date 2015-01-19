package ru.rknrl.castles.view.utils.tutor.commands {
import flash.events.Event;
import flash.events.EventDispatcher;

public class Exec extends EventDispatcher implements ITutorCommand {
    private var func:Function;

    public function Exec(func:Function) {
        this.func = func;
    }

    public function execute():void {
        func();
        dispatchEvent(new Event(Event.COMPLETE));
    }

    public function enterFrame():void {
    }
}
}
