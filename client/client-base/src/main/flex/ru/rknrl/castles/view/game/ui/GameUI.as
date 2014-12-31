package ru.rknrl.castles.view.game.ui {
import flash.display.Sprite;
import flash.geom.Point;

import ru.rknrl.castles.view.game.ui.magicItems.MagicItemsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.dto.PlayerInfoDTO;

public class GameUI extends Sprite {

    public function GameUI(playerInfos:Vector.<PlayerInfoDTO>, layout:Layout, loadImageManager:LoadImageManager) {

        this.layout = layout;
    }

    public function set layout(value:Layout):void {
    }
}
}
