package ru.rknrl.castles.menu.screens.main.popup.popup.item {
import flash.display.Sprite;

import ru.rknrl.utils.OverrideMe;

public class PopupItem extends Sprite {
    public function PopupItem() {
        mouseChildren = false;
    }

    public function animate():void {
        throw OverrideMe();
    }
}
}
