package ru.rknrl.castles.menu.screens.main.startLocation.objects {
import flash.display.Bitmap;
import flash.display.Sprite;

import ru.rknrl.castles.utils.Shadow;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.SlotId;
import ru.rknrl.funnyUi.Lock;

public class MenuGround extends Sprite {
    private var _slotId:SlotId;

    public function get slotId():SlotId {
        return _slotId;
    }

    private var shadow:Shadow;
    private var lockView:Lock;

    public function MenuGround(slotId:SlotId) {
        _slotId = slotId;

        mouseChildren = false;

        const size:int = CellSize.SIZE.id();

        const mouseHolder:Bitmap = new Bitmap(Utils.transparent);
        mouseHolder.width = size;
        mouseHolder.height = size;
        mouseHolder.x = -size / 2;
        mouseHolder.y = -size / 2;
        addChild(mouseHolder);

        shadow = new Shadow();
        shadow.y = Layout.shadowY;
        addChild(shadow);

        addChild(lockView = new Lock());
        lockView.visible = false;
    }

    public function set hasBuilding(value:Boolean):void {
        shadow.visible = !value;
    }

    public function set lock(value:Boolean):void {
        lockView.visible = value;
        mouseEnabled = !value;
    }
}
}
