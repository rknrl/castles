package ru.rknrl.castles.controller.mock {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.setTimeout;

import ru.rknrl.loaders.BitmapLoader;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.loaders.ILoader;
import ru.rknrl.loaders.ParallelLoader;

public class LoadImageManagerMock extends EventDispatcher implements ILoadImageManager {
    private var delay:int;
    private var error:Boolean;

    private var loader:ParallelLoader;

    public function LoadImageManagerMock(urls:Vector.<String>, delay:int, error:Boolean = false) {
        this.delay = delay;
        this.error = error;

        const loaders:Vector.<ILoader> = new <ILoader>[];
        for each(var url:String in urls) {
            loaders.push(new BitmapLoader(url));
        }
        loader = new ParallelLoader(loaders);
        loader.addEventListener(Event.COMPLETE, onComplete);
        loader.load();
    }

    private function onComplete(event:Event):void {
        dispatchEvent(new Event(Event.COMPLETE));
    }

    public function load(url:String, callback:Function):void {
        if (url == null) {
            callback(url, null);
        } else if (error) {
            if (delay) {
                setTimeout(function ():void {
                    callback(url, null);
                }, delay);
            } else {
                callback(url, null);
            }
        } else if (delay) {
            setTimeout(function ():void {
                callback(url, loader.data[url]);
            }, delay);
        } else {
            callback(url, loader.data[url]);
        }
    }
}
}
