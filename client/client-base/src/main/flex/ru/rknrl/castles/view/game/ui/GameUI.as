package ru.rknrl.castles.view.game.ui {
import flash.display.Sprite;

import ru.rknrl.castles.view.game.ui.magicItems.MagicItemsView;
import ru.rknrl.castles.view.layout.Layout;

public class GameUI extends Sprite {
    private const avatars:Vector.<GameAvatar> = new <GameAvatar>[];
    public var magicItems:MagicItemsView;

    public function GameUI(layout:Layout) {
        this.layout = layout;
        addChild(magicItems = new MagicItemsView(layout));
    }

    public function set layout(value:Layout):void {
        magicItems.layout = value;
    }
}
}
