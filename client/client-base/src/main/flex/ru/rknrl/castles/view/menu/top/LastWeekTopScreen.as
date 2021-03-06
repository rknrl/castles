//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.top {
import protos.Place;

import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.text.TextField;

import protos.Top;

import ru.rknrl.castles.model.events.AcceptTopEvent;
import ru.rknrl.castles.model.menu.top.TopUtils;
import ru.rknrl.castles.model.menu.top.TopUtils;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.core.points.Point;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.display.Align;
import ru.rknrl.display.createTextField;

public class LastWeekTopScreen extends Sprite {
    private static const topSize:int = 5;
    private var loadImageManager:ILoadImageManager;

    private var mouseHolder:Bitmap;
    private var avatarsHolder:Sprite;
    private const avatars:Vector.<FlyAvatar> = new <FlyAvatar>[];

    private var placeTextField:TextField;
    private var titleTextField:TextField;

    private var title:Sprite;

    public function LastWeekTopScreen(top:Top, place:Place, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager) {
        this.loadImageManager = loadImageManager;
        _layout = layout;

        addChild(mouseHolder = new Bitmap(Colors.transparent));
        alignMouseHolder();

        addChild(avatarsHolder = new Sprite());

        this.top = top;

        addChild(title = new Sprite());

        placeTextField = createTextField(Fonts.title);
        title.addChild(placeTextField);
        this.place = place;

        titleTextField = createTextField(Fonts.title);
        titleTextField.text = "Лучшие на прошлой неделе";
        title.addChild(titleTextField);
        alignTitle();

        addEventListener(MouseEvent.MOUSE_DOWN, onClick);
    }

    public function set place(value:Place):void {
        placeTextField.text = value ? "Вы заняли " + value.place + " место" : "Вы не играли на прошлой неделе";
        alignPlace();
    }

    private var _top:Top;

    public function set top(value:Top):void {
        _top = value;

        while (avatarsHolder.numChildren) avatarsHolder.removeChildAt(0);
        avatars.length = 0;

        for (var i:int = 1; i <= topSize; i++) {
            const avatarBitmapSize:Number = Layout.itemSize * _layout.bitmapDataScale;
            const photoUrl:String = TopUtils.getPlace(value, i).getPhotoUrl(avatarBitmapSize, avatarBitmapSize);
            const avatar:FlyAvatar = new FlyAvatar(photoUrl, _layout.bitmapDataScale, loadImageManager, Colors.top(i));
            avatars.push(avatar);
            avatarsHolder.addChild(avatar);
        }

        alignAvatars();
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;
        alignMouseHolder();
        alignAvatars();
        alignPlace();
        alignTitle();
    }

    private function alignMouseHolder():void {
        mouseHolder.width = _layout.screenWidth;
        mouseHolder.height = _layout.screenHeight;
    }

    private function alignAvatars():void {
        Align.horizontal(Vector.<DisplayObject>(avatars), Layout.itemSize, Layout.itemGap);
        avatarsHolder.scaleX = avatarsHolder.scaleY = _layout.scale;
        avatarsHolder.x = _layout.screenCenterX - avatarsHolder.width / 2;
        avatarsHolder.y = _layout.contentCenterY;

        for each(var avatar:FlyAvatar in avatars) avatar.bitmapDataScale = _layout.bitmapDataScale;
    }

    private function alignPlace():void {
        placeTextField.scaleX = placeTextField.scaleY = _layout.scale;
        const pos:Point = _layout.title(placeTextField.width, placeTextField.height);
        placeTextField.x = pos.x;
        placeTextField.y = pos.y;
    }

    private function alignTitle():void {
        titleTextField.scaleX = titleTextField.scaleY = _layout.scale;
        const pos:Point = _layout.balance(titleTextField.width);
        titleTextField.x = pos.x;
        titleTextField.y = pos.y;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new AcceptTopEvent(_top))
    }
}
}
