package ru.rknrl.castles.view.game {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.game.area.GameArea;
import ru.rknrl.castles.view.game.gameOver.GameOverScreen;
import ru.rknrl.castles.view.game.ui.GameAvatar;
import ru.rknrl.castles.view.game.ui.magicItems.MagicItemsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.loaders.ILoadImageManager;

public class GameView extends Sprite {
    private var locale:CastlesLocale;
    private var loadImageManager:ILoadImageManager;

    public var area:GameArea;
    private var ui:Sprite;

    private const avatars:Vector.<GameAvatar> = new <GameAvatar>[];
    public var magicItems:MagicItemsView;
    public var tutor:GameTutorialView;

    public function get supportedPlayersCount():int {
        return _layout.supportedPlayersCount;
    }

    public function GameView(playerInfos:Vector.<PlayerInfo>, h:int, v:int, layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager, deviceFactory:DeviceFactory) {
        this.locale = locale;
        this.loadImageManager = loadImageManager;
        addChild(area = new GameArea(h, v));
        addChild(ui = new Sprite());
        ui.addChild(magicItems = new MagicItemsView(layout));
        addChild(tutor = new GameTutorialView(layout, deviceFactory));

        for (var i:int = 0; i < playerInfos.length; i++) {
            const playerInfo:PlayerInfo = playerInfos[i];
            const avatar:GameAvatar = new GameAvatar(playerInfo, layout, loadImageManager);
            ui.addChild(avatar);
            avatars.push(avatar);
        }

        this.layout = layout;
        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
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
        dispatchEvent(new GameMouseEvent(GameMouseEvent.ENTER_FRAME, new Point(area.mouseX, area.mouseY)))
    }

    private function onMouseEvent(event:MouseEvent):void {
        dispatchEvent(new GameMouseEvent(getMouseEventType(event.type), new Point(area.mouseX, area.mouseY)))
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

        area.scaleX = area.scaleY = value.scale;
        const areaPos:Point = value.gameAreaPos(area.h, area.v);
        area.x = areaPos.x;
        area.y = areaPos.y;

        magicItems.layout = value;

        for each(var avatar:GameAvatar in avatars) {
            avatar.bitmapDataScale = value.bitmapDataScale;
            avatar.scaleX = avatar.scaleY = value.scale;
            const avatarPos:Point = value.gameAvatarPos(avatar.playerId, area.h, area.v);
            avatar.x = avatarPos.x;
            avatar.y = avatarPos.y;
        }

        if (gameOverScreen) gameOverScreen.layout = value;
        if (tutor) {
            tutor.layout = value;
            tutor.areaPos = areaPos;
        }
    }

    private function onKeyUp(event:KeyboardEvent):void {
        if (event.keyCode == Keyboard.ESCAPE) {
            dispatchEvent(new Event(GameViewEvents.SURRENDER, true));
        }
    }

    private var gameOverScreen:GameOverScreen;

    public function openGameOverScreen(winners:Vector.<PlayerInfo>, losers:Vector.<PlayerInfo>, win:Boolean, reward:int):void {
        if (gameOverScreen) throw new Error("gameOverScreen already open");
        area.visible = false;
        ui.visible = false;
        addChild(gameOverScreen = new GameOverScreen(winners, losers, win, reward, _layout, locale, loadImageManager))
    }
}
}
