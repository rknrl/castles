package ru.rknrl.castles.menu.screens.top {
import flash.display.BitmapData;
import flash.display.Sprite;

import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.utils.centerize;
import ru.rknrl.utils.changeTextFormat;

public class TopScreen extends MenuScreen {
    private static const count:int = 7;

    private var sender:AccountFacadeSender;
    private var locale:CastlesLocale;

    private var titleHolder:Sprite;
    private var title:Label;
    private var info:Label;

    private const avatars:Vector.<TopAvatar> = new <TopAvatar>[];

    public function TopScreen(id:String, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.locale = locale;

        titleHolder = new Sprite();
        addChild(titleHolder);

        titleHolder.addChild(title = createTextField(layout.shopTitleTextFormat, "Лучшие за неделю. Ваше место: 999"));
//        titleHolder.addChild(info = createTextField(layout.shopTitleTextFormat, "Ваше место: 9999"));

        for (var i:int = 0; i < count; i++) {
            const avatar:TopAvatar = new TopAvatar(new TopAvatarData(new BitmapData(32, 32, false, 0), "Толя Янот"), layout);
            avatars.push(avatar);
            addChild(avatar);
        }

        updateLayout(layout);
        super(id);
    }

    private var layout:Layout;

    public function updateLayout(layout:Layout):void {
        this.layout = layout;

        titleHolder.x = layout.titleCenterX;
        titleHolder.y = layout.titleCenterY;

        changeTextFormat(title, layout.shopTitleTextFormat);
        centerize(title);

        var y:int = layout.bodyTop;

        for each(var avatar:TopAvatar in avatars) {
            avatar.updateLayout(layout);
            avatar.x = layout.bodyCenterX - avatar.width / 2;
            avatar.y = y + layout.topAvatarHeight / 2 - layout.topAvatarBitmapSize / 2;
            y += layout.topAvatarHeight;
        }

        updateTransition(_transition);
    }

    private var _transition:Number = 0;

    override public function set transition(value:Number):void {
        _transition = value;
        updateTransition(_transition);
    }

    private function updateTransition(value:Number):void {
    }
}
}
