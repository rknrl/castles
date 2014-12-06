package ru.rknrl.castles.menu.screens {
import flash.display.Shape;
import flash.display.Sprite;
import flash.events.Event;
import flash.text.TextField;
import flash.text.TextFormat;
import flash.utils.getTimer;

import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.utils.changeTextFormat;
import ru.rknrl.utils.createTextField;

public class LoadingScreen extends Screen {
    private var color:uint;
    private var holder:Sprite;
    private var progressBar:Shape;
    private var label:TextField;

    public function LoadingScreen(text:String, textFormat:TextFormat, layout:Layout) {
        color = Colors.randomColor();

        addChild(holder = new Sprite());
        holder.addChild(progressBar = new Shape());
        holder.addChild(label = createTextField(textFormat, text));
        label.textColor = color;

        updateLayout(layout, textFormat);

        addEventListener(Event.ENTER_FRAME, onEnterFrame);

        super();
    }

    public function updateLayout(layout:Layout, textFormat:TextFormat):void {
        holder.x = layout.stageCenterX;
        holder.y = layout.stageCenterY;

        const size:int = layout.progressBarSize;
        progressBar.graphics.clear();
        progressBar.graphics.beginFill(color);
        progressBar.graphics.drawRect(-size / 2, -size / 2, size, size);
        progressBar.graphics.endFill();
        progressBar.x = 0;
        progressBar.y = -size;

        changeTextFormat(label, textFormat);
        label.x = -label.width / 2;
        label.y = 0;
    }

    private function onEnterFrame(event:Event):void {
        progressBar.rotation = getTimer() / 4;
    }

    override protected function set inTransition(value:Number):void {
        holder.scaleX = holder.scaleY = value;
    }
}
}