package ru.rknrl.castles.view {
import flash.display.BitmapData;
import flash.geom.ColorTransform;

import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SlotId;

public class Colors {
    public static const navigationPoint:uint = 0xdddddd;
    public static const navigationPointSelected:uint = 0xaaaaaa;

    public static const transparent:BitmapData = new BitmapData(1, 1, true, 0);

    public static const flaskFillColor:uint = 0xcccccc;

    public static const red:uint = 0xfa6755;
    public static const yellow:uint = 0xfdbd15;
    public static const grey:uint = 0x999999;
    public static const cyan:uint = 0x2ad9dc;
    public static const magenta:uint = 0x9d6dff;
    public static const colors:Vector.<uint> = new <uint>[red, yellow, grey, cyan, magenta];

    public static const redTransform:ColorTransform = colorToTransform(red);
    public static const yellowTransform:ColorTransform = colorToTransform(yellow);
    public static const greyTransform:ColorTransform = colorToTransform(grey);
    public static const cyanTransform:ColorTransform = colorToTransform(cyan);
    public static const magentaTransform:ColorTransform = colorToTransform(magenta);

    public static const colorTransforms:Vector.<ColorTransform> = new <ColorTransform>[redTransform, yellowTransform, greyTransform, cyanTransform, magentaTransform];

    public static function colorToTransform(color:uint):ColorTransform {
        const r:Number = ((color >> 16) & 0xFF) / 0xFF;
        const g:Number = ((color >> 8) & 0xFF) / 0xFF;
        const b:Number = (color & 0xFF) / 0xFF;
        return new ColorTransform(r, g, b, 1)
    }

    public static function slotIdToColorTransforms(slotId:SlotId):ColorTransform {
        switch (slotId) {
            case SlotId.SLOT_1:
                return Colors.redTransform;
            case SlotId.SLOT_2:
                return Colors.greyTransform;
            case SlotId.SLOT_3:
                return Colors.yellowTransform;
            case SlotId.SLOT_4:
                return Colors.magentaTransform;
            case SlotId.SLOT_5:
                return Colors.cyanTransform;
        }
        throw new Error("unknown slotId " + slotId);
    }

    public static function skillTypeToColor(skillType:SkillType):uint {
        switch (skillType) {
            case SkillType.ATTACK:
                return red;
            case SkillType.DEFENCE:
                return magenta;
            case SkillType.SPEED:
                return yellow;
        }
        throw new Error("unknown skillType " + skillType);
    }

    public static function skillTypeToColorTransform(skillType:SkillType):ColorTransform {
        switch (skillType) {
            case SkillType.ATTACK:
                return redTransform;
            case SkillType.DEFENCE:
                return magentaTransform;
            case SkillType.SPEED:
                return yellowTransform;
        }
        throw new Error("unknown skillType " + skillType);
    }

    public static function itemLightColorTransform(itemType:ItemType):ColorTransform {
        switch (itemType) {
            case ItemType.FIREBALL:
                return lightRedTransform;
            case ItemType.STRENGTHENING:
                return lightYellowTransform;
            case ItemType.VOLCANO:
                return lightGreyTransform;
            case ItemType.TORNADO:
                return lightCyanTransform;
            case ItemType.ASSISTANCE:
                return lightMagentaTransform;
        }
        throw new Error("unknown itemType " + itemType);
    }

    public static function itemColorTransform(itemType:ItemType):ColorTransform {
        switch (itemType) {
            case ItemType.FIREBALL:
                return redTransform;
            case ItemType.STRENGTHENING:
                return yellowTransform;
            case ItemType.VOLCANO:
                return greyTransform;
            case ItemType.TORNADO:
                return cyanTransform;
            case ItemType.ASSISTANCE:
                return magentaTransform;
        }
        throw new Error("unknown itemType " + itemType);
    }

    // game

    public static const grass:uint = 0xc3ffa7;
    public static const groundColor:BitmapData = new BitmapData(1, 1, false, grass);

    public static const lightYellow:uint = 0xfcf3d4;
    public static const lightMagenta:uint = 0xe8ddff;
    public static const lightCyan:uint = 0xc0fae9;
    public static const lightRed:uint = 0xecb9bc;
    public static const lightGrey:uint = 0xeeeeee;

    public static const lightRedTransform:ColorTransform = colorToTransform(lightRed);
    public static const lightYellowTransform:ColorTransform = colorToTransform(lightYellow);
    public static const lightGreyTransform:ColorTransform = colorToTransform(lightGrey);
    public static const lightCyanTransform:ColorTransform = colorToTransform(lightCyan);
    public static const lightMagentaTransform:ColorTransform = colorToTransform(lightMagenta);

    public static const noOwnerGroundColor:BitmapData = new BitmapData(1, 1, false, lightGrey);

    public static const groundColors:Vector.<BitmapData> = new <BitmapData>[
        new BitmapData(1, 1, false, lightYellow),
        new BitmapData(1, 1, false, lightCyan),
        new BitmapData(1, 1, false, lightMagenta),
        new BitmapData(1, 1, false, lightRed)
    ];

    public static const noOwnerTransform:ColorTransform = greyTransform;

    public static const playerTransforms:Vector.<ColorTransform> = new <ColorTransform>[
        yellowTransform,
        cyanTransform,
        magentaTransform,
        redTransform
    ];

    public static const playerColors:Vector.<uint> = new <uint>[
        yellow,
        cyan,
        magenta,
        red
    ];
}
}
