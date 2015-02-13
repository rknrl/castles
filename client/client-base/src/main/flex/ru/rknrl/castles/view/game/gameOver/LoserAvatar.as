//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.gameOver {
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.MovieClip;
import flash.display.Shape;
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.BitmapUtils;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.loaders.ILoadImageManager;

public class LoserAvatar extends Sprite {
    private static const avatarSize:int = Layout.itemSize;

    private var loading:LockView;
    private var defaultAvatar:DefaultAvatarMC;
    private var movieClip:MovieClip;
    private var avatar:Sprite;
    private var color:uint;

    public function LoserAvatar(photoUrl:String, loadImageManager:ILoadImageManager, color:uint) {
        this.color = color;

        addChild(defaultAvatar = new DefaultAvatarMC());
        defaultAvatar.width = defaultAvatar.height = avatarSize;
        defaultAvatar.transform.colorTransform = Colors.transform(color);

        addChild(loading = new LockView());
        loading.visible = true;

        loadImageManager.load(photoUrl, onBitmapDataLoad)
    }

    private function onBitmapDataLoad(url:String, bitmapData:BitmapData):void {
        if (movieClip) throw new Error("movieClip already on stage");

        if (!bitmapData) {
            bitmapData = new DefaultAvatarBitmapData();
            bitmapData.colorTransform(bitmapData.rect, Colors.transform(color));
        }
        defaultAvatar.visible = false;
        loading.visible = false;

        addChild(movieClip = new LoserMC());
        movieClip.addEventListener(Event.EXIT_FRAME, onExitFrame);
        movieClip.play();

        avatar = new Sprite();
        const shape:Shape = BitmapUtils.createCircleShape(BitmapUtils.square(bitmapData, avatarSize));
        shape.x = -avatarSize / 2;
        shape.y = -avatarSize / 2;
        avatar.addChild(shape);
        addChild(avatar);
    }

    private function onExitFrame(event:Event):void {
        const bitmap:DisplayObject = movieClip.getChildByName("avatar");
        if (bitmap) {
            avatar.x = bitmap.x;
            avatar.y = bitmap.y;
            avatar.rotation = bitmap.rotation;
        }

        if (movieClip.currentFrame == movieClip.totalFrames) {
            movieClip.removeEventListener(Event.EXIT_FRAME, onExitFrame);
            movieClip.stop();
        }
    }
}
}
