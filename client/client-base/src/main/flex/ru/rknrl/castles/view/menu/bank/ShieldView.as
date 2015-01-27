package ru.rknrl.castles.view.menu.bank {
import flash.display.Sprite;

public class ShieldView extends Sprite {
    public function ShieldView(text: String) {
        const shield:Shield = new Shield();
        shield.textField.text = text;
        addChild(shield)
    }
}
}
