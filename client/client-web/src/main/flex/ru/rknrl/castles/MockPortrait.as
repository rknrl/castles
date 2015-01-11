package ru.rknrl.castles {

import flash.display.Sprite;
import flash.display.StageAlign;
import flash.display.StageQuality;
import flash.display.StageScaleMode;
import flash.events.Event;

import ru.rknrl.castles.controller.mock.ControllerMock;
import ru.rknrl.castles.controller.mock.LoadImageManagerMock;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.locale.CastlesLocale;

[SWF(width="320", height="568", frameRate="60", backgroundColor="#ffffff")]
public class MockPortrait extends Sprite {
    private var view:View;

    public function MockPortrait() {
        stage.scaleMode = StageScaleMode.NO_SCALE;
        stage.align = StageAlign.TOP_LEFT;
        stage.quality = StageQuality.BEST;

        const layout:Layout = new LayoutPortrait(stage.stageWidth, stage.stageHeight, stage.contentsScaleFactor);

        addChild(view = new View(layout, new CastlesLocale(""), new LoadImageManagerMock(1000)));

        new ControllerMock(view);

        stage.addEventListener(Event.RESIZE, onResize);
    }

    private function onResize(event:Event):void {
        view.layout = new LayoutPortrait(stage.stageWidth, stage.stageHeight, stage.contentsScaleFactor);
    }
}
}
