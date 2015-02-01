package ru.rknrl.castles.view.utils {
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.utils.getCharIndices;

public function applyStarTextFormat(textField:TextField):void {
    const indices:Vector.<int> = getCharIndices(textField.text, "★");

    for each(var index:int in indices) {
        textField.setTextFormat(Fonts.star, index, index + 1);
    }
}
}
