package ru.rknrl.castles.view.game.ui.magicItems {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.geom.Rectangle;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.Animated;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.ItemType;
import ru.rknrl.utils.centerize;

public class GameMagicItemIcon extends Animated {
    private var backIcon:DisplayObject;
    private var frontIcon:DisplayObject;
    private var textField:TextField;

    public function GameMagicItemIcon(itemType:ItemType, count:int) {
        addChild(backIcon = Fla.createItem(itemType));
        backIcon.transform.colorTransform = Colors.transform(Colors.light(Colors.item(itemType)));

        addChild(frontIcon = Fla.createItem(itemType));
        frontIcon.transform.colorTransform = Colors.transform(Colors.item(itemType));

        addChild(textField = createTextField(Fonts.magicItemNumber));

        this.count = count;
    }

    private var _count:int;

    public function set count(value:int):void {
        _count = value;
        textField.text = value.toString();
        centerize(textField);
        frontIcon.visible = _count > 0;
        backIcon.visible = _count == 0;
    }

    public function set cooldownProgress(value:Number):void {
        if (_count > 0) {
            const border:Number = 4;
            const w:Number = backIcon.width + border;
            const h:Number = backIcon.height + border;
            const left:Number = -w / 2;
            const top:Number = -h / 2 + h * (1 - value);
            frontIcon.scrollRect = new Rectangle(left, top, w, h * value);
            frontIcon.x = left;
            frontIcon.y = top;
            backIcon.visible = value < 1;
        }
    }
}
}
