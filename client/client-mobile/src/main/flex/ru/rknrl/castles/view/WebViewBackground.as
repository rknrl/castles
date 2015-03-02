//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view {
import flash.display.Bitmap;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.asocial.Social;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.utils.createTextField;

public class WebViewBackground extends Sprite {
    public static const LOGIN_CANCEL:String = "loginCancel";

    private var mouseHolder:Bitmap;
    private var textField:TextField;

    public function WebViewBackground(layout:Layout/*, locale:CastlesLocale*/) {
        addChild(mouseHolder = new Bitmap(Colors.transparent));
        addEventListener(MouseEvent.MOUSE_DOWN, onClick);

        textField = createTextField(Fonts.title);
        textField.text = "Back to Castles";
        addChild(textField);

        this.layout = layout;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(LOGIN_CANCEL));
    }

    public function set layout(value:Layout):void {
        textField.scaleX = textField.scaleY = value.scale;
        textField.x = (value.screenWidth - textField.width) / 2;
        textField.y = value.screenHeight - Social.footerHeight / 2 - textField.height / 2;

        mouseHolder.width = value.screenWidth;
        mouseHolder.height = value.screenHeight;
    }
}
}
