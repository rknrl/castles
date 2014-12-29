package ru.rknrl.castles.view.menu.main.popups {
import flash.events.MouseEvent;

import ru.rknrl.castles.model.events.RemoveBuildingEvent;
import ru.rknrl.castles.model.events.UpgradeBuildingEvent;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.main.popups.icons.RemoveIcon;
import ru.rknrl.castles.view.menu.main.popups.icons.UpgradeIcon;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.castles.view.popups.popup.PopupItem;
import ru.rknrl.dto.SlotId;

public class UpgradePopup extends Popup {
    private var slotId:SlotId;
    private var popup:Popup;

    public function UpgradePopup(slotId:SlotId, canUpgrade:Boolean, canRemove:Boolean, upgradePrice:int, layout:Layout, locale:CastlesLocale) {
        this.slotId = slotId;
        const items:Vector.<PopupItem> = new <PopupItem>[];

        if (canUpgrade) {
            const upgradeItem:PopupItem = new PopupItem(layout, new UpgradeIcon(), locale.upgrade, upgradePrice);
            upgradeItem.addEventListener(MouseEvent.CLICK, onUpgradeClick);
            items.push(upgradeItem);
        }

        if (canRemove) {
            const deleteItem:PopupItem = new PopupItem(layout, new RemoveIcon(), locale.remove, 0);
            deleteItem.addEventListener(MouseEvent.CLICK, onRemoveClick);
            items.push(deleteItem);
        }

        addChild(popup = layout.createPopup(locale.build, locale.cancel, items, layout));
    }

    override public function set layout(value:Layout):void {
        popup.layout = value;
    }

    override public function set transition(value:Number):void {
        popup.transition = value;
    }

    private function onUpgradeClick(event:MouseEvent):void {
        dispatchEvent(new UpgradeBuildingEvent(slotId));
    }

    private function onRemoveClick(event:MouseEvent):void {
        dispatchEvent(new RemoveBuildingEvent(slotId));
    }
}
}


