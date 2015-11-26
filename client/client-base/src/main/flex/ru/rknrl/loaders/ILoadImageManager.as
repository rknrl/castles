//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.loaders {
public interface ILoadImageManager {
    /**
     * @param url     если равен null, то возвратит bitmapData = null
     * @param callback (url: String, bitmapData: BitmapData): void;
     *          Возвратит bitmapData = null если при загрузке произошла ошибка
     */
    function load(url:String, callback:Function):void
}
}
