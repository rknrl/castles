package ru.rknrl.castles.view.game.ui {
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.top.Avatar;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.PlayerInfoDTO;

public class GameAvatar extends Sprite {
    private var avatar:Avatar;
    private var textField:TextField;

    public function GameAvatar(playerInfo:PlayerInfoDTO, layout:Layout, loadImageManager:LoadImageManager) {
        addChild(new Avatar(playerInfo.photoUrl, layout.gameAvatarSize, layout.bitmapDataScale, loadImageManager));

        addChild(textField = createTextField(Fonts.gameAvatar));
        textField.text = playerInfo.name;
    }

    public function set layout(value:Layout):void {
        avatar.bitmapDataScale = value.bitmapDataScale;
    }
}
}
