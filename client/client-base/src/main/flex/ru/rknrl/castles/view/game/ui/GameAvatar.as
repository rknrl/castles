//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.ui {
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.menu.top.Avatar;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.loaders.ILoadImageManager;

public class GameAvatar extends Sprite {
    private var avatar:Avatar;
    private var textField:TextField;

    private var _playerId:PlayerIdDTO;

    public function get playerId():PlayerIdDTO {
        return _playerId;
    }

    public function GameAvatar(playerInfo:PlayerInfo, layout:Layout, loadImageManager:ILoadImageManager) {
        _playerId = playerInfo.playerId;

        const color:uint = Colors.playerColor(playerInfo.playerId);

        const avatarBitmapSize:Number = layout.notScaledGameAvatarSize * layout.bitmapDataScale;
        const photoUrl:String = playerInfo.info.getPhotoUrl(avatarBitmapSize, avatarBitmapSize);
        addChild(avatar = new Avatar(photoUrl, layout.notScaledGameAvatarSize, layout.bitmapDataScale, loadImageManager, color));
        const bitmapPos:Point = layout.gameAvatarBitmapPos(playerInfo.playerId);
        avatar.x = bitmapPos.x;
        avatar.y = bitmapPos.y;

        addChild(textField = layout.createGameAvatarTextField());
        textField.textColor = color;
        textField.text = playerInfo.info.fullName;

        const textPos:Point = layout.gameAvatarTextPos(playerInfo.playerId, textField.width, textField.height);
        textField.x = textPos.x;
        textField.y = textPos.y;
    }

    public function set bitmapDataScale(value:Number):void {
        avatar.bitmapDataScale = value;
    }
}
}
