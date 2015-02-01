package ru.rknrl.castles.view.popups {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.test;

public class PopupManager extends Sprite {
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
        if (this.popup) throw new Error(this.popup + " already exists");
        if (modalScreen) throw new Error("modalScreen already exists");
        addChild(modalScreen = new ModalScreen(_layout));
        addChild(this.popup = popup);

        transition = 0;
        nextTransition = 1;
        updateTransition();

        mouseChildren = true;
    }

    public function close(event:Event = null):void {
        mouseChildren = false;
        nextTransition = 0;
    }

    test function openImmediate():void {
        transition = nextTransition = 1;
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

    private static const epsilon:Number = 0.005;
    private static const speed:Number = 100;
    private var transition:Number = 0;
    private var nextTransition:Number = 0;

    private var lastTime:int;

    private function onEnterFrame(event:Event):void {
        updateTransition();
    }

    private function updateTransition():void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;

        const delta:Number = nextTransition - transition;

        if (Math.abs(delta) < epsilon) {
            transition = nextTransition;
        } else {
            transition += delta * (deltaTime / speed);
        }

        if (modalScreen) modalScreen.transition = transition;
        if (popup) popup.transition = transition;

        if (nextTransition == 0 && transition == 0 && popup) removePopup();
    }

    public function animatePrice():void {
        if (popup) popup.animatePrice();
    }
}
}
