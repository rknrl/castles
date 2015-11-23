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

import ru.rknrl.core.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.top.Avatar;
import protos.PlayerId;
import ru.rknrl.loaders.ILoadImageManager;

public class GameAvatar extends Sprite {
    private var avatar:Avatar;
    private var textField:TextField;

    private var playerInfo:PlayerInfo;
    private var layout:Layout;
    private var locale:CastlesLocale;

    public function get playerId():PlayerId {
        return playerInfo.playerId;
    }

    public function GameAvatar(playerInfo:PlayerInfo, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager) {
        this.playerInfo = playerInfo;
        this.layout = layout;
        this.locale = locale;

        const color:uint = Colors.playerColor(playerInfo.playerId);

        const avatarBitmapSize:Number = layout.notScaledGameAvatarSize * layout.bitmapDataScale;
        const photoUrl:String = playerInfo.info.getPhotoUrl(avatarBitmapSize, avatarBitmapSize);
        addChild(avatar = new Avatar(photoUrl, layout.notScaledGameAvatarSize, layout.bitmapDataScale, loadImageManager, color));
        const bitmapPos:Point = layout.gameAvatarBitmapPos(playerInfo.playerId);
        avatar.x = bitmapPos.x;
        avatar.y = bitmapPos.y;

        addChild(textField = layout.createGameAvatarTextField());
        textField.textColor = color;

        this.dead = false;
    }

    public function set bitmapDataScale(value:Number):void {
        avatar.bitmapDataScale = value;
    }

    public function set dead(value:Boolean):void {
        textField.text = value ? locale.dead : playerInfo.info.fullName;

        const textPos:Point = layout.gameAvatarTextPos(playerInfo.playerId, textField.width, textField.height);
        textField.x = textPos.x;
        textField.y = textPos.y;
    }

    public function set tutorBlur(value:Boolean):void {
        alpha = value ? 0.3 : 1;
    }
}
}
