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
import flash.geom.ColorTransform;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.commands.Exec;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.InfinityWait;
import ru.rknrl.castles.view.utils.tutor.commands.Move;
import ru.rknrl.castles.view.utils.tutor.commands.TutorParallelCommands;
import ru.rknrl.castles.view.utils.tutor.commands.TutorSequenceCommands;
import ru.rknrl.castles.view.utils.tutor.commands.Wait;
import ru.rknrl.castles.view.utils.tutor.commands.WaitForClick;

public class TutorialView extends TutorialViewBase {
    public static const TUTOR_COMPLETE:String = "tutorComplete";

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
        _cursor.visible = false;
        _cursor.transform.colorTransform = new ColorTransform(0, 0, 0);

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
        command = new TutorSequenceCommands(commands, false);
        command.addEventListener(Event.COMPLETE, hide);
        command.execute();
    }

    override public function hide(event:Event = null):void {
        super.hide(event);
        clear();
        _cursor.visible = false;
        command.removeEventListener(Event.COMPLETE, hide);
        command = null;
        dispatchEvent(new Event(TUTOR_COMPLETE));
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
        return new Move(Points.two(a, b), _cursor, duration)
    }

    protected function tweenPath(points:Points):ITutorCommand {
        return new Move(points, _cursor, duration)
    }

    protected function get open():ITutorCommand {
        return exec(show);
    }

    protected function cursorPos(point:Point):ITutorCommand {
        return exec(function ():void {
            _cursor.x = point.x;
            _cursor.y = point.y;
        });
    }

    protected function get showCursor():ITutorCommand {
        return exec(function ():void {
            _cursor.visible = true
        });
    }

    protected function get hideCursor():ITutorCommand {
        return exec(function ():void {
            _cursor.visible = false
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

    protected static function parallel(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorParallelCommands(commands);
    }

    protected static function sequence(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, false);
    }

    protected static function loop(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, true);
    }

    protected function get waitForClick():ITutorCommand {
        return new WaitForClick(stage);
    }

    protected static function wait(duration:int):ITutorCommand {
        return new Wait(duration);
    }

    protected static function get infinityWait():ITutorCommand {
        return new InfinityWait();
    }

    protected static function exec(func:Function):ITutorCommand {
        return new Exec(func);
    }

    protected function clear():void {
        while (itemsLayer.numChildren) itemsLayer.removeChildAt(0)
    }
}
}
