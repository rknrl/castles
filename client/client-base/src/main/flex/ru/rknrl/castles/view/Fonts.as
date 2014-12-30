package ru.rknrl.castles.view {
import flash.text.TextFormat;
import flash.text.TextFormatAlign;

public class Fonts {
    private static const regularFont:LightFont = new LightFont();
    private static const boldFont:BoldFont = new BoldFont();

    private static function regular(size:Number, color:Object = null):TextFormat {
        return new TextFormat(regularFont.fontName, size, color);
    }

    private static function regularCenter(size:Number):TextFormat {
        return new TextFormat(regularFont.fontName, size, null, null, null, null, null, null, TextFormatAlign.CENTER);
    }

    private static function bold(size:Number, color:Object = null):TextFormat {
        return new TextFormat(boldFont.fontName, size, color, true);
    }

    public static const skillName:TextFormat = regular(18);
    public static const magicItemNumber:TextFormat = bold(18, 0xffffff);
    public static const balance:TextFormat = regular(18, 0xaaaaaa);
    public static const title:TextFormat = regular(18, 0xaaaaaa);
    public static const button:TextFormat = regular(18, 0xffffff);
    public static const play:TextFormat = regular(20, 0xaaaaaa);
    public static const loading:TextFormat = regularCenter(30);
    public static const gameAvatar:TextFormat = regular(18);

    public static const buildingNumber:TextFormat = bold(20, 0xfffffff);
    public static const unitNumber:TextFormat = regular(18);

    public static const popupCancel:TextFormat = regular(20);
    public static const popupTitle:TextFormat = regular(20);
    public static const popupText:TextFormat = regular(20);
    public static const popupPrice:TextFormat = regular(20, 0xaaaaaa);

    private static const starFont:StarFont = new StarFont();
    public static const starTextFormat:TextFormat = new TextFormat(starFont.fontName);
}
}
