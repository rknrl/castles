package ru.rknrl.castles.menu.screens.shop {
import flash.display.Sprite;
import flash.text.TextField;
import flash.text.TextFormat;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.ItemType;
import ru.rknrl.funnyUi.Animated;
import ru.rknrl.funnyUi.Lock;
import ru.rknrl.utils.changeTextFormat;
import ru.rknrl.utils.createTextField;

public class Item extends Animated {
    private var _itemType:ItemType;

    public function get itemType():ItemType {
        return _itemType;
    }

    private var icon:Sprite;

    private var itemWidth:int;

    private var holder:Sprite;

    private var countTextField:TextField;
    private var lockView:Lock;

    private var nameTextField:TextField;

    public function Item(itemType:ItemType, count:int, color:uint, layout:Layout, locale:CastlesLocale) {
        _itemType = itemType;
        mouseChildren = false;

        addChild(holder = new Sprite());
        holder.addChild(icon = Utils.getItemIcon(itemType));

        holder.addChild(nameTextField = addNameTextField(itemType, color, layout.shopItemNameTextFormat, locale));
        holder.addChild(countTextField = createTextField(layout.shopItemCountTextFormat));
        holder.addChild(lockView = new Lock());
        lockView.visible = false;

        this.count = count;

        updateLayout(layout);
    }

    public function updateLayout(layout:Layout):void {
        this.itemWidth = layout.shopItemWidth;

        holder.x = -itemWidth / 2;
        holder.y = -layout.shopItemHeight / 2;

        const ratio:Number = icon.width / icon.height;
        icon.x = itemWidth / 2;
        icon.y = itemWidth / 2;
        icon.width = itemWidth;
        icon.height = itemWidth / ratio;

        lockView.x = itemWidth / 2;
        lockView.y = itemWidth / 2;
        lockView.scaleX = lockView.scaleY = layout.scale;

        changeTextFormat(nameTextField, layout.shopItemNameTextFormat);
        nameTextField.x = itemWidth / 2 - nameTextField.width / 2;
        nameTextField.y = layout.shopItemHeight - nameTextField.height;

        changeTextFormat(countTextField, layout.shopItemCountTextFormat);
        centerizeCount();
    }

    private static function addNameTextField(itemType:ItemType, color:uint, textFormat:TextFormat, locale:CastlesLocale):TextField {
        const nameTextField:TextField = createTextField(textFormat, locale.getItemName(itemType));
        nameTextField.textColor = color;
        return nameTextField;
    }

    public function set color(value:uint):void {
        icon.transform.colorTransform = Colors.colorToTransform(value);
        nameTextField.textColor = value;
    }

    private var _count:int;

    public function get count():int {
        return _count;
    }

    public function set count(value:int):void {
        _count = value;
        countTextField.text = value.toString();
        centerizeCount();
    }

    private function centerizeCount():void {
        countTextField.x = itemWidth / 2 - countTextField.width / 2;
        countTextField.y = itemWidth / 2 - countTextField.height / 2;
    }

    public function lock():void {
        lockView.visible = true;
        countTextField.visible = false;
        mouseEnabled = false;
    }

    public function unlock():void {
        lockView.visible = false;
        countTextField.visible = true;
        mouseEnabled = true;
    }
}
}
