package ru.rknrl.castles.view.loading {
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.view.layout.Layout;

public class NoConnectionScreen extends LoadingScreen {
    public function NoConnectionScreen(text:String, layout:Layout) {
        super(text, layout);
        addEventListener(MouseEvent.CLICK, onClick);
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(ViewEvents.TRY_CONNECT, true));
    }
}
}
