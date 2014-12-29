package ru.rknrl.castles.view.menu.top {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.text.TextField;

import ru.rknrl.castles.model.menu.top.Top;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.utils.Align;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.castles.view.utils.createTextField;

public class TopScreen extends Screen {
    private static const topSize:int = 5;

    private var avatarsHolder:Sprite;
    private const avatars:Vector.<TopAvatar> = new <TopAvatar>[];
    private var titleTextField:TextField;

    public function TopScreen(top:Top, layout:Layout, locale:CastlesLocale, loadImageManager:LoadImageManager) {
        addChild(avatarsHolder = new Sprite());

        for (var i:int = 1; i <= topSize; i++) {
            const avatar:TopAvatar = new TopAvatar(top.getPlace(i), layout, loadImageManager);
            avatar.addEventListener(MouseEvent.CLICK, onClick);
            avatars.push(avatar);
            avatarsHolder.addChild(avatar);
        }

        titleTextField = createTextField(Fonts.title);
        titleTextField.text = locale.topTitle;

        this.layout = layout;
    }

    private function getAvatar(place:int):TopAvatar {
        for each(var avatar:TopAvatar in avatars) {
            if (avatar.userInfo.place == place) return avatar;
        }
        throw new Error("can't find avatar " + place);
    }

    public function set top(value:Top):void {
        for (var i:int = 1; i <= topSize; i++) {
            getAvatar(i).userInfo = value.getPlace(i);
        }
    }

    override public function set layout(value:Layout):void {
        Align.horizontal(Vector.<DisplayObject>(avatars), Layout.itemSize, Layout.itemGap);
        avatarsHolder.scaleX = avatarsHolder.scaleY = value.scale;
        avatarsHolder.x = value.screenCenterX - avatarsHolder.width / 2;
        avatarsHolder.y = value.contentCenterY;

        for each(var avatar:TopAvatar in avatars) avatar.layout = value;

        titleTextField.scaleX = titleTextField.scaleY = value.scale;
        const titlePos:Point = value.title(titleTextField.width, titleTextField.height);
        titleTextField.x = titlePos.x;
        titleTextField.y = titlePos.y;
    }

    private function onClick(event:MouseEvent):void {

    }

    override public function get titleContent():DisplayObject {
        return titleTextField;
    }
}
}
