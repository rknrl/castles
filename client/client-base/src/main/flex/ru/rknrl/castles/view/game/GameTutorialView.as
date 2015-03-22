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

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.game.area.TornadoPathView;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.AnimatedTextField;
import ru.rknrl.castles.view.utils.tutor.TutorialView;
import ru.rknrl.dto.ItemType;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;
import ru.rknrl.loaders.ILoadImageManager;

public class GameTutorialView extends TutorialView {
    public var arrows:ArrowsView;
    private var tornadoPath:TornadoPathView;
    private var locale:CastlesLocale;
    private var loadImageManager:ILoadImageManager;

    public function GameTutorialView(layout:Layout, locale:CastlesLocale, deviceFactory:DeviceFactory, loadImageManager:ILoadImageManager) {
        super(layout, deviceFactory);
        this.locale = locale;
        this.loadImageManager = loadImageManager;

        addChild(arrows = new ArrowsView());
        arrows.transform.colorTransform = Colors.tutorTransform;

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

    public function addButton(text:String):void {
        const textField:AnimatedTextField = new AnimatedTextField(Fonts.play);
        textField.text = text;
        textField.textScale = layout.scale;
        textField.x = layout.screenCenterX;
        textField.y = layout.gameMagicItemsY;
        textField.bounce();
        itemsLayer.addChild(textField);
    }

    public static function indexOf(itemType:ItemType):int {
        return ItemType.values.indexOf(itemType);
    }

    private var _areaPos:Point;

    public function setAreaRect(areaPos:Point, h:int, v:int):void {
        _areaPos = areaPos;
        arrows.scaleX = arrows.scaleY = layout.scale;
        tornadoPath.scaleX = tornadoPath.scaleY = layout.scale;
        arrows.x = tornadoPath.x = _areaPos.x;
        arrows.y = tornadoPath.y = _areaPos.y;
    }

    public function toGlobal(buildingPos:Point):Point {
        return new Point(_areaPos.x + buildingPos.x * layout.scale, _areaPos.y + buildingPos.y * layout.scale)
    }

    public function toGlobalPoints(points:Vector.<Point>):Vector.<Point> {
        const result:Vector.<Point> = new <Point>[];
        for each(var point:Point in points) result.push(toGlobal(point));
        return result;
    }

    private var tornado:Boolean;
    private var tornadoStartTime:int;
    public var tornadoPoints:Points;

    public function addTornadoPath():void {
        tornadoStartTime = getTimer();
        tornado = true;
    }

    public function removeTornadoPath():void {
        tornado = false;
        tornadoPath.clear();
    }

    private static const easer:IEaser = new Linear(0, 1);

    private function onEnterFrame(event:Event):void {
        arrows.orientArrows(new Point((cursor.x - _areaPos.x) / layout.scale, (cursor.y - _areaPos.y) / layout.scale));
        if (tornado) {
            const progress:Number = interpolate(0, 1, getTimer(), tornadoStartTime, 500, easer);
            tornadoPath.drawPath(tornadoPoints, tornadoPoints.totalDistance * progress);
        }
    }

    override public function clear():void {
        arrows.removeArrows();
        tornado = false;
        tornadoPath.clear();
        super.clear();
    }
}
}
