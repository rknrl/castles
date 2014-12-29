package ru.rknrl.castles.view.menu.top {
import flash.display.BitmapData;
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.castles.view.utils.BitmapUtils;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.castles.view.utils.centerize;

public class Avatar extends Sprite {
    private var loadImageManager:LoadImageManager;
    private var size:int;

    private var shape:Shape;

    public function Avatar(url:String, size:int, bitmapDataScale:Number, loadImageManager:LoadImageManager) {
        this.loadImageManager = loadImageManager;
        this.size = size;
        _bitmapDataScale = bitmapDataScale;

        this.url = url;
    }

    private var _url:String;

    public function set url(value:String):void {
        _url = value;
        loadImageManager.load(value, onBitmapDataLoad);
    }

    private var _bitmapData:BitmapData;

    private function onBitmapDataLoad(url:String, bitmapData:BitmapData):void {
        if (_url == url) {
            _bitmapData = bitmapData;
            updateShape();
        }
    }

    private var _bitmapDataScale:Number;

    public function set bitmapDataScale(value:Number):void {
        _bitmapDataScale = value;
        updateShape();
    }

    private function updateShape():void {
        if (!_bitmapData) return;

        if (shape) removeChild(shape);
        shape = BitmapUtils.createCircleShape(BitmapUtils.square(_bitmapData, size * _bitmapDataScale));
        shape.width = shape.height = size;
        addChild(shape);
        centerize(shape);
    }
}
}
