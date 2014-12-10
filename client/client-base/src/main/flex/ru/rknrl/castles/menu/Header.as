package ru.rknrl.castles.menu {
import flash.display.BitmapData;
import flash.display.Shape;
import flash.display.Sprite;

import ru.rknrl.BitmapUtils;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.funnyUi.GoldTextField;
import ru.rknrl.utils.changeTextFormat;

public class Header extends Sprite {
    private var avatar:Shape;
    private var nameTextField:Label;
    private var goldTextField:GoldTextField;
    private var layout:Layout;

    public function Header(gold:int, layout:Layout) {
        mouseChildren = false;
        addChild(avatar = BitmapUtils.createCircleShape(new BitmapData(64, 64, false, 0)));
        addChild(nameTextField = createTextField(layout.headerTextFormat, "Толя Янот"));
        addChild(goldTextField = new GoldTextField("", layout.headerTextFormat, gold, Colors.magenta));
        updateLayout(layout);
        this.gold = gold;
    }

    public function updateLayout(layout:Layout):void {
        this.layout = layout;

        avatar.width = avatar.height = layout.headerAvatarSize;
        avatar.x = layout.minigap;
        avatar.y = layout.minigap;

        changeTextFormat(nameTextField, layout.headerTextFormat);
        nameTextField.x = layout.minigap + layout.headerAvatarSize + layout.minigap;
        nameTextField.y = layout.minigap + (layout.headerAvatarSize - nameTextField.height) / 2;

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
        goldTextField.y = layout.minigap + (layout.headerAvatarSize - nameTextField.height) / 2;
    }
}
}
