package ru.rknrl.castles.view.menu.top {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.model.menu.top.Top;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.utils.Align;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.loaders.ILoadImageManager;

public class TopScreen extends Screen {
    private static const topSize:int = 5;

    private var avatarsHolder:Sprite;
    private const avatars:Vector.<FlyAvatar> = new <FlyAvatar>[];
    private var titleTextField:TextField;

    public function TopScreen(top:Top, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager) {
        addChild(avatarsHolder = new Sprite());

        for (var i:int = 1; i <= topSize; i++) {
            const avatarBitmapSize:Number = Layout.itemSize * layout.bitmapDataScale;
            const photoUrl:String = top.getPlace(i).info.getPhotoUrl(avatarBitmapSize, avatarBitmapSize);
            const avatar:FlyAvatar = new FlyAvatar(photoUrl, layout.bitmapDataScale, loadImageManager, Colors.top(i));
            avatars.push(avatar);
            avatarsHolder.addChild(avatar);
        }

        titleTextField = createTextField(Fonts.title);
        titleTextField.text = locale.topTitle;

        this.layout = layout;
    }

    override public function set layout(value:Layout):void {
        Align.horizontal(Vector.<DisplayObject>(avatars), Layout.itemSize, Layout.itemGap);
        avatarsHolder.scaleX = avatarsHolder.scaleY = value.scale;
        avatarsHolder.x = value.screenCenterX - avatarsHolder.width / 2;
        avatarsHolder.y = value.contentCenterY;

        for each(var avatar:FlyAvatar in avatars) avatar.bitmapDataScale = value.bitmapDataScale;

        titleTextField.scaleX = titleTextField.scaleY = value.scale;
        const titlePos:Point = value.title(titleTextField.width, titleTextField.height);
        titleTextField.x = titlePos.x;
        titleTextField.y = titlePos.y;
    }

    override public function get titleContent():DisplayObject {
        return titleTextField;
    }
}
}
