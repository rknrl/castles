package ru.rknrl.castles.controller.mock {
import flash.utils.setTimeout;

import ru.rknrl.loaders.ILoadImageManager;

public class LoadImageManagerMock implements ILoadImageManager {
    private var delay:int;
    private var error:Boolean;

    public function LoadImageManagerMock(delay:int, error:Boolean = false) {
        this.delay = delay;
        this.error = error;
    }

    public function load(url:String, callback:Function):void {
        if (error) {
            if (delay) {
                setTimeout(function ():void {
                    callback(url, null);
                }, delay);
            } else {
                callback(url, null);
            }
            return;
        }

        switch (url) {
            case "1":
                if (delay) {
                    setTimeout(function ():void {
                        callback(url, new BitmapData1());
                    }, delay);
                } else {
                    callback(url, new BitmapData1());
                }
                break;
            case "2":
                if (delay) {
                    setTimeout(function ():void {
                        callback(url, new BitmapData2());
                    }, delay);
                } else {
                    callback(url, new BitmapData2());
                }
                break;
            case "3":
                if (delay) {
                    setTimeout(function ():void {
                        callback(url, new BitmapData3());
                    }, delay);
                } else {
                    callback(url, new BitmapData3());
                }
                break;
            case "4":
                if (delay) {
                    setTimeout(function ():void {
                        callback(url, new BitmapData4());
                    }, delay);
                } else {
                    callback(url, new BitmapData4());
                }
                break;
            case "5":
                if (delay) {
                    setTimeout(function ():void {
                        callback(url, new BitmapData5());
                    }, delay);
                } else {
                    callback(url, new BitmapData5());
                }
                break;
        }
    }
}
}
