package ru.rknrl.castles.menu.slider {

import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;

public class MenuButton extends TextField {
    private var _id:String;

    public function get id():String {
        return _id;
    }

    private var color:uint;

    public function MenuButton(id:String, textFormat:TextFormat, text:String, color:uint) {
        _id = id;
        embedFonts = true;
        autoSize = TextFieldAutoSize.LEFT;
        selectable = false;
        defaultTextFormat = textFormat;
        this.text = text;
        this.color = color;
        backgroundColor = color;
        selected = false;
    }

    public function set selected(value:Boolean):void {
        background = value;
        textColor = value ? 0xffffff : color;
    }
}
}
