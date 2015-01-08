package ru.rknrl.castles.view.menu.shop {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.centerize;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.dto.ItemType;

public class ShopMagicItemIcon extends Sprite {
    private var textField:TextField;

    public function ShopMagicItemIcon(itemType:ItemType, count:int) {
        const icon:DisplayObject = Fla.createItem(itemType);
        icon.transform.colorTransform = Colors.transform(Colors.item(itemType));
        addChild(icon);

        addChild(textField = createTextField(Fonts.magicItemNumber));

        this.count = count;
    }

    public function set count(value:int):void {
        textField.text = value.toString();
        centerize(textField);
    }
}
}
