package ru.rknrl.castles.view.menu.navigate.points {
import flash.display.Sprite;
import flash.events.MouseEvent;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.navigate.*;

public class NavigationPoint extends Sprite {
    public function NavigationPoint(screen:Screen) {
        _screen = screen;
        redraw();
        addEventListener(MouseEvent.CLICK, onClick);
    }

    private var _screen:Screen;

    public function get screen():Screen {
        return _screen;
    }

    private var _selected:Boolean;

    public function set selected(value:Boolean):void {
        _selected = value;
        redraw();
    }

    private function redraw():void {
        graphics.clear();
        graphics.beginFill(_selected ? Colors.navigationPointSelected : Colors.navigationPoint);
        graphics.drawCircle(0, 0, Layout.navigationPointSize / 2);
        graphics.endFill();
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new ChangeScreenEvent(_screen));
    }
}
}
