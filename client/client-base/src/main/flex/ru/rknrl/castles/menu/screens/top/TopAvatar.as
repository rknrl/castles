package ru.rknrl.castles.menu.screens.top {
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.BitmapUtils;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.utils.changeTextFormat;

public class TopAvatar extends Sprite {
    private var left:Boolean;

    private var avatar:Shape;
    private var nameTextField:Label;

    public function TopAvatar(data:TopAvatarData, layout:Layout) {
        mouseChildren = false;
        this.left = left;

        addChild(avatar = BitmapUtils.createCircleShape(data.bitmapData));

        addChild(nameTextField = createTextField(layout.topAvatarTextFormat, data.text));

        updateLayout(layout);
    }

    public function updateLayout(layout:Layout):void {
        avatar.width = avatar.height = layout.topAvatarBitmapSize;

        changeTextFormat(nameTextField, layout.topAvatarTextFormat);
        nameTextField.y = (layout.topAvatarBitmapSize - nameTextField.height) / 2;

        nameTextField.x = avatar.width + layout.gap;
    }
}
}
