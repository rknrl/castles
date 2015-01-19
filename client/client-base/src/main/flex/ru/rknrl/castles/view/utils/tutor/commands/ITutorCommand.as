package ru.rknrl.castles.view.utils.tutor.commands {
import flash.events.IEventDispatcher;

[Event(name="complete", type="flash.events.Event")]
public interface ITutorCommand extends IEventDispatcher {
    function execute():void;

    function enterFrame():void;
}
}
