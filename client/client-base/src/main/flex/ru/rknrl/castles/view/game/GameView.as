//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game {
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;
import flash.utils.getTimer;

import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.core.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.game.area.GameArea;
import ru.rknrl.castles.view.game.gameOver.GameOverScreen;
import ru.rknrl.castles.view.game.ui.GameAvatar;
import ru.rknrl.castles.view.game.ui.magicItems.MagicItemsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.dto.BuildingId;
import ru.rknrl.dto.PlayerId;
import ru.rknrl.loaders.ILoadImageManager;

public class GameView extends Sprite {
    private var locale:CastlesLocale;
    private var loadImageManager:ILoadImageManager;

    private var _area:GameArea;

    public function get area():GameArea {
        return _area;
    }

    private var _magicItems:MagicItemsView;

    public function get magicItems():MagicItemsView {
        return _magicItems;
    }

    private var _tutor:GameTutorialView;

    public function get tutor():GameTutorialView {
        return _tutor;
    }

    public function get supportedPlayersCount():int {
        return _layout.supportedPlayersCount;
    }

    private var ui:Sprite;
    private const avatars:Vector.<GameAvatar> = new <GameAvatar>[];

    public function GameView(playerInfos:Vector.<PlayerInfo>, h:int, v:int, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager, deviceFactory:DeviceFactory) {
        this.locale = locale;
        this.loadImageManager = loadImageManager;
        addChild(_area = new GameArea(h, v));
        addChild(ui = new Sprite());
        ui.addChild(_magicItems = new MagicItemsView(layout));
        addChild(_tutor = new GameTutorialView(layout, locale, deviceFactory, loadImageManager));

        for (var i:int = 0; i < playerInfos.length; i++) {
            const playerInfo:PlayerInfo = playerInfos[i];
            const avatar:GameAvatar = new GameAvatar(playerInfo, layout, locale, loadImageManager);
            ui.addChild(avatar);
            avatars.push(avatar);
        }

        this.layout = layout;

        preloadGameOverAvatars(playerInfos);

        addEventListener(GameViewEvents.SHAKE, onShake);
        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
    }

    /** Заранее загружаем аватарки для геймовер экрана, чтобы там не ждать */
    private function preloadGameOverAvatars(playerInfos:Vector.<PlayerInfo>):void {
        for each(var playerInfo:PlayerInfo in playerInfos) {
            const gameOverAvatarBitmapSize:Number = _layout.gameOverAvatarBitmapSize;
            const gameOverAvatarUrl:String = playerInfo.info.getPhotoUrl(gameOverAvatarBitmapSize, gameOverAvatarBitmapSize);
            loadImageManager.load(gameOverAvatarUrl, function (url:String, bitmapData:BitmapData):void {
            });
        }
    }

    private function onAddedToStage(event:Event):void {
        removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseEvent);
        stage.addEventListener(MouseEvent.MOUSE_UP, onMouseEvent);
        stage.addEventListener(KeyboardEvent.KEY_UP, onKeyUp);
    }

    public function removeListeners():void {
        stage.removeEventListener(MouseEvent.MOUSE_DOWN, onMouseEvent);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseEvent);
        stage.removeEventListener(KeyboardEvent.KEY_UP, onKeyUp);
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        if (time < shakeStartTime + shakeDuration) {
            area.rotation = Math.sin(time - shakeStartTime) / 1.5;
        } else {
            area.rotation = 0;
        }
        dispatchEvent(new GameMouseEvent(GameMouseEvent.ENTER_FRAME, new Point(_area.mouseX, _area.mouseY)));
    }

    private function onMouseEvent(event:MouseEvent):void {
        if (mouseEnabled) {
            dispatchEvent(new GameMouseEvent(getMouseEventType(event.type), new Point(_area.mouseX, _area.mouseY)));
        }
    }

    private static function getMouseEventType(eventType:String):String {
        switch (eventType) {
            case MouseEvent.MOUSE_DOWN:
                return GameMouseEvent.MOUSE_DOWN;
            case MouseEvent.MOUSE_UP:
                return GameMouseEvent.MOUSE_UP;
        }
        throw new Error("unknown mouse event type " + eventType);
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;

        _area.scaleX = _area.scaleY = value.scale;
        const areaPos:Point = value.gameAreaPos(_area.h, _area.v);
        _area.x = areaPos.x;
        _area.y = areaPos.y;

        _magicItems.layout = value;

        for each(var avatar:GameAvatar in avatars) {
            avatar.bitmapDataScale = value.bitmapDataScale;
            avatar.scaleX = avatar.scaleY = value.scale;
            const avatarPos:Point = value.gameAvatarPos(avatar.playerId, _area.h, _area.v);
            avatar.x = avatarPos.x;
            avatar.y = avatarPos.y;
        }

        if (gameOverScreen) gameOverScreen.layout = value;
        if (_tutor) {
            _tutor.layout = value;
            _tutor.areaPos = areaPos;
        }
    }

    private function onKeyUp(event:KeyboardEvent):void {
        if (event.keyCode == Keyboard.ESCAPE) {
            dispatchEvent(new Event(GameViewEvents.SURRENDER));
        }
    }

    private static const shakeDuration:int = 200;
    private var shakeStartTime:int;

    private function onShake(event:Event = null):void {
        shakeStartTime = getTimer();
    }

    private var gameOverScreen:GameOverScreen;

    public function openGameOverScreen(winners:Vector.<PlayerInfo>, losers:Vector.<PlayerInfo>, win:Boolean, reward:int):void {
        if (gameOverScreen) throw new Error("gameOverScreen already open");
        _area.visible = ui.visible = _tutor.visible = false;
        addChild(gameOverScreen = new GameOverScreen(winners, losers, win, reward, _layout, locale, loadImageManager))
    }

    private function getAvatarById(playerId:PlayerId):GameAvatar {
        for each(var avatar:GameAvatar in avatars) {
            if (avatar.playerId.id == playerId.id) return avatar;
        }
        throw new Error("can't find avatar " + playerId.id);
    }

    public function setDeadAvatar(playerId:PlayerId):void {
        getAvatarById(playerId).dead = true;
    }

    public function tutorBlur(playerIds:Vector.<PlayerId>, buildingIds:Vector.<BuildingId>):void {
        for each(var playerId:PlayerId in playerIds)
            getAvatarById(playerId).tutorBlur = true;
        area.tutorBlur(buildingIds);
    }

    public function tutorUnblur():void {
        for each(var avatar:GameAvatar in avatars)
            avatar.tutorBlur = false;
        area.tutorUnblur();
    }
}
}
