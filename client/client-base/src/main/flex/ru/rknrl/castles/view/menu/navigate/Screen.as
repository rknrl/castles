package ru.rknrl.castles.view.menu.navigate {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.view.layout.Layout;

public class Screen extends Sprite {
    public function set layout(value:Layout):void {
    }

    public function get titleContent():DisplayObject {
        throw new Error();
    }

    public function set transition(value:Number):void {
        if (titleContent) {
            titleContent.alpha = value;
        }
    }

    public function set lock(value:Boolean):void {
    }

    public function animatePrice():void {
    }
}
}
