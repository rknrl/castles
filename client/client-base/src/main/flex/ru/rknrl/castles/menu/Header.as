package ru.rknrl.castles.menu {
import flash.display.Sprite;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.funnyUi.GoldTextField;

public class Header extends Sprite {
    private var goldTextField:GoldTextField;
    private var layout:Layout;

    public function Header(gold:int, layout:Layout) {
        mouseChildren = false;
        addChild(goldTextField = new GoldTextField("", layout.headerTextFormat, gold, Colors.magenta));
        updateLayout(layout);
        this.gold = gold;
    }

    public function updateLayout(layout:Layout):void {
        this.layout = layout;

        goldTextField.textFormat = layout.headerTextFormat;
        posGoldTextField();
    }

    public function animateGold():void {
        goldTextField.animate();
    }

    public function set gold(value:int):void {
        goldTextField.gold = value;
        posGoldTextField();
    }

    private function posGoldTextField():void {
        goldTextField.x = layout.stageWidth - layout.minigap - goldTextField.width;
        goldTextField.y = layout.minigap + (layout.headerAvatarSize - goldTextField.height) / 2;
    }
}
}
