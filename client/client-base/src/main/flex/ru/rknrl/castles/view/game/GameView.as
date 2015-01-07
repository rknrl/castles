package ru.rknrl.castles.view.game {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

import ru.rknrl.castles.model.events.GameMouseEvent;
import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.game.area.GameArea;
import ru.rknrl.castles.view.game.gameOver.GameOverScreen;
import ru.rknrl.castles.view.game.ui.GameAvatar;
import ru.rknrl.castles.view.game.ui.magicItems.MagicItemsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.dto.PlayerInfoDTO;

public class GameView extends Sprite {
    private var locale:CastlesLocale;
    private var loadImageManager:LoadImageManager;

    public var area:GameArea;
    private var ui:Sprite;

    private const avatars:Vector.<GameAvatar> = new <GameAvatar>[];
    public var magicItems:MagicItemsView;

    public function GameView(playerInfos:Vector.<PlayerInfoDTO>, h:int, v:int, layout:Layout, locale:CastlesLocale, loadImageManager:LoadImageManager) {
        this.locale = locale;
        this.loadImageManager = loadImageManager;
        addChild(area = new GameArea(h, v));
        addChild(ui = new Sprite());
        ui.addChild(magicItems = new MagicItemsView(layout));

        for (var i:int = 0; i < playerInfos.length; i++) {
            const avatar:GameAvatar = new GameAvatar(i, playerInfos[i], layout, loadImageManager);
            ui.addChild(avatar);
            avatars.push(avatar);
        }

        this.layout = layout;
        addEventListener(Event.ADDED_TO_STAGE, onAddedToStage);
    }

    private function onAddedToStage(event:Event):void {
        removeEventListener(Event.ADDED_TO_STAGE, onAddedToStage);

        stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseEvent);
        stage.addEventListener(MouseEvent.MOUSE_MOVE, onMouseEvent);
        stage.addEventListener(MouseEvent.MOUSE_UP, onMouseEvent);
        stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    public function removeListeners():void {
        stage.removeEventListener(MouseEvent.MOUSE_DOWN, onMouseEvent);
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onMouseEvent);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseEvent);
        stage.removeEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    private function onMouseEvent(event:MouseEvent):void {
        dispatchEvent(new GameMouseEvent(getMouseEventType(event.type), new Point(area.mouseX, area.mouseY)))
    }

    private static function getMouseEventType(eventType:String):String {
        switch (eventType) {
            case MouseEvent.MOUSE_DOWN:
                return GameMouseEvent.MOUSE_DOWN;
            case MouseEvent.MOUSE_MOVE:
                return GameMouseEvent.MOUSE_MOVE;
            case MouseEvent.MOUSE_UP:
                return GameMouseEvent.MOUSE_UP;
        }
        throw new Error("unknown mouse event type " + eventType);
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;

        area.scaleX = area.scaleY = value.scale;
        const areaPos:Point = value.gameAreaPos(area.width, area.height);
        area.x = areaPos.x;
        area.y = areaPos.y;

        magicItems.layout = value;

        for (var i:int = 0; i < avatars.length; i++) {
            const avatar:GameAvatar = avatars[i];
            avatar.bitmapDataScale = value.bitmapDataScale;
            avatar.scaleX = avatar.scaleY = value.scale;
            const avatarPos:Point = value.gameAvatarPos(i, area.width, area.height);
            avatar.x = avatarPos.x;
            avatar.y = avatarPos.y;
        }
    }

    private function onKeyDown(event:KeyboardEvent):void {
        if (event.keyCode == Keyboard.ESCAPE) {
            stage.removeEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
            dispatchEvent(new Event(GameViewEvents.SURRENDER, true));
        }
    }

    public function openGameOverScreen(winner:PlayerInfoDTO, losers:Vector.<PlayerInfoDTO>, win:Boolean, reward:int):void {
        area.visible = false;
        ui.visible = false;
        addChild(new GameOverScreen(winner, losers, win, reward, _layout, locale, loadImageManager))
    }
}
}
