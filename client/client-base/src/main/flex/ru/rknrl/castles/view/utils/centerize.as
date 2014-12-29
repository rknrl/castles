package ru.rknrl.castles.view.utils {
import flash.display.DisplayObject;

public function centerize(displayObject:DisplayObject):void {
    displayObject.x = -displayObject.width / 2;
    displayObject.y = -displayObject.height / 2;
}
}
