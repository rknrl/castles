package ru.rknrl.castles.game.ui {
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.EventDispatcher;
import flash.events.MouseEvent;
import flash.utils.Dictionary;
import flash.utils.getTimer;

import ru.rknrl.castles.game.layout.GameLayout;
import ru.rknrl.castles.game.ui.avatar.GameAvatarData;
import ru.rknrl.castles.menu.screens.gameOver.GameOverScreen;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.ItemStateDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.ItemsStateDTO;

public class GameUI extends Sprite {
    private var layout:Layout;
    private var locale:CastlesLocale;

    private const typeToItem:Dictionary = new Dictionary();
    private const avatars:Vector.<DisplayObject> = new <DisplayObject>[];

    public function GameUI(layout:Layout, gameLayout:GameLayout, locale:CastlesLocale, itemsState:ItemsStateDTO) {
        this.locale = locale;

        // todo: game avatars hardcode
        const avatarsDatas:Vector.<GameAvatarData> =
                new <GameAvatarData>[
                    new GameAvatarData(new BitmapData(64, 64, false, 0), "Тамара Григорьевна", Colors.yellow),
                    new GameAvatarData(new BitmapData(64, 64, false, 0), "Alexandra Serova", Colors.cyan),
                    new GameAvatarData(new BitmapData(64, 64, false, 0), "Толя Янот", Colors.magenta),
                    new GameAvatarData(new BitmapData(64, 64, false, 0), "Napoleon1769", Colors.red)
                ];

        avatarsDatas.length = layout is LayoutLandscape ? 4 : 2; // todo: game avatars hardcode

        addAvatars(gameLayout, avatarsDatas);
        addItems(gameLayout, itemsState);
        updateLayout(layout, gameLayout);
    }

    public function updateLayout(layout:Layout, gameLayout:GameLayout):void {
        this.layout = layout;

        if (gameOverScreen) gameOverScreen.updateLayout(layout);

        for (var i:int = 0; i < Utils.ALL_ITEMS.length; i++) {
            const itemType:ItemType = Utils.ALL_ITEMS[i];
            const item:GameItem = typeToItem[itemType];
            item.updateLayout(gameLayout);
            item.x = gameLayout.gameItemLeft + i * (gameLayout.gameItemSize + gameLayout.gameItemHorGap);
            item.y = gameLayout.gameItemTop;
        }

        for (var i:int = 0; i < avatars.length; i++) {
            gameLayout.updateGameAvatar(i, avatars[i])
        }
    }

    private function addAvatars(gameLayout:GameLayout, datas:Vector.<GameAvatarData>):void {
        for (var i:int = 0; i < datas.length; i++) {
            const avatar:DisplayObject = gameLayout.createGameAvatar(i, datas[i]);
            avatars.push(avatar);
            addChild(avatar);
        }
    }

    private function addItems(layout:GameLayout, itemsState:ItemsStateDTO):void {
        function getItemState(itemType:ItemType):ItemStateDTO {
            for each(var itemState:ItemStateDTO in itemsState.items) {
                if (itemState.itemType == itemType) {
                    return itemState;
                }
            }
            throw new Error("can't find item state " + itemType);
        }

        for (var i:int = 0; i < Utils.ALL_ITEMS.length; i++) {
            const itemType:ItemType = Utils.ALL_ITEMS[i];
            const itemState:ItemStateDTO = getItemState(itemType);
            const item:GameItem = new GameItem(itemType, layout, itemState.millisTillCooldownEnd, getTimer(), itemState.cooldownDuration, itemState.count);
            item.addEventListener(MouseEvent.CLICK, onItemClick);
            typeToItem[itemType] = item;
            addChild(item);
        }
    }

    public function updateItem(itemType:ItemType, millisTillCooldownEnd:int, time:int, cooldownDuration:int, count:int):void {
        const item:GameItem = typeToItem[itemType];
        item.update(millisTillCooldownEnd, time, cooldownDuration, count);
    }

    private function onItemClick(event:MouseEvent):void {
        const item:GameItem = GameItem(event.target);
        dispatchEvent(new ItemClickEvent(item))
    }

    private var gameOverScreen:GameOverScreen;

    public function openGameOverScreen(win:Boolean, reward:int):EventDispatcher {
        if (gameOverScreen) throw new Error("gameOverScreen already created");
        addChild(gameOverScreen = new GameOverScreen(win, reward, layout, locale));
        for each(var item:GameItem in typeToItem) item.alpha = 0.1;
        for each(var avatar:DisplayObject in avatars) avatar.alpha = 0.5;
        return gameOverScreen;
    }
}
}
