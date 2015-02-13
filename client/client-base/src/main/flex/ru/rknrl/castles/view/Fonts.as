//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view {
import flash.text.TextFormat;
import flash.text.TextFormatAlign;

public class Fonts {
    private static const regularFont:RegularFont = new RegularFont();
    private static const boldFont:BoldFont = new BoldFont();
    private static const lightFont:LightFont = new LightFont();
    private static const starFont:StarFont = new StarFont();

    private static function regular(size:Number, color:Object = null):TextFormat {
        return new TextFormat(regularFont.fontName, size, color);
    }

    private static function regularCenter(size:Number):TextFormat {
        return new TextFormat(regularFont.fontName, size, null, null, null, null, null, null, TextFormatAlign.CENTER);
    }

    private static function bold(size:Number, color:Object = null):TextFormat {
        return new TextFormat(boldFont.fontName, size, color, true);
    }

    private static function light(size:Number, color:Object = null):TextFormat {
        return new TextFormat(lightFont.fontName, size, color);
    }

    private static function lightCenter(size:Number):TextFormat {
        return new TextFormat(lightFont.fontName, size, null, null, null, null, null, null, TextFormatAlign.CENTER);
    }

    public static const balance:TextFormat = regular(18, Colors.grey);
    public static const title:TextFormat = regular(18, Colors.grey);
    public static const loading:TextFormat = regularCenter(30);
    public static const magicItemNumber:TextFormat = bold(18, 0xffffff);
    public static const play:TextFormat = regular(24, Colors.darkGrey);
    public static const skillName:TextFormat = regular(18);
    public static const button:TextFormat = regular(18, 0xffffff);

    public static const gameAvatarPortrait:TextFormat = light(18);
    public static const gameAvatarLandscape:TextFormat = lightCenter(30);

    public static const buildingNumber:TextFormat = bold(20, 0xfffffff);
    public static const unitNumber:TextFormat = regular(20);

    public static const popupCancel:TextFormat = regular(20);
    public static const popupTitle:TextFormat = regular(20);
    public static const popupText:TextFormat = light(20);
    public static const popupPrice:TextFormat = regular(18, Colors.grey);

    public static const star:TextFormat = new TextFormat(starFont.fontName);
}
}
