package ru.rknrl.castles.view.utils.tutor {
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.getTimer;

import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.popups.ModalScreen;
import ru.rknrl.utils.Tweener;

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

        tweener.value = 0;
        tweener.nextValue = 1;
        updateTransition();
    }

    protected final function closeImpl(event:Event = null):void {
        tweener.nextValue = 0;
    }

    private function closeImmediate():void {
        if (!visible) throw new Error("tutor not visible");
        visible = false;
    }

    public final function get playing():Boolean {
        return visible;
    }

    private static const speed:Number = 100;
    private const tweener:Tweener = new Tweener(speed);

    private function onEnterFrame(event:Event):void {
        updateTransition();
    }

    private function updateTransition():void {
        tweener.update(getTimer());

        if (modalScreen) alpha = tweener.value;

        if (tweener.nextValue == 0 && tweener.value == 0 && visible) closeImmediate();
    }
}
}
