package ru.rknrl.castles.game.ui {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.geom.Matrix;
import flash.geom.Rectangle;
import flash.utils.getTimer;

import ru.rknrl.castles.game.layout.GameLayout;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.ItemType;
import ru.rknrl.utils.centerize;

public class GameItem extends Sprite {
    private var _itemType:ItemType;

    public function get itemType():ItemType {
        return _itemType;
    }

    private var icon:Sprite;
    private var cooldownIcon:Bitmap;
    private var countTextField:Label;

    private var originalIconWidth:Number;
    private var originalIconHeight:Number;
    private var ratio:Number;

    public function GameItem(itemType:ItemType, layout:GameLayout, millisTillCooldownEnd:int, time:int, cooldownDuration:int, count:int) {
        _itemType = itemType;

        mouseChildren = false;

        addChild(icon = createIcon(itemType));
        originalIconWidth = icon.width;
        originalIconHeight = icon.height;

        addChild(cooldownIcon = new Bitmap(new BitmapData(originalIconWidth, originalIconHeight, true, 0)));
        cooldownIcon.x = -originalIconWidth / 2;
        cooldownIcon.y = -originalIconHeight / 2;

        addChild(countTextField = createTextField(Layout.gameItemCounterTextFormat));

        updateLayout(layout);
        update(millisTillCooldownEnd, time, cooldownDuration, count);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function updateLayout(layout:GameLayout):void {
        ratio = layout.gameItemSize / icon.height;
        updateScale();
    }

    private static function createIcon(itemType:ItemType):Sprite {
        const icon:Sprite = Utils.getItemIcon(itemType);
        icon.transform.colorTransform = Colors.colorToTransform(Colors.red);
        return icon;
    }

    private var _selected:Boolean;

    public function set selected(value:Boolean):void {
        _selected = value;
        updateScale();
    }

    private function updateScale():void {
        scaleX = scaleY = _selected ? ratio * 1.5 : ratio;
    }

    private var _count:int;
    private var cooldownDuration:int;
    private var startTime:int;
    private var endTime:int;

    public function update(millisTillCooldownEnd:int, time:int, cooldownDuration:int, count:int):void {
        if (_count > count) unlock();
        _count = count;

        countTextField.text = count.toString();
        centerize(countTextField);

        this.cooldownDuration = cooldownDuration;
        endTime = time + millisTillCooldownEnd;
        startTime = endTime - cooldownDuration;
    }

    public function canUse(time:int):Boolean {
        return !_lock && _count > 0 && time >= endTime;
    }

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();

        if (_lock && _count == 0) {
            icon.alpha = 0.2;
            cooldownIcon.visible = false;
        } else if (time >= endTime) {
            icon.alpha = 1;
            cooldownIcon.visible = false;
        } else {
            icon.alpha = 0.2;
            cooldownIcon.visible = true;

            const progress:Number = (time - startTime) / cooldownDuration;

            const matrix:Matrix = new Matrix();
            matrix.translate(originalIconWidth / 2, originalIconHeight / 2);
            const clipRect:Rectangle = new Rectangle(0, originalIconHeight - originalIconHeight * progress, originalIconWidth, originalIconHeight);
            cooldownIcon.bitmapData.fillRect(cooldownIcon.bitmapData.rect, 0);
            cooldownIcon.bitmapData.draw(icon, matrix, Colors.colorToTransform(Colors.red), null, clipRect);
        }
    }


    private var _lock:Boolean;

    public function lock():void {
        _lock = true;
    }

    public function unlock():void {
        _lock = false;
    }
}
}
