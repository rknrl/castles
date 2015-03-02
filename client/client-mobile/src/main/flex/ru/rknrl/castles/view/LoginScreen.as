//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.utils.Align;
import ru.rknrl.utils.createTextField;

public class LoginScreen extends Sprite {
    public static const DEVICE_ID:String = "DEVICE_ID";
    public static const FACEBOOK:String = "FACEBOOK";
    public static const VKONTAKTE:String = "VKONTAKTE";
    public static const ODNOKLASSNIKI:String = "ODNOKLASSNIKI";
    public static const MOI_MIR:String = "MOI_MIR";

    private var iconsHolder:Sprite;
    private const icons:Vector.<Sprite> = new <Sprite>[];
    private var laterTextField:TextField;

    public function LoginScreen(layout:Layout/*, locale:CastlesLocale*/) {
        addChild(iconsHolder = new Sprite());

        addIcon(new SocialIcon(FACEBOOK, new IconFB()));
        addIcon(new SocialIcon(VKONTAKTE, new IconVK()));
        addIcon(new SocialIcon(ODNOKLASSNIKI, new IconOK()));
        addIcon(new SocialIcon(MOI_MIR, new IconMM()));

        laterTextField = createTextField(Fonts.title);
        laterTextField.text = "Залогинться позже";
        addChild(laterTextField);
        laterTextField.addEventListener(MouseEvent.MOUSE_DOWN, onLaterClick);

        this.layout = layout;
    }

    public function addIcon(icon:SocialIcon):void {
        icons.push(icon);
        iconsHolder.addChild(icon);
        icon.addEventListener(MouseEvent.MOUSE_DOWN, onIconClick);
    }

    public function set layout(value:Layout):void {
        Align.horizontal(Vector.<DisplayObject>(icons), Layout.itemSize, Layout.itemGap);
        iconsHolder.scaleX = iconsHolder.scaleY = value.scale;
        iconsHolder.x = value.screenCenterX - iconsHolder.width / 2;
        iconsHolder.y = value.contentCenterY;

        laterTextField.scaleX = laterTextField.scaleY = value.scale;
        const titlePos:Point = value.title(laterTextField.width, laterTextField.height);
        laterTextField.x = titlePos.x;
        laterTextField.y = titlePos.y;
    }

    private function onIconClick(event:MouseEvent):void {
        const icon:SocialIcon = SocialIcon(event.target);
        dispatchEvent(new LoginEvent(icon.socialName));
    }

    private function onLaterClick(event:MouseEvent):void {
        dispatchEvent(new LoginEvent(DEVICE_ID));
    }
}
}