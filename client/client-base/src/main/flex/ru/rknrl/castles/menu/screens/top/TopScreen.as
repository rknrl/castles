package ru.rknrl.castles.menu.screens.top {
import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;

public class TopScreen extends Screen {
    private static const count:int = 5;

    private var sender:AccountFacadeSender;
    private var locale:CastlesLocale;

    private const rows:Vector.<TopRow> = new <TopRow>[];

    public function TopScreen(sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.locale = locale;

        for (var i:int = 0; i < count; i++) {
            const row:TopRow = new TopRow(layout);
            rows.push(row);
            addChild(row);
        }

        updateLayout(layout);
    }

    private var layout:Layout;

    public function updateLayout(layout:Layout):void {
        this.layout = layout;

        var y:int = 100;

        for each(var row:TopRow in rows) {
            row.updateLayout(layout);
            row.x = 100;
            row.y = y;
            y += layout.topAvatarHeight;
        }
    }
}
}

import flash.display.BitmapData;
import flash.display.Sprite;
import flash.text.TextFormat;

import ru.rknrl.castles.menu.screens.top.TopAvatar;
import ru.rknrl.castles.menu.screens.top.TopAvatarData;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.utils.changeTextFormat;

class TopRow extends Sprite {
    private var placeTextField:Label;
    private var avatar:TopAvatar;
    private var winTextField:Label;
    private var loseTextField:Label;


    public function TopRow(layout:Layout) {
        const textFormat:TextFormat = layout.topAvatarTextFormat; // todo

        placeTextField = createTextField(textFormat, "1");
        addChild(placeTextField);

        avatar = new TopAvatar(new TopAvatarData(new BitmapData(32, 32, false, 0), "Толя Янот"), layout);
        addChild(avatar);

        winTextField = createTextField(textFormat, "100");
        addChild(winTextField);

        loseTextField = createTextField(textFormat, "100");
        addChild(loseTextField);
    }

    public function updateLayout(layout:Layout):void {
        const textFormat:TextFormat = layout.topAvatarTextFormat; // todo

        changeTextFormat(placeTextField, textFormat);
        changeTextFormat(winTextField, textFormat);
        changeTextFormat(loseTextField, textFormat);
        avatar.updateLayout(layout);

        avatar.x = 100;

        winTextField.x = 400;
        loseTextField.x = 500;
    }
}