package ru.rknrl.castles.view.game.ui {
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.GameColors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.top.Avatar;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.dto.PlayerInfoDTO;

public class GameAvatar extends Sprite {
    private var avatar:Avatar;
    private var textField:TextField;
    private var number:int;

    public function GameAvatar(number:int, playerInfo:PlayerInfoDTO, layout:Layout, loadImageManager:LoadImageManager) {
        this.number = number;

        addChild(avatar = new Avatar(playerInfo.photoUrl, layout.notScaledGameAvatarSize, layout.bitmapDataScale, loadImageManager));
        const bitmapPos:Point = layout.gameAvatarBitmapPos(number);
        avatar.x = bitmapPos.x;
        avatar.y = bitmapPos.y;

        addChild(textField = layout.createGameAvatarTextField());
        textField.textColor = GameColors.colorById(playerInfo.id);
        textField.text = playerInfo.name;

        const textPos:Point = layout.gameAvatarTextPos(number, textField.width, textField.height);
        textField.x = textPos.x;
        textField.y = textPos.y;
    }

    public function set bitmapDataScale(value:Number):void {
        avatar.bitmapDataScale = value;
    }
}
}
