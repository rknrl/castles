package ru.rknrl.castles.view.game.gameOver {
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.text.TextField;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
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
    private const urlToLoserAvatar:Dictionary = new Dictionary();

    public function GameOverScreen(winner:PlayerInfo, losers:Vector.<PlayerInfo>, win:Boolean, reward:int, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager) {
        addChild(title = createTextField(Fonts.title));
        title.text = win ? locale.win(reward) : locale.lose(reward);
        applyStarTextFormat(title);

        addChild(holder = new Sprite());
        holderWidth = Layout.itemSize * (losers.length + 1) + Layout.itemGap * (losers.length - 1) + winnerLooserGap + animationOffset;

        const winnerPhotoUrl:String = winner.info.getPhotoUrl(Layout.itemSize, Layout.itemSize); // todo: scale
        winnerAvatar = new FlyAvatar(winnerPhotoUrl, layout.bitmapDataScale, loadImageManager);
        winnerAvatar.x = Layout.itemSize / 2;
        holder.addChild(winnerAvatar);

        for (var i:int = 0; i < losers.length; i++) {
            const loserAvatar:LoserAvatar = new LoserAvatar();
            loserAvatar.x = Layout.itemSize + winnerLooserGap + Layout.itemSize / 2 + i * (Layout.itemSize + Layout.itemGap);
            holder.addChild(loserAvatar);

            const url:String = losers[i].info.getPhotoUrl(Layout.itemSize, Layout.itemSize); // todo: scale
            urlToLoserAvatar[url] = loserAvatar;
            loadImageManager.load(url, onBitmapDataLoaded);
        }

        this.layout = layout;
        addEventListener(MouseEvent.CLICK, onClick);
    }

    private function onBitmapDataLoaded(url:String, bitmapData:BitmapData):void {
        LoserAvatar(urlToLoserAvatar[url]).bitmapData = bitmapData;
    }

    public function set layout(value:Layout):void {
        title.scaleX = title.scaleY = value.scale;
        const pos:Point = value.rewardText(title.width, title.height);
        title.x = pos.x;
        title.y = pos.y;

        winnerAvatar.bitmapDataScale = value.bitmapDataScale;

        // todo: change loserAvatar bitmapDataScale

        holder.scaleX = holder.scaleY = value.scale;
        holder.x = value.screenCenterX - holderWidth * value.scale / 2;
        holder.y = value.contentCenterY;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(GameViewEvents.LEAVE_BUTTON_CLICK, true));
    }
}
}
