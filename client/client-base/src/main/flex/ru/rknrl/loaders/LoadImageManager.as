//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.loaders {
import flash.display.BitmapData;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.utils.Dictionary;

import ru.rknrl.loaders.typed.BitmapDataLoader;

/**
 * Мененджер загрузок картинок из интернета
 * Грузит последовательно
 */
public class LoadImageManager implements ILoadImageManager {
    private var urlToBitmapData:Dictionary = new Dictionary();
    private const urlToCallbacksVector:Dictionary = new Dictionary();
    private const urlToError:Dictionary = new Dictionary();

    private const loadersQueue:Vector.<BitmapDataLoader> = new <BitmapDataLoader>[];

    private var loading:Boolean;

    private var max:int;
    private var count:int;

    public function LoadImageManager(max:int) {
        this.max = max;
    }

    /**
     * @param url     если равен null, то возвратит bitmapData = null
     * @param callback (url: String, bitmapData: BitmapData): void;
     *          Возвратит bitmapData = null если при загрузке произошла ошибка
     */
    public function load(url:String, callback:Function):void {
        if (url == null) {
            callback(url, null);
            return;
        }

        if (urlToError[url]) {
            callback(url, null);
            return;
        }

        const bitmapData:BitmapData = urlToBitmapData[url];
        if (bitmapData) {
            callback(url, bitmapData);
            return;
        }

        const callbacks:Vector.<Function> = urlToCallbacksVector[url];
        if (callbacks) {
            if (callbacks.indexOf(callback) == -1) {
                callbacks.push(callback);
            }
            return;
        }

        const loader:BitmapDataLoader = new BitmapDataLoader(url);

        urlToCallbacksVector[url] = new <Function>[callback];
        loader.addEventListener(Event.COMPLETE, onComplete);
        loader.addEventListener(IOErrorEvent.IO_ERROR, onLoadFailed);
        loader.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onLoadFailed);

        loadersQueue.push(loader);

        loadNext();
    }

    private function loadNext():void {
        if (!loading && loadersQueue.length) {
            const loader:BitmapDataLoader = loadersQueue.shift();
            loader.load();
            loading = true;
        }
    }

    private function onComplete(event:Event):void {
        const loader:BitmapDataLoader = BitmapDataLoader(event.target);
        completeLoad(loader, loader.bitmapData);
    }

    private function onLoadFailed(event:Event):void {
        const loader:BitmapDataLoader = BitmapDataLoader(event.target);
        const url:String = loader.url;
        urlToError[url] = true;
        completeLoad(loader, null);
    }

    private function completeLoad(loader:BitmapDataLoader, bitmapData:BitmapData):void {
        loader.removeEventListener(Event.COMPLETE, onComplete);
        loader.removeEventListener(IOErrorEvent.IO_ERROR, onLoadFailed);
        loader.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onLoadFailed);

        const url:String = loader.url;

        const callbacks:Vector.<Function> = urlToCallbacksVector[url];
        for each(var completeFunction:Function in callbacks) {
            completeFunction(url, bitmapData);
        }
        urlToCallbacksVector[url] = null;

        count++;
        if (count > max) clear();

        urlToBitmapData[url] = bitmapData;

        loading = false;
        loadNext();
    }

    private function clear():void {
        count = 0;
        // todo: Удалять все загруженное не очень то рационально,
        // todo: ведь будут удалены и часто используемые картинки (например собственная аватарка)
        urlToBitmapData = new Dictionary();
    }
}
}
