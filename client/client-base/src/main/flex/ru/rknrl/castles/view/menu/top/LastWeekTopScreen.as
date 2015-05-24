//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.top {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.model.events.AcceptTopEvent;
import ru.rknrl.castles.model.menu.top.Top;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.core.points.Point;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.utils.Align;
import ru.rknrl.utils.createTextField;

public class LastWeekTopScreen extends Screen {
    private static const topSize:int = 5;
    private var loadImageManager:ILoadImageManager;

    private var avatarsHolder:Sprite;
    private const avatars:Vector.<FlyAvatar> = new <FlyAvatar>[];

    private var placeTextField:TextField;
    private var titleTextField:TextField;

    private var title:Sprite;

    public function LastWeekTopScreen(top:Top, place:Number, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager) {
        this.loadImageManager = loadImageManager;
        addChild(avatarsHolder = new Sprite());

        _layout = layout;
        this.top = top;

        title = new Sprite();

        placeTextField = createTextField(Fonts.title);
        title.addChild(placeTextField);
        this.place = place;

        titleTextField = createTextField(Fonts.title);
        titleTextField.text = locale.topTitle;
        title.addChild(titleTextField);
        alignTitle();

        addEventListener(MouseEvent.MOUSE_DOWN, onClick);
    }

    public function set place(value:Number):void {
        placeTextField.text = "У вас " + value + " место";
        alignPlace();
    }

    private var _top:Top;

    public function set top(value:Top):void {
        _top = value;

        while (avatarsHolder.numChildren) avatarsHolder.removeChildAt(0);
        avatars.length = 0;

        for (var i:int = 1; i <= topSize; i++) {
            const avatarBitmapSize:Number = Layout.itemSize * _layout.bitmapDataScale;
            const photoUrl:String = value.getPlace(i).info.getPhotoUrl(avatarBitmapSize, avatarBitmapSize);
            const avatar:FlyAvatar = new FlyAvatar(photoUrl, _layout.bitmapDataScale, loadImageManager, Colors.top(i));
            avatars.push(avatar);
            avatarsHolder.addChild(avatar);
        }

        alignAvatars();
    }

    private var _layout:Layout;

    override public function set layout(value:Layout):void {
        _layout = value;
        alignAvatars();
        alignPlace();
        alignTitle();
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
        const pos:Point = _layout.balance(placeTextField.width);
        placeTextField.x = pos.x;
        placeTextField.y = pos.y;
    }

    private function alignTitle():void {
        titleTextField.scaleX = titleTextField.scaleY = _layout.scale;
        const pos:Point = _layout.title(titleTextField.width, titleTextField.height);
        titleTextField.x = pos.x;
        titleTextField.y = pos.y;
    }

    override public function get titleContent():DisplayObject {
        return title;
    }

    override public function get showBalance():Boolean {
        return false;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new AcceptTopEvent(_top))
    }
}
}
