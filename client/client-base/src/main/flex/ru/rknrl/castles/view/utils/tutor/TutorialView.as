//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils.tutor {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.Move;

public class TutorialView extends Sprite {
    private var _itemsLayer:Sprite;

    public function get itemsLayer():Sprite {
        return _itemsLayer;
    }

    private var _cursorHalo:CursorHalo;
    private var _cursor:Sprite;

    public function get cursor():Sprite {
        return _cursor;
    }

    public function TutorialView(layout:Layout, deviceFactory:DeviceFactory) {
        mouseEnabled = mouseChildren = false;

        addChild(_itemsLayer = new Sprite());

        addChild(_cursor = new Sprite());
        _cursor.addChild(_cursorHalo = new CursorHalo());
        const cursorMC:DisplayObject = deviceFactory.cursor();
        cursorMC.alpha = 0.8;
        _cursor.addChild(cursorMC);
        _cursor.visible = false;
        _cursor.transform.colorTransform = Colors.tutorTransform;

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        this.layout = layout;
    }

    private var _layout:Layout;

    public function get layout():Layout {
        return _layout;
    }

    public function set layout(value:Layout):void {
        _layout = value;
        _cursor.scaleX = _cursor.scaleY = value.scale;
    }

    private var command:ITutorCommand;

    public function play(c:ITutorCommand):void {
        command = c;
        command.execute();
    }

    private function onEnterFrame(event:Event):void {
        if (command) command.enterFrame();
    }

    public final function get playing():Boolean {
        return command;
    }

    public function get screenCorner():Point {
        return new Point(layout.screenWidth, layout.screenHeight);
    }

    protected static const tweenDuration:int = 500;

    protected function _tween(a:Point, b:Point):ITutorCommand {
        return new Move(Points.two(a, b), _cursor, tweenDuration)
    }

    public function cursorPos(point:Point):void {
        _cursor.x = point.x;
        _cursor.y = point.y;
    }

    public function showCursor():void {
        _cursor.visible = true
    }

    public function hideCursor():void {
        _cursor.visible = false
    }

    public function click():void {
        _cursorHalo.mouseUp();
    }

    public function mouseDown():void {
        _cursorHalo.mouseDown();
    }

    public function mouseUp():void {
        _cursorHalo.mouseUp();
    }

    public function clear():void {
        while (itemsLayer.numChildren) itemsLayer.removeChildAt(0);
        _cursorHalo.clear();
        cursorPos(screenCorner);
    }
}
}
