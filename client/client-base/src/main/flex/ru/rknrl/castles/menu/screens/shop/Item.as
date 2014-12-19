package ru.rknrl.castles.menu.screens.shop {
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.Shadow;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.animation.Fly;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.ItemType;
import ru.rknrl.funnyUi.Animated;
import ru.rknrl.funnyUi.Lock;
import ru.rknrl.utils.centerize;

public class Item extends Animated {
    public static const SIZE:int = 48;

    private var _itemType:ItemType;

    public function get itemType():ItemType {
        return _itemType;
    }

    private var countTextField:Label;
    private var lockView:Lock;

    private var fly:Fly;

    public function Item(itemType:ItemType, count:int, color:uint) {
        _itemType = itemType;
        mouseChildren = false;

        const holder:Sprite = new Sprite();
        addChild(holder);

        const icon:Sprite = Utils.getItemIcon(itemType);
        icon.transform.colorTransform = Colors.colorToTransform(color);
        holder.addChild(icon);

        holder.addChild(countTextField = createTextField(Layout.shopItemCountTextFormat));

        holder.addChild(lockView = new Lock());
        lockView.visible = false;

        const shadow:Shadow = new Shadow();
        shadow.y = SIZE;
        addChild(shadow);

        fly = new Fly(holder, shadow);

        this.count = count;

        addEventListener(Event.ENTER_FRAME, enterFrameHandler);
    }

    private var _count:int;

    public function get count():int {
        return _count;
    }

    public function set count(value:int):void {
        _count = value;
        countTextField.text = value.toString();
        centerize(countTextField);
    }

    public function set lock(value:Boolean):void {
        lockView.visible = value;
        countTextField.visible = !value;
        mouseEnabled = !value;
    }

    private function enterFrameHandler(e:Event):void {
        fly.onEnterFrame();
    }
}
}
