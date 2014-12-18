package ru.rknrl.castles.game.ui.avatar {
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.BitmapUtils;
import ru.rknrl.castles.game.layout.GameLayoutPortrait;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.utils.changeTextFormat;

public class AvatarPortrait extends Sprite {
    private var left:Boolean;

    private var avatar:Shape;
    private var nameTextField:Label;

    public function AvatarPortrait(data:GameAvatarData, left:Boolean, layout:GameLayoutPortrait) {
        mouseChildren = false;
        this.left = left;

        addChild(avatar = BitmapUtils.createCircleShape(data.bitmapData));

        addChild(nameTextField = createTextField(layout.avatarTextFormat, data.text));
        nameTextField.textColor = data.color;

        updateLayout(layout);
    }

    public function updateLayout(layout:GameLayoutPortrait):void {
        avatar.width = avatar.height = layout.avatarBitmapSize;

        changeTextFormat(nameTextField, layout.avatarTextFormat);
        nameTextField.y = (layout.avatarBitmapSize - nameTextField.height) / 2;

        if (left) {
            nameTextField.x = avatar.width + layout.avatarHorGap;
        } else {
            avatar.x = nameTextField.width + layout.avatarHorGap;
        }
    }
}
}
