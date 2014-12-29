package ru.rknrl.castles.view.utils {
import flash.text.TextField;
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;

public function createTextField(textFormat:TextFormat):TextField {
    const textField:TextField = new TextField();
    textField.autoSize = TextFieldAutoSize.LEFT;
    textField.selectable = false;
    textField.embedFonts = true;
    textField.defaultTextFormat = textFormat;
    return textField;
}
}
