//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.asocial.mobile.FB;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.utils.createTextField;

public class WebViewBackground extends Sprite {
    public function WebViewBackground(layout:Layout) {
        const textField:TextField = createTextField(Fonts.title);
        textField.text = "Back to Castles";
        textField.scaleX = textField.scaleY = layout.scale;
        textField.x = (layout.screenWidth - textField.width) / 2;
        textField.y = layout.screenHeight - FB.footerHeight / 2 - textField.height / 2;
        addChild(textField);
    }
}
}
