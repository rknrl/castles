//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.ui.magicItems {
import flash.display.DisplayObject;
import flash.geom.Rectangle;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.utils.Animated;
import protos.ItemType;
import ru.rknrl.display.centerize;
import ru.rknrl.display.createTextField;

public class GameMagicItemIcon extends Animated {
    private var backIcon:DisplayObject;
    private var frontIcon:DisplayObject;
    private var textField:TextField;

    public function GameMagicItemIcon(itemType:ItemType, count:int) {
        addChild(backIcon = Fla.createItem(itemType));
        backIcon.transform.colorTransform = Colors.transform(Colors.light(Colors.item(itemType)));

        addChild(frontIcon = Fla.createItem(itemType));
        frontIcon.transform.colorTransform = Colors.transform(Colors.item(itemType));

        addChild(textField = createTextField(Fonts.magicItemNumber));

        this.count = count;
    }

    private var _count:int;

    public function set count(value:int):void {
        _count = value;
        textField.text = value.toString();
        centerize(textField);
        updateLock();
    }

    public function set cooldownProgress(value:Number):void {
        if (!locked) {
            const border:Number = 4;
            const w:Number = backIcon.width + border;
            const h:Number = backIcon.height + border;
            const left:Number = -w / 2;
            const top:Number = -h / 2 + h * (1 - value);
            frontIcon.scrollRect = new Rectangle(left, top, w, h * value);
            frontIcon.x = left;
            frontIcon.y = top;
            backIcon.visible = value < 1;
        }
    }

    private var _tutorLock:Boolean;

    public function set tutorLock(value:Boolean):void {
        _tutorLock = value;
        updateLock();
    }

    public function get locked():Boolean {
        return _count == 0 || _tutorLock
    }

    private function updateLock():void {
        frontIcon.visible = !locked;
        backIcon.visible = locked;
    }
}
}
