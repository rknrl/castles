//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.bank {
import flash.display.Shape;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.Animated;
import ru.rknrl.castles.view.utils.LockView;
import ru.rknrl.castles.view.utils.applyStarTextFormat;
import ru.rknrl.display.centerize;
import ru.rknrl.display.createTextField;

public class Button extends Sprite {
    private var holder:Animated;
    private var rect:Shape;
    private var textField:TextField;
    private var lockView:LockView;

    public function Button(layout:Layout) {
        mouseChildren = false;
        addChild(holder = new Animated());
        holder.addChild(rect = new Shape());
        holder.addChild(textField = createTextField(Fonts.button));
        holder.addChild(lockView = new LockView());
        this.layout = layout;
    }

    public function set layout(value:Layout):void {
        lockView.scaleX = lockView.scaleY = value.scale;

        textField.scaleX = textField.scaleY = value.scale;
        centerize(textField);

        const buttonWidth:Number = value.buttonWidth(textField.width);
        const buttonHeight:Number = value.buttonHeight;
        rect.graphics.clear();
        rect.graphics.beginFill(Colors.magenta);
        rect.graphics.drawRoundRect(-buttonWidth / 2, -buttonHeight / 2, buttonWidth, buttonHeight, value.corner, value.corner);
        rect.graphics.endFill();
    }

    public function set text(value:String):void {
        textField.text = value;
        applyStarTextFormat(textField);
        centerize(textField);
    }

    public function set lock(value:Boolean):void {
        textField.visible = !value;
        lockView.visible = value;
        mouseEnabled = !value;
    }

    public function animate():void {
        holder.bounce();
    }
}
}
