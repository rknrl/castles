package ru.rknrl.castles.controller.mock {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.Dictionary;
import flash.utils.setTimeout;

import ru.rknrl.loaders.BitmapLoader;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.loaders.ILoader;
import ru.rknrl.loaders.ParallelLoader;

public class LoadImageManagerMock extends EventDispatcher implements ILoadImageManager {
    private static const urls:Vector.<String> = new <String>[
        "mock_avatars/1.png",
        "mock_avatars/2.png",
        "mock_avatars/3.png",
        "mock_avatars/4.png",
        "mock_avatars/5.png"
    ];
    private var loader:ParallelLoader;
    private const data:Dictionary = new Dictionary();

    private var delay:int;
    private var error:Boolean;

    public function LoadImageManagerMock(delay:int, error:Boolean = false) {
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
        for (var i:int = 0; i < urls.length; i++) {
            const key:String = (i + 1).toString();
            data[key] = loader.data[urls[i]];
        }
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
                callback(url, data[url]);
            }, delay);
        } else {
            callback(url, data[url]);
        }
    }
}
}
