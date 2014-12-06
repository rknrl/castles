package ru.rknrl.castles.game.ui.avatar {
import flash.display.Shape;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.BitmapUtils;
import ru.rknrl.castles.game.layout.GameLayoutLandscape;
import ru.rknrl.utils.changeTextFormat;

public class AvatarLandscape extends Sprite {
    private var avatar:Shape;
    private var nameTextField:TextField;

    public function AvatarLandscape(data:AvatarData, layout:GameLayoutLandscape) {
        mouseChildren = false;

        addChild(avatar = BitmapUtils.createCircleShape(data.bitmapData));

        addChild(nameTextField = new TextField());
        nameTextField.embedFonts = true;
        nameTextField.wordWrap = true;
        nameTextField.defaultTextFormat = layout.avatarTextFormat;
        nameTextField.textColor = data.color;
        nameTextField.text = data.text;
        nameTextField.multiline = true;

        updateLayout(layout);
    }

    public function updateLayout(layout:GameLayoutLandscape):void {
        avatar.width = avatar.height = layout.avatarBitmapSize;
        avatar.x = -layout.avatarBitmapSize / 2;
        avatar.y = -layout.avatarBitmapSize;

        changeTextFormat(nameTextField, layout.avatarTextFormat);

        nameTextField.width = layout.avatarWidth;
        nameTextField.height = layout.avatarTextHeight;

        nameTextField.x = -width / 2;
        nameTextField.y = layout.avatarVerGap;
    }
}
}
