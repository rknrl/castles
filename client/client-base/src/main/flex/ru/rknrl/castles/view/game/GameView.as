package ru.rknrl.castles.view.game {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.view.game.area.GameArea;
import ru.rknrl.castles.view.game.ui.GameUI;
import ru.rknrl.castles.view.layout.Layout;

public class GameView extends Sprite {
    public var area:GameArea;
    public var ui:GameUI;

    public function GameView(layout:Layout) {
        addChild(area = new GameArea(8, 11));
        addChild(ui = new GameUI(layout));

        this.layout = layout;
        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
    }

    private function onAddedToStage(event:Event):void {
        removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseEvent);
        stage.addEventListener(MouseEvent.MOUSE_MOVE, onMouseEvent);
        stage.addEventListener(MouseEvent.MOUSE_UP, onMouseEvent);
        stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    public function removeListeners():void {
        stage.removeEventListener(MouseEvent.MOUSE_DOWN, onMouseEvent);
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onMouseEvent);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseEvent);
        stage.removeEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    private function onMouseEvent(event:MouseEvent):void {
        dispatchEvent(new GameMouseEvent(event.type, new Point(area.mouseX, area.mouseY)))
    }

    public function set layout(value:Layout):void {
        area.scaleX = area.scaleY = value.scale;
        area.x = value.screenCenterX - area.width / 2;

        ui.layout = value;
    }

    private function onKeyDown(event:KeyboardEvent):void {
        if (event.keyCode == Keyboard.ESCAPE) {
            dispatchEvent(new Event(GameViewEvents.SURRENDER, true));
        }
    }

    public function openGameOverScreen():void {

    }
}
}
