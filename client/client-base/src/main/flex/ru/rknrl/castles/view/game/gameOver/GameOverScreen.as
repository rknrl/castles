package ru.rknrl.castles.view.game.gameOver {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.top.FlyAvatar;
import ru.rknrl.castles.view.utils.applyStarTextFormat;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.loaders.ILoadImageManager;

public class GameOverScreen extends Sprite {
    private static const winnerLooserGap:int = 24;
    private static const animationOffset:int = 24;

    private var title:TextField;
    private var holder:Sprite;
    private var holderWidth:Number;
    private var winnerAvatar:FlyAvatar;

    public function GameOverScreen(winner:PlayerInfo, losers:Vector.<PlayerInfo>, win:Boolean, reward:int, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager) {
        addChild(title = createTextField(Fonts.title));
        title.text = win ? locale.win(reward) : locale.lose(reward);
        applyStarTextFormat(title);

        addChild(holder = new Sprite());
        holderWidth = Layout.itemSize * (losers.length + 1) + Layout.itemGap * (losers.length - 1) + winnerLooserGap + animationOffset;

        const avatarBitmapSize:Number = Layout.itemSize * layout.bitmapDataScale;
        const winnerPhotoUrl:String = winner.info.getPhotoUrl(avatarBitmapSize, avatarBitmapSize);
        const winnerColor:uint = Colors.playerColor(winner.playerId);
        winnerAvatar = new FlyAvatar(winnerPhotoUrl, layout.bitmapDataScale, loadImageManager, winnerColor);
        winnerAvatar.x = Layout.itemSize / 2;
        holder.addChild(winnerAvatar);

        for (var i:int = 0; i < losers.length; i++) {
            const url:String = losers[i].info.getPhotoUrl(avatarBitmapSize, avatarBitmapSize);
            const color:uint = Colors.playerColor(losers[i].playerId);
            const loserAvatar:LoserAvatar = new LoserAvatar(url, loadImageManager, color);
            loserAvatar.x = Layout.itemSize + winnerLooserGap + Layout.itemSize / 2 + i * (Layout.itemSize + Layout.itemGap);
            holder.addChild(loserAvatar);
        }

        this.layout = layout;
        addEventListener(MouseEvent.CLICK, onClick);
    }

    public function set layout(value:Layout):void {
        title.scaleX = title.scaleY = value.scale;
        const pos:Point = value.rewardText(title.width, title.height);
        title.x = pos.x;
        title.y = pos.y;

        winnerAvatar.bitmapDataScale = value.bitmapDataScale;

        holder.scaleX = holder.scaleY = value.scale;
        holder.x = value.screenCenterX - holderWidth * value.scale / 2;
        holder.y = value.contentCenterY;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(GameViewEvents.LEAVE_BUTTON_CLICK, true));
    }
}
}
