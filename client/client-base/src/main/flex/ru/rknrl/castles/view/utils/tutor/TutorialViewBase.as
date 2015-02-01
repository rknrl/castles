package ru.rknrl.castles.view.utils.tutor {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.popups.ModalScreen;

public class TutorialViewBase extends Sprite {
    private var modalScreen:ModalScreen;

    public function TutorialViewBase(layout:Layout) {
        addChild(modalScreen = new ModalScreen(layout));

        visible = false;

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
        _layout = layout;
    }

    private var _layout:Layout;

    public function get layout():Layout {
        return _layout;
    }

    public function set layout(value:Layout):void {
        _layout = value;
        modalScreen.layout = _layout;
    }

    protected final function openImpl():void {
        if (visible) throw new Error("tutor already visible");
        visible = true;

        transition = 0;
        nextTransition = 1;
        updateTransition();
    }

    protected final function closeImpl(event:Event = null):void {
        nextTransition = 0;
    }

    private function closeImmediate():void {
        if (!visible) throw new Error("tutor not visible");
        visible = false;
    }

    public final function get playing():Boolean {
        return visible;
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
            transition += delta * Math.min(1, deltaTime / speed);
        }

        if (modalScreen) alpha = transition;

        if (nextTransition == 0 && transition == 0 && visible) closeImmediate();
    }
}
}
