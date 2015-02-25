//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.utils.createTextField;

public class LoginScreen extends Sprite {
    public static const LOGIN_VIA_FACEBOOK:String = "loginViaFacebook";
    public static const LOGIN_VIA_DEVICE_ID:String = "loginViaDeviceId";

    public function LoginScreen(layout: Layout) {
        const facebookTextField:TextField = createTextField(Fonts.loading);
        facebookTextField.text = "Facebook login";
        facebookTextField.addEventListener(MouseEvent.CLICK, onFacebookClick);
        addChild(facebookTextField);

        const cancelTextField:TextField = createTextField(Fonts.loading);
        cancelTextField.text = "No, Later";
        cancelTextField.y = 300;
        cancelTextField.addEventListener(MouseEvent.CLICK, onCancelClick);
        addChild(cancelTextField)
    }

    private function onFacebookClick(event:MouseEvent):void {
        dispatchEvent(new Event(LOGIN_VIA_FACEBOOK));
    }

    private function onCancelClick(event:MouseEvent):void {
        dispatchEvent(new Event(LOGIN_VIA_DEVICE_ID));
    }
}
}
