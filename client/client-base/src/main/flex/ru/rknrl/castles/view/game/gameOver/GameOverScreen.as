package ru.rknrl.castles.view.game.gameOver {
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.geom.Point;
import flash.text.TextField;
import flash.utils.Dictionary;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.top.FlyAvatar;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.castles.view.utils.applyStarTextFormat;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.PlayerInfoDTO;

public class GameOverScreen extends Sprite {
    private static const winnerLooserGap:int = 48;

    private var title:TextField;
    private var holder:Sprite;
    private var winnerAvatar:FlyAvatar;
    private const urlToLoserAvatar:Dictionary = new Dictionary();

    public function GameOverScreen(layout:Layout, locale:CastlesLocale, loadImageManager:LoadImageManager, winner:PlayerInfoDTO, losers:Vector.<PlayerInfoDTO>, win:Boolean, reward:int) {
        addChild(title = createTextField(Fonts.title));
        title.text = win ? locale.win(reward) : locale.lose(reward);
        applyStarTextFormat(title);

        addChild(holder = new Sprite());

        winnerAvatar = new FlyAvatar(winner.photoUrl, layout.bitmapDataScale, loadImageManager);
        winnerAvatar.x = Layout.itemSize / 2;
        holder.addChild(winnerAvatar);

        for (var i:int = 0; i < losers.length; i++) {
            const loserAvatar:LoserAvatar = new LoserAvatar();
            loserAvatar.x = Layout.itemSize + winnerLooserGap + Layout.itemSize / 2 + i * (Layout.itemSize + Layout.itemGap);
            holder.addChild(loserAvatar);

            const url:String = losers[i].photoUrl;
            urlToLoserAvatar[url] = loserAvatar;
            loadImageManager.load(url, onBitmapDataLoaded);
        }

        this.layout = layout;
    }

    private function onBitmapDataLoaded(url:String, bitmapData:BitmapData):void {
        LoserAvatar(urlToLoserAvatar[url]).bitmapData = bitmapData;
    }

    public function set layout(value:Layout):void {
        title.scaleX = title.scaleY = value.scale;
        const pos:Point = value.title(title.width, title.height);
        title.x = pos.x;
        title.y = pos.y;

        winnerAvatar.bitmapDataScale = value.bitmapDataScale;

        // todo: change loserAvatar bitmapDataScale

        holder.scaleX = holder.scaleY = value.scale;
        holder.x = value.screenCenterX - holder.width / 2;
        holder.y = value.contentCenterY;
    }
}
}
