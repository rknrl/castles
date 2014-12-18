package ru.rknrl.castles.menu.slider {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.Sprite;

import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.castles.utils.layout.LayoutPortrait;

public class ScreenSliderDebug {
    public static function lanscape(layout:LayoutLandscape):Sprite {
        const sprite:Sprite = new Sprite();

        const title:Bitmap = new Bitmap(new BitmapData(layout.bodyWidth, layout.titleHeight, true, 0x55005500));
        title.y = layout.titleTop;
        sprite.addChild(title);

        const body:Bitmap = new Bitmap(new BitmapData(layout.bodyWidth, layout.bodyHeight, true, 0x55000022));
        body.y = layout.bodyTop;
        sprite.addChild(body);

        return sprite;
    }

    public static function portrait(layout:LayoutPortrait):Sprite {
        const sprite:Sprite = new Sprite();

        const header:Bitmap = new Bitmap(new BitmapData(layout.stageWidth, layout.headerHeight, true, 0x55000022));
        header.y = 0;
        sprite.addChild(header);

        const title:Bitmap = new Bitmap(new BitmapData(layout.stageWidth, layout.titleHeight, true, 0x55005500));
        title.y = layout.titleTop;
        sprite.addChild(title);

        const body:Bitmap = new Bitmap(new BitmapData(layout.stageWidth, layout.bodyHeight, true, 0x55000022));
        body.y = layout.bodyTop;
        sprite.addChild(body);

        const footer:Bitmap = new Bitmap(new BitmapData(layout.stageWidth, layout.footerHeight, true, 0x55000022));
        footer.y = layout.footerTop;
        sprite.addChild(footer);

        return sprite;
    }
}
}
