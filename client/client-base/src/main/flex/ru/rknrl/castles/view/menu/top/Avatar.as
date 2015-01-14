package ru.rknrl.castles.view.menu.top {
import flash.display.BitmapData;
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.BitmapUtils;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.utils.centerize;

public class Avatar extends Sprite {
    private var loadImageManager:ILoadImageManager;
    private var size:int;
    private var loading:LockView;
    private var defaultAvatar:DefaultAvatarMC;
    private var shape:Shape;

    public function Avatar(url:String, size:int, bitmapDataScale:Number, loadImageManager:ILoadImageManager, color:uint) {
        this.loadImageManager = loadImageManager;
        this.size = size;
        _bitmapDataScale = bitmapDataScale;

        addChild(defaultAvatar = new DefaultAvatarMC());
        defaultAvatar.width = defaultAvatar.height = size;
        defaultAvatar.transform.colorTransform = Colors.transform(color);

        addChild(loading = new LockView());
        loading.scaleX = loading.scaleY = size / Layout.itemSize;
        loading.visible = true;

        loadImageManager.load(url, onBitmapDataLoad);
    }

    private var bitmapData:BitmapData;

    private function onBitmapDataLoad(url:String, bitmapData:BitmapData):void {
        this.bitmapData = bitmapData;
        loading.visible = false;
        if (bitmapData) {
            defaultAvatar.visible = false;
            updateShape();
        }
    }

    private var _bitmapDataScale:Number;

    public function set bitmapDataScale(value:Number):void {
        _bitmapDataScale = value;
        if (!defaultAvatar.visible) updateShape();
    }

    private function updateShape():void {
        if (shape) removeChild(shape);
        shape = BitmapUtils.createCircleShape(BitmapUtils.square(bitmapData, size * _bitmapDataScale));
        shape.width = shape.height = size;
        addChild(shape);
        centerize(shape);
    }
}
}
