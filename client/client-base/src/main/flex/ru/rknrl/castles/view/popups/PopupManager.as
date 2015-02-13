//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.popups {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.test;
import ru.rknrl.utils.Tweener;

public class PopupManager extends Sprite {
    private static const speed:Number = 100;
    private const tweener:Tweener = new Tweener(speed);

    public function PopupManager(layout:Layout) {
        this.layout = layout;
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;
        if (modalScreen) modalScreen.layout = _layout;
        if (popup) popup.layout = _layout;
    }

    private var modalScreen:ModalScreen;
    private var popup:Popup;

    public function open(popup:Popup):void {
        if (modalScreen) throw new Error("modalScreen already exists");
        if (this.popup) throw new Error(this.popup + " already exists");
        addChild(modalScreen = new ModalScreen(_layout));
        addChild(this.popup = popup);

        tweener.value = 0;
        tweener.nextValue = 1;
        updateTransition();

        mouseChildren = true;
    }

    public function close(event:Event = null):void {
        mouseChildren = false;
        tweener.nextValue = 0;
    }

    test function openImmediate():void {
        tweener.value = tweener.nextValue = 1;
        updateTransition();
    }

    test function closeImmediate():void {
        removePopup();
    }

    private function removePopup():void {
        if (!popup) throw new Error("no popup to close");
        removeChild(popup);
        popup = null;

        if (!modalScreen) throw new Error("no modalScreen to close");
        removeChild(modalScreen);
        modalScreen = null;
    }

    private function onEnterFrame(event:Event):void {
        updateTransition();
    }

    private function updateTransition():void {
        tweener.update(getTimer());

        if (modalScreen) modalScreen.transition = tweener.value;
        if (popup) popup.transition = tweener.value;

        if (tweener.nextValue == 0 && tweener.value == 0 && popup) removePopup();
    }

    public function animatePrice():void {
        if (popup) popup.animatePrice();
    }
}
}
