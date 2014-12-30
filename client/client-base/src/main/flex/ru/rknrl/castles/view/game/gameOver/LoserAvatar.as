package ru.rknrl.castles.view.game.gameOver {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.MovieClip;
import flash.display.Sprite;
import flash.events.Event;

public class LoserAvatar extends Sprite {
    private var movieClip:MovieClip;

    private var _bitmapData:BitmapData;

    public function set bitmapData(value:BitmapData):void {
        if (_bitmapData == null) {
            addChild(movieClip = new LoserMC());
            movieClip.addEventListener(Event.EXIT_FRAME, onExitFrame);
            movieClip.play();
        }
        _bitmapData = value;
    }

    private function onExitFrame(event:Event):void {
        for (var i:int = 0; i < movieClip.numChildren; i++) {
            const bitmap:Bitmap = movieClip.getChildAt(i) as Bitmap;
            if (bitmap) bitmap.bitmapData = _bitmapData;
        }

        if (movieClip.currentFrame == movieClip.totalFrames) {
            movieClip.removeEventListener(Event.EXIT_FRAME, onExitFrame);
            movieClip.stop();
        }
    }
}
}
