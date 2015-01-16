package ru.rknrl.castles.view.menu.navigate.points {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.MouseEvent;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.navigate.*;

public class NavigationPoint extends Sprite {
    private static const mouseHolderSize:Number = Layout.navigationPointSize + Layout.navigationPointGap;

    public function NavigationPoint(screen:Screen) {
        mouseChildren = false;

        const mouseHolder:Bitmap = new Bitmap(Colors.transparent);
        mouseHolder.width = mouseHolder.height = mouseHolderSize;
        mouseHolder.x = mouseHolder.y = -mouseHolderSize / 2;
        addChild(mouseHolder);

        _screen = screen;
        redraw();
        addEventListener(MouseEvent.MOUSE_DOWN, onClick);
        addEventListener(MouseEvent.ROLL_OVER, onRollOver);
        addEventListener(MouseEvent.ROLL_OUT, onRollOut);
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

    private var _rollOver:Boolean;

    private function onRollOver(event:MouseEvent):void {
        _rollOver = true;
        redraw();
    }

    private function onRollOut(event:MouseEvent):void {
        _rollOver = false;
        redraw();
    }

    private function redraw():void {
        graphics.clear();
        graphics.beginFill(color);
        graphics.drawCircle(0, 0, Layout.navigationPointSize / 2);
        graphics.endFill();
    }

    private function get color():uint {
        if (_rollOver) return 0xeeeeee;
        return _selected ? Colors.darkGrey : Colors.grey;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new ChangeScreenEvent(_screen));
    }
}
}
