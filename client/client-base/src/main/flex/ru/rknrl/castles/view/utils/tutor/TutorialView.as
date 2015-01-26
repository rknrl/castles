package ru.rknrl.castles.view.utils.tutor {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.castlesTest;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.commands.Exec;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.Move;
import ru.rknrl.castles.view.utils.tutor.commands.TutorCommandQueue;
import ru.rknrl.castles.view.utils.tutor.commands.Wait;

use namespace castlesTest;

public class TutorialView extends TutorialViewBase {
    private var _itemsLayer:Sprite;

    public function get itemsLayer():Sprite {
        return _itemsLayer;
    }

    private var _cursorAnimation:CursorAnimation;
    private var _cursor:Sprite;

    public function get cursor():Sprite {
        return _cursor;
    }

    public function TutorialView(layout:Layout, deviceFactory:DeviceFactory) {
        super(layout);

        addChild(_itemsLayer = new Sprite());

        addChild(_cursor = new Sprite());
        const cursorMC:DisplayObject = deviceFactory.cursor();
        cursorMC.alpha = 0.8;
        _cursor.addChild(cursorMC);
        _cursor.addChild(_cursorAnimation = new CursorAnimation());

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        this.layout = layout;
    }

    override public function set layout(value:Layout):void {
        super.layout = value;
        _cursor.scaleX = _cursor.scaleY = value.scale;
    }

    private var command:ITutorCommand;
    private var onCompleteFunc:Function;

    public function play(commands:Vector.<ITutorCommand>, onCompleteFunc:Function = null):void {
        if (this.command) throw new Error("tutor already playing");
        this.command = new TutorCommandQueue(commands);
        this.onCompleteFunc = onCompleteFunc;
        command.addEventListener(Event.COMPLETE, onTutorComplete);
        command.execute();
    }

    private function onTutorComplete(event:Event = null):void {
        if (onCompleteFunc) onCompleteFunc();
        command.removeEventListener(Event.COMPLETE, onTutorComplete);
        command = null;
    }

    override protected function closeImpl(event:Event = null):void {
        if (command) {
            onTutorComplete()
        }
        super.closeImpl(event);
    }

    private function onEnterFrame(event:Event):void {
        if (command) command.enterFrame();
    }

    private static const duration:int = 500;

    public function tween(a:Point, b:Point):ITutorCommand {
        return new Move(new Points(new <Point>[a, b]), _cursor, duration)
    }

    public function tweenPath(points:Points):ITutorCommand {
        return new Move(points, _cursor, duration)
    }

    public function get open():ITutorCommand {
        return exec(openImpl);
    }

    public function get close():ITutorCommand {
        return exec(closeImpl);
    }

    public function pos(point:Point):ITutorCommand {
        return exec(function ():void {
            _cursor.x = point.x;
            _cursor.y = point.y;
        });
    }

    protected function get click():ITutorCommand {
        return exec(_cursorAnimation.mouseUp);
    }

    protected function get mouseDown():ITutorCommand {
        return exec(_cursorAnimation.mouseDown);
    }

    protected function get mouseUp():ITutorCommand {
        return exec(_cursorAnimation.mouseUp);
    }

    public static function wait(duration:int):ITutorCommand {
        return new Wait(duration);
    }

    public static function exec(func:Function):ITutorCommand {
        return new Exec(func);
    }

    protected function get screenCorner():Point {
        return new Point(layout.screenWidth, layout.screenHeight);
    }
}
}
