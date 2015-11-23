//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils {
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.common.Strings;

public function applyStarTextFormat(textField:TextField):void {
    const indices:Vector.<int> = Strings.getCharIndices(textField.text, "★");

    for each(var index:int in indices) {
        textField.setTextFormat(Fonts.star, index, index + 1);
    }
}
}
