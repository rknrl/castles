package ru.rknrl.castles {

import flash.display.Sprite;
import flash.display.StageAlign;
import flash.display.StageQuality;
import flash.display.StageScaleMode;
import flash.events.Event;

import ru.rknrl.castles.controller.mock.ControllerMock;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.utils.LoadImageManager;

[SWF(width="1024", height="768", frameRate="60", backgroundColor="#ffffff")]
public class MockLandscape extends Sprite {
    private var view:View;

    public function MockLandscape() {
        stage.scaleMode = StageScaleMode.NO_SCALE;
        stage.align = StageAlign.TOP_LEFT;
        stage.quality = StageQuality.BEST;

        const layout:Layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, stage.contentsScaleFactor);

        addChild(view = new View(layout, new CastlesLocale(""), new LoadImageManager()));

        new ControllerMock(view);

        stage.addEventListener(Event.RESIZE, onResize);
    }

    private function onResize(event:Event):void {
        view.layout = new LayoutLandscape(stage.stageWidth, stage.stageHeight, stage.contentsScaleFactor);
    }
}
}
