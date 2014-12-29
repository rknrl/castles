package ru.rknrl.castles.view.utils {
public class LoadImageManager {
    public function load(url:String, callback:Function):void {
        switch (url) {
            case "1":
                callback(url, new BitmapData1());
                break;
            case "2":
                callback(url, new BitmapData2());
                break;
            case "3":
                callback(url, new BitmapData3());
                break;
            case "4":
                callback(url, new BitmapData4());
                break;
            case "5":
                callback(url, new BitmapData5());
                break;
        }
    }
}
}
