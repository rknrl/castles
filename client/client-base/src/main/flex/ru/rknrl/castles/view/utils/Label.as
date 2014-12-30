package ru.rknrl.castles.view.utils {
import flash.text.TextField;
import flash.text.TextFormat;

import ru.rknrl.utils.getCharIndices;

public class Label extends TextField {
    override public function set defaultTextFormat(format:TextFormat):void {
        super.defaultTextFormat = format;
        applyStarTextFormat();
    }

    override public function set text(value:String):void {
        super.text = value;
        applyStarTextFormat();
    }

    private static const starFont:StarFont = new StarFont();
    private static const starTextFormat:TextFormat = new TextFormat(starFont.fontName);

    private function applyStarTextFormat():void {
        const indices:Vector.<int> = getCharIndices(text, "★");

        for each(var index:int in indices) {
            setTextFormat(starTextFormat, index, index + 1);
        }
    }
}
}
