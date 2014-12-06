package ru.rknrl.castles.menu.screens.main.popup.popup {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.Utils;
import ru.rknrl.easers.interpolate;

public class Popup extends Sprite {
    private var _value:Number;

    public function get value():Number {
        return _value;
    }

    private var startValue:Number;
    private var endValue:Number;
    private var startTime:int;
    private var duration:int;
    private var moving:Boolean;

    public function Popup() {
        open();

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private function open():void {
        startValue = 0;
        _value = 0;
        endValue = 1;
        startTime = getTimer();
        duration = Utils.popupDuration;
        moving = true;
        updateValue(0);
    }

    public function close():void {
        startValue = _value;
        endValue = 0;
        startTime = getTimer();
        duration = Utils.popupDuration * _value;
        moving = true;
    }

    private function onEnterFrame(event:Event):void {
        if (moving) {
            _value = interpolate(startValue, endValue, getTimer(), startTime, duration, Utils.popupEaser);
            updateValue(_value);

            if (_value == endValue) {
                dispatchEvent(new Event(Event.COMPLETE));
                moving = false;
            }
        }
    }

    protected function updateValue(value:Number):void {
        // override me
    }
}
}
