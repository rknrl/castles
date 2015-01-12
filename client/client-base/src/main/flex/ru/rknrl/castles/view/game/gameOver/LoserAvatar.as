package ru.rknrl.castles.view.game.gameOver {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.MovieClip;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.loaders.ILoadImageManager;

public class LoserAvatar extends Sprite {
    private var loading:LockView;
    private var defaultAvatar:DefaultAvatarMC;
    private var movieClip:MovieClip;
    private var bitmapData:BitmapData;
    private var color:uint;

    public function LoserAvatar(photoUrl:String, loadImageManager:ILoadImageManager, color:uint) {
        this.color = color;

        addChild(defaultAvatar = new DefaultAvatarMC());
        defaultAvatar.width = defaultAvatar.height = Layout.itemSize;
        defaultAvatar.transform.colorTransform = Colors.transform(color);

        addChild(loading = new LockView());
        loading.visible = true;

        loadImageManager.load(photoUrl, onBitmapDataLoad)
    }

    private function onBitmapDataLoad(url:String, bitmapData:BitmapData):void {
        if (!bitmapData) {
            bitmapData = new DefaultAvatar();
            bitmapData.colorTransform(bitmapData.rect, Colors.transform(color));
        }
        defaultAvatar.visible = false;
        loading.visible = false;

        addChild(movieClip = new LoserMC());
        movieClip.addEventListener(Event.EXIT_FRAME, onExitFrame);
        movieClip.play();
        this.bitmapData = bitmapData;
    }

    private function onExitFrame(event:Event):void {
        for (var i:int = 0; i < movieClip.numChildren; i++) {
            const bitmap:Bitmap = movieClip.getChildAt(i) as Bitmap;
            if (bitmap) bitmap.bitmapData = bitmapData;
        }

        if (movieClip.currentFrame == movieClip.totalFrames) {
            movieClip.removeEventListener(Event.EXIT_FRAME, onExitFrame);
            movieClip.stop();
        }
    }
}
}
