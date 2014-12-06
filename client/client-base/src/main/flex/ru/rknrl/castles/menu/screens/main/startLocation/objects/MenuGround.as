package ru.rknrl.castles.menu.screens.main.startLocation.objects {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.dto.SlotId;
import ru.rknrl.funnyUi.Lock;

public class MenuGround extends Sprite {
    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    private var bitmap:Bitmap;
    private var lockView:Lock;

    public function MenuGround(slotId:SlotId, width:int, height:int) {
        _slotId = slotId;

        mouseChildren = false;

        bitmap = new Bitmap(Colors.groundColor);
        bitmap.width = width;
        bitmap.height = height;
        bitmap.x = -width / 2;
        bitmap.y = -height / 2;
        addChild(bitmap);

        addChild(lockView = new Lock());
        lockView.visible = false
    }

    public function set bitmapData(value:BitmapData):void {
        bitmap.bitmapData = value;
    }

    public function lock():void {
        lockView.visible = true;
        mouseEnabled = false;
    }

    public function unlock():void {
        lockView.visible = false;
        mouseEnabled = true;
    }
}
}
