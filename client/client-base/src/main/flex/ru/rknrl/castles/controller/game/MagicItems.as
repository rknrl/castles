//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import ru.rknrl.castles.view.game.ui.magicItems.MagicItemsView;
import protos.ItemType;

public class MagicItems {
    private var view:MagicItemsView;

    public function MagicItems(view:MagicItemsView) {
        this.view = view;
    }

    private var _selected:ItemType;

    public function get selected():ItemType {
        return _selected;
    }

    public function set selected(value:ItemType):void {
        _selected = value;
        view.selected = value;
    }

    public function useItem():void {
        view.lock = true;
        view.useItem(_selected);
        selected = null;
    }
}
}
