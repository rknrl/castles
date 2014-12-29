package ru.rknrl.castles.view.loading {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.text.TextField;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.Shadow;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class LoadingScreen extends Sprite {
    private var holder:Sprite;

    public function LoadingScreen(text:String, layout:Layout) {
        addChild(holder = new Sprite());

        holder.addChild(new Shadow());

        const tower:DisplayObject = Fla.createBuilding(BuildingType.TOWER, BuildingLevel.LEVEL_3);
        tower.transform.colorTransform = Colors.yellowTransform;
        holder.addChild(tower);

        const textField:TextField = new TextField();
        textField.selectable = false;
        textField.embedFonts = true;
        textField.defaultTextFormat = Fonts.loading;
        textField.textColor = Colors.yellow;
        textField.text = text;
        textField.width = 200;
        textField.wordWrap = true;
        textField.x = -textField.width / 2;
        textField.y = 10;
        holder.addChild(textField);

        this.layout = layout;
    }

    public function set layout(value:Layout):void {
        holder.scaleX = holder.scaleY = value.scale;
        holder.x = value.screenCenterX;
        holder.y = value.contentCenterY;
    }
}
}
