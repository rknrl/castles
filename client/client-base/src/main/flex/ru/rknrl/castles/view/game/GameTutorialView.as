//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game {
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.game.area.TornadoPathView;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.AnimatedTextField;
import ru.rknrl.castles.view.utils.tutor.TutorialView;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.Move;
import ru.rknrl.core.points.Point;
import ru.rknrl.core.points.Points;
import ru.rknrl.dto.ItemType;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;
import ru.rknrl.loaders.ILoadImageManager;

public class GameTutorialView extends TutorialView {
    private var _arrows:ArrowsView;

    public function get arrows():ArrowsView {
        return _arrows;
    }

    private var _locale:CastlesLocale;

    public function get locale():CastlesLocale {
        return _locale;
    }

    private var tornadoPath:TornadoPathView;
    private var loadImageManager:ILoadImageManager;

    public function GameTutorialView(layout:Layout, locale:CastlesLocale, deviceFactory:DeviceFactory, loadImageManager:ILoadImageManager) {
        super(layout, deviceFactory);
        this._locale = locale;
        this.loadImageManager = loadImageManager;

        addChild(_arrows = new ArrowsView());
        _arrows.transform.colorTransform = Colors.tutorTransform;

        addChild(tornadoPath = new TornadoPathView());
        tornadoPath.transform.colorTransform = Colors.tutorTransform;

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function addText(text:String):void {
        const textField:AnimatedTextField = new AnimatedTextField(Fonts.title);
        textField.text = text;
        textField.textScale = layout.scale;
        textField.x = layout.screenCenterX;
        textField.y = -8; // todo
        textField.bounce();
        itemsLayer.addChild(textField);
    }

    public function addNextButton():void {
        const textField:AnimatedTextField = new AnimatedTextField(Fonts.play);
        textField.text = _locale.next;
        textField.textScale = layout.scale;
        textField.x = layout.screenCenterX;
        textField.y = layout.gameMagicItemsY;
        textField.bounce();
        itemsLayer.addChild(textField);
    }

    private var _areaPos:Point;

    public function set areaPos(value:Point):void {
        _areaPos = value;
        _arrows.scaleX = _arrows.scaleY = layout.scale;
        tornadoPath.scaleX = tornadoPath.scaleY = layout.scale;
        _arrows.x = tornadoPath.x = _areaPos.x;
        _arrows.y = tornadoPath.y = _areaPos.y;
    }

    private function toGlobal(buildingPos:Point):Point {
        return new Point(_areaPos.x + buildingPos.x * layout.scale, _areaPos.y + buildingPos.y * layout.scale)
    }

    private function toGlobalPoints(points:Vector.<Point>):Vector.<Point> {
        const result:Vector.<Point> = new <Point>[];
        for each(var point:Point in points) result.push(toGlobal(point));
        return result;
    }

    private var tornado:Boolean;
    private var tornadoStartTime:int;
    private var tornadoPoints:Points;

    public function startDrawTornado(points:Vector.<Point>):void {
        tornadoPoints = new Points(points);
        tornadoStartTime = getTimer();
        tornado = true;
    }

    public function endDrawTornado():void {
        tornado = false;
        tornadoPath.clear();
    }

    private static const easer:IEaser = new Linear(0, 1);

    private function onEnterFrame(event:Event):void {
        _arrows.orientArrows(new Point((cursor.x - _areaPos.x) / layout.scale, (cursor.y - _areaPos.y) / layout.scale));
        if (tornado) {
            const progress:Number = interpolate(0, 1, getTimer(), tornadoStartTime, 500, easer);
            tornadoPath.drawPath(tornadoPoints, progress);
        }
    }

    override public function clear():void {
        _arrows.removeArrows();
        tornado = false;
        tornadoPath.clear();
        super.clear();
    }

    public function tweenGame(a:Point, b:Point):ITutorCommand {
        return _tween(toGlobal(a), toGlobal(b));
    }

    public function tweenFromCorner(b:Point):ITutorCommand {
        return _tween(screenCorner, toGlobal(b));
    }

    public function tweenFromItem(itemType:ItemType, b:Point):ITutorCommand {
        return _tween(layout.gameMagicItem(indexOf(itemType)), toGlobal(b));
    }

    public function tweenFromCornerToItem(itemType:ItemType):ITutorCommand {
        return _tween(screenCorner, layout.gameMagicItem(indexOf(itemType)));
    }

    public function tweenPath(points:Vector.<Point>):ITutorCommand {
        return new Move(new Points(toGlobalPoints(points)), cursor, tweenDuration)
    }

    private static function indexOf(itemType:ItemType):int {
        return ItemType.values.indexOf(itemType);
    }
}
}
