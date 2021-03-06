//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.main.popups {
import flash.events.MouseEvent;

import ru.rknrl.castles.model.events.BuildEvent;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.main.popups.icons.PopupBuildIcon;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.castles.view.popups.popup.PopupItem;
import protos.BuildingType;
import protos.SlotId;

public class BuildPopup extends Popup {
    private var slotId:SlotId;
    private var popup:Popup;
    private const items:Vector.<PopupItem> = new <PopupItem>[];

    public function BuildPopup(slotId:SlotId, price:int, layout:Layout, locale:CastlesLocale) {
        this.slotId = slotId;

        for each(var buildingType:BuildingType in BuildingType.values) {
            const item:BuildItem = new BuildItem(buildingType, layout, new PopupBuildIcon(buildingType), locale.buildingName(buildingType), price);
            item.addEventListener(MouseEvent.MOUSE_DOWN, onClick);
            items.push(item)
        }

        addChild(popup = layout.createPopup(locale.build, locale.cancel, items, layout));
    }

    override public function set layout(value:Layout):void {
        popup.layout = value;
    }

    override public function set transition(value:Number):void {
        popup.transition = value;
    }

    private function onClick(event:MouseEvent):void {
        const item:BuildItem = BuildItem(event.target);
        dispatchEvent(new BuildEvent(slotId, item.buildingType));
    }

    override public function animatePrice():void {
        for each(var item:PopupItem in items) item.animatePrices();
    }
}
}

