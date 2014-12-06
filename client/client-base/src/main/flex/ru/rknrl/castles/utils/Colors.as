package ru.rknrl.castles.utils {
import flash.display.BitmapData;
import flash.geom.ColorTransform;

public class Colors {
    public static const grass:uint = 0xc3ffa7;

    public static const lightYellow:uint = 0xfcf3d4;
    public static const yellow:uint = 0xfdbd15;
    public static const darkYellow:uint = 0xd19c10;

    public static const lightMagenta:uint = 0xe8ddff;
    public static const magenta:uint = 0x9d6dff;
    public static const darkMagenta:uint = 0x8159d3;

    public static const lightCyan:uint = 0xc0fae9;
    public static const cyan:uint = 0x2ad9dc;
    public static const darkCyan:uint = 0x23b5b5;

    public static const lightRed:uint = 0xecb9bc;
    public static const red:uint = 0xfa6755;
    public static const darkRed:uint = 0xd05546;

    public static const lightGrey:uint = 0xeeeeee;
    public static const grey:uint = 0x777777;
    public static const darkGrey:uint = 0x999999;

    public static const lightColors:Vector.<uint> = new <uint>[
        lightMagenta, lightCyan, lightRed
    ];

    public static const colors:Vector.<uint> = new <uint>[
        yellow,
        magenta, darkMagenta,
        cyan, darkCyan,
        red, darkRed,
        grey, darkGrey
    ];


    public static function colorToLight(color:uint):uint {
        switch (color) {
            case yellow:
            case darkYellow:
                return lightYellow;

            case magenta:
            case darkMagenta:
                return lightMagenta;

            case cyan:
            case darkCyan:
                return lightCyan;

            case red:
            case darkRed:
                return lightRed;

            case grey:
            case darkGrey:
                return lightGrey;
        }
        throw new Error();
    }

    public static function randomLightColor():uint {
        const index:int = Math.random() * lightColors.length;
        return lightColors[index];
    }

    public static function randomColor():uint {
        const index:int = Math.random() * colors.length;
        return colors[index];
    }

    public static const groundColor:BitmapData = new BitmapData(1, 1, false, grass);
    public static const noOwnerGroundColor:BitmapData = new BitmapData(1, 1, false, lightGrey);

    public static const groundColors:Vector.<BitmapData> = new <BitmapData>[
        new BitmapData(1, 1, false, lightRed),
        new BitmapData(1, 1, false, lightMagenta),
        new BitmapData(1, 1, false, lightYellow),
        new BitmapData(1, 1, false, lightCyan)
    ];

    public static const noOwnerColor:uint = grey;
    public static const noOwnerColorTransform:ColorTransform = colorToTransform(noOwnerColor);

    public static const playerColors:Vector.<uint> = new <uint>[
        red,
        magenta,
        yellow,
        cyan
    ];

    public static const playerColorTransforms:Vector.<ColorTransform> = colorsToTransforms(playerColors);

    public static function colorsToTransforms(colors:Vector.<uint>):Vector.<ColorTransform> {
        const result:Vector.<ColorTransform> = new Vector.<ColorTransform>(colors.length, true);
        for (var i:int = 0; i < colors.length; i++) {
            result[i] = colorToTransform(colors[i]);
        }
        return result;
    }

    public static function colorToTransform(color:uint):ColorTransform {
        const r:Number = ((color >> 16) & 0xFF) / 0xFF;
        const g:Number = ((color >> 8) & 0xFF) / 0xFF;
        const b:Number = (color & 0xFF) / 0xFF;
        return new ColorTransform(r, g, b, 1)
    }
}
}
