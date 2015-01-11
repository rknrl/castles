package ru.rknrl.castles.view.menu.top {
import flash.display.BitmapData;
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.castles.view.utils.BitmapUtils;
import ru.rknrl.castles.view.utils.centerize;
import ru.rknrl.loaders.ILoadImageManager;

public class Avatar extends Sprite {
    private var url:String;
    private var loadImageManager:ILoadImageManager;
    private var size:int;

    private var shape:Shape;

    public function Avatar(url:String, size:int, bitmapDataScale:Number, loadImageManager:ILoadImageManager) {
        this.url = url;
        this.loadImageManager = loadImageManager;
        this.size = size;
        _bitmapDataScale = bitmapDataScale;
        loadImageManager.load(url, onBitmapDataLoad);
    }

    private var bitmapData:BitmapData;

    private function onBitmapDataLoad(url:String, bitmapData:BitmapData):void {
        if (this.url == url) {
            this.bitmapData = bitmapData;
            updateShape();
        }
    }

    private var _bitmapDataScale:Number;

    public function set bitmapDataScale(value:Number):void {
        _bitmapDataScale = value;
        updateShape();
    }

    private function updateShape():void {
        if (!bitmapData) return;

        if (shape) removeChild(shape);
        shape = BitmapUtils.createCircleShape(BitmapUtils.square(bitmapData, size * _bitmapDataScale));
        shape.width = shape.height = size;
        addChild(shape);
        centerize(shape);
    }
}
}
