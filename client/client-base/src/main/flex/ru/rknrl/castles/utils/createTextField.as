package ru.rknrl.castles.utils {
import flash.text.TextFieldAutoSize;
import flash.text.TextFormat;

public function createTextField(textFormat:TextFormat, text:String = null):Label {
    const textField:Label = new Label();
    textField.embedFonts = true;
    textField.selectable = false;
    textField.autoSize = TextFieldAutoSize.LEFT;
    textField.defaultTextFormat = textFormat;
    if (text) textField.text = text;
    return textField;
}
}
