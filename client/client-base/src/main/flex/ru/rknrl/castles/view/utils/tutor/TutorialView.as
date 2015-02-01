package ru.rknrl.castles.view.utils.tutor {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.commands.Exec;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.Move;
import ru.rknrl.castles.view.utils.tutor.commands.TutorCommandQueue;
import ru.rknrl.castles.view.utils.tutor.commands.Wait;

public class TutorialView extends TutorialViewBase {
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
        super(layout);

        addChild(_itemsLayer = new Sprite());

        addChild(_cursor = new Sprite());
        _cursor.addChild(_cursorHalo = new CursorHalo());
        const cursorMC:DisplayObject = deviceFactory.cursor();
        cursorMC.alpha = 0.8;
        _cursor.addChild(cursorMC);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        this.layout = layout;
    }

    override public function set layout(value:Layout):void {
        super.layout = value;
        _cursor.scaleX = _cursor.scaleY = value.scale;
    }

    private var command:ITutorCommand;

    protected function play(commands:Vector.<ITutorCommand>):void {
        if (command) throw new Error("tutor already playing");
        command = new TutorCommandQueue(commands);
        command.addEventListener(Event.COMPLETE, onTutorComplete);
        command.execute();
    }

    private function onTutorComplete(event:Event = null):void {
        command.removeEventListener(Event.COMPLETE, onTutorComplete);
        command = null;
    }

    private function onEnterFrame(event:Event):void {
        if (command) command.enterFrame();
    }

    // commands

    private static const duration:int = 500;

    protected function get screenCorner():Point {
        return new Point(layout.screenWidth, layout.screenHeight);
    }

    protected function tween(a:Point, b:Point):ITutorCommand {
        return new Move(new Points(new <Point>[a, b]), _cursor, duration)
    }

    protected function tweenPath(points:Points):ITutorCommand {
        return new Move(points, _cursor, duration)
    }

    protected function get open():ITutorCommand {
        return exec(openImpl);
    }

    protected function get close():ITutorCommand {
        return exec(closeImpl);
    }

    protected function cursorPos(point:Point):ITutorCommand {
        return exec(function ():void {
            _cursor.x = point.x;
            _cursor.y = point.y;
        });
    }

    protected function get click():ITutorCommand {
        return exec(_cursorHalo.mouseUp);
    }

    protected function get mouseDown():ITutorCommand {
        return exec(_cursorHalo.mouseDown);
    }

    protected function get mouseUp():ITutorCommand {
        return exec(_cursorHalo.mouseUp);
    }

    protected static function wait(duration:int):ITutorCommand {
        return new Wait(duration);
    }

    protected static function exec(func:Function):ITutorCommand {
        return new Exec(func);
    }
}
}
