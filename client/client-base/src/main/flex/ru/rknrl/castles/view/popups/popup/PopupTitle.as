//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.popups.popup {
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.display.createTextField;

public class PopupTitle extends Sprite {
    private var textField:TextField;

    public function PopupTitle(text:String, width:Number, layout:Layout) {
        addChild(textField = createTextField(Fonts.popupTitle));
        textField.text = text;

        setLayout(width, layout);
    }

    public function setLayout(width:Number, value:Layout):void {
        textField.scaleX = textField.scaleY = value.scale;
        textField.x = (width - textField.width) / 2;
        textField.y = value.popupTitleTextY(textField.height);
    }
}
}
