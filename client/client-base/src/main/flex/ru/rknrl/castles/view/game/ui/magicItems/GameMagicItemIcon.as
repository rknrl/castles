package ru.rknrl.castles.view.game.ui.magicItems {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.geom.Rectangle;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.centerize;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.ItemType;

public class GameMagicItemIcon extends Sprite {
    private var backIcon:DisplayObject;
    private var frontIcon:DisplayObject;
    private var textField:TextField;

    public function GameMagicItemIcon(itemType:ItemType, count:int) {
        backIcon = Fla.createItem(itemType);
        backIcon.transform.colorTransform = Colors.itemLightColorTransform(itemType);
        addChild(backIcon);

        frontIcon = Fla.createItem(itemType);
        frontIcon.transform.colorTransform = Colors.itemColorTransform(itemType);
        addChild(frontIcon);

        textField = createTextField(Fonts.magicItemNumber);
        addChild(textField);

        this.count = count;
    }

    public function set count(value:int):void {
        textField.text = value.toString();
        centerize(textField);
    }

    public function set cooldownProgress(value:Number):void {
        frontIcon.scrollRect = new Rectangle(0, 0, frontIcon.width, frontIcon.height * value);
        backIcon.visible = value == 1;
    }
}
}
