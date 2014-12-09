package ru.rknrl.castles.menu {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

import ru.rknrl.castles.menu.screens.main.popup.PopupScreen;
import ru.rknrl.castles.menu.screens.main.popup.popup.Popup;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.easers.interpolate;

public class PopupManager extends Sprite {
    public static const START_CLOSE:String = "startClose";
    public static const END_CLOSE:String = "endClose";

    private var layout:Layout;

    public function PopupManager(layout:Layout) {
        updateLayout(layout);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    public function updateLayout(layout:Layout):void {
        this.layout = layout;
        if (popupScreen) popupScreen.updateLayout(layout)
    }

    private var popupScreen:PopupScreen;
    private var popup:Popup;

    public function openPopup(popup:Popup):void {
        if (this.popup) throw new Error("popup already open");
        if (popupScreen) throw new Error("popupScreen already open");

        popupScreen = new PopupScreen(layout);
        popupScreen.addEventListener(MouseEvent.MOUSE_DOWN, onPopupScreenClick);
        addChild(popupScreen);

        this.popup = popup;
        addChild(popup);

        playOpenAnimation();
    }

    private function onPopupScreenClick(event:Event):void {
        closePopup();
    }

    public function closePopup():void {
        if (!popup) throw new Error("popup==null");
        if (!popupScreen) throw new Error("popupScreen==null");
        popupScreen.removeEventListener(MouseEvent.MOUSE_DOWN, onPopupScreenClick);

        dispatchEvent(new Event(START_CLOSE));
        playCloseAnimation();
    }

    private function onCloseComplete():void {
        removeChild(popup);
        popup = null;

        removeChild(popupScreen);
        popupScreen = null;

        dispatchEvent(new Event(END_CLOSE));
    }

    // animation
    private var startTransition:Number;
    private var endTransition:Number;
    private var startTime:int;
    private var duration:int;
    private var inAnimation:Boolean;

    private function playOpenAnimation():void {
        startTransition = 0;
        transition = startTransition;
        endTransition = 1;
        startTime = getTimer();
        duration = Utils.popupDuration;
        inAnimation = true;
    }

    public function playCloseAnimation():void {
        startTransition = transition;
        endTransition = 0;
        startTime = getTimer();
        duration = Utils.popupDuration * transition;
        inAnimation = true;
    }

    private function onEnterFrame(event:Event):void {
        if (inAnimation) {
            transition = interpolate(startTransition, endTransition, getTimer(), startTime, duration, Utils.popupEaser);

            if (transition == endTransition) {
                inAnimation = false;
                if(endTransition == 0) onCloseComplete();
            }
        }
    }

    private var _transition:Number = 0;

    public function set transition(value:Number):void {
        _transition = value;
        popupScreen.transition = _transition;
        popup.transition = _transition;
    }

    public function get transition():Number {
        return _transition;
    }
}
}
