//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view {
import flash.display.BitmapData;
import flash.geom.ColorTransform;
import flash.utils.Dictionary;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SlotId;

public class Colors {

    // color values

//    public static const transparent:BitmapData = new BitmapData(1, 1, true, 0x44444444);
    public static const transparent:BitmapData = new BitmapData(1, 1, true, 0);

    public static const flaskFillColor:uint = 0xcccccc;

    public static const red:uint = 0xfa6654;
    public static const lightRed:uint = 0xecb8bb;

    public static const yellow:uint = 0xfdbc14;
    public static const lightYellow:uint = 0xfcf3d4;

    public static const grey:uint = 0x999999;
    public static const lightGrey:uint = 0xeeeeee;
    public static const darkGrey:uint = 0x767676;

    public static const cyan:uint = 0x29d9dc;
    public static const lightCyan:uint = 0xbffae9;

    public static const magenta:uint = 0x9c6cff;
    public static const lightMagenta:uint = 0xe8ddff;

    public static function light(color:uint):uint {
        switch (color) {
            case red:
                return lightRed;
            case yellow:
                return lightYellow;
            case grey:
                return lightGrey;
            case cyan:
                return lightCyan;
            case magenta:
                return lightMagenta;
        }
        throw new Error("unknown color " + color)
    }

    private static const all:Vector.<uint> = new <uint>[red, yellow, grey, cyan, magenta];

    // color transforms

    private static const transforms:Dictionary = createTransforms();

    private static function transformImpl(color:uint):ColorTransform {
        const r:Number = ((color >> 16) & 0xFF) / 0xFF;
        const g:Number = ((color >> 8) & 0xFF) / 0xFF;
        const b:Number = (color & 0xFF) / 0xFF;
        return new ColorTransform(r, g, b, 1)
    }

    private static function createTransforms():Dictionary {
        const result:Dictionary = new Dictionary();
        for each(var color:uint in all) {
            result[color] = transformImpl(color);
            result[light(color)] = transformImpl(light(color));
        }
        return result;
    }

    public static function transform(color:uint):ColorTransform {
        const transform:ColorTransform = transforms[color];
        if (!transform) throw new Error("no transform for color " + color);
        return transform;
    }

    public static const tutorTransform: ColorTransform = transformImpl(darkGrey);

    // bitmap dates

    public static const flaskFillBitmapData:BitmapData = new BitmapData(1, 1, false, flaskFillColor);

    public static const modalBitmapData:BitmapData = new BitmapData(1, 1, true, 0x66000000);

    public static const grassBitmapData:BitmapData = new BitmapData(1, 1, false, 0xc2ffa6);

    private static const lightBitmapDates:Dictionary = createLightBitmapDates();

    private static function createLightBitmapDates():Dictionary {
        const result:Dictionary = new Dictionary();
        for each(var color:uint in all) {
            result[color] = new BitmapData(1, 1, false, light(color));
        }
        return result;
    }

    public static function lightBitmapData(color:uint):BitmapData {
        const bitmapData:BitmapData = lightBitmapDates[color];
        if (!bitmapData) throw new Error("no bitmapData for color " + color);
        return bitmapData;
    }

    // menu objects colors

    public static function slot(slotId:SlotId):uint {
        switch (slotId) {
            case SlotId.SLOT_1:
                return red;
            case SlotId.SLOT_2:
                return grey;
            case SlotId.SLOT_3:
                return yellow;
            case SlotId.SLOT_4:
                return magenta;
            case SlotId.SLOT_5:
                return cyan;
        }
        throw new Error("unknown slotId " + slotId);
    }

    public static function skill(skillType:SkillType):uint {
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

    public static function item(itemType:ItemType):uint {
        switch (itemType) {
            case ItemType.FIREBALL:
                return red;
            case ItemType.STRENGTHENING:
                return yellow;
            case ItemType.VOLCANO:
                return grey;
            case ItemType.TORNADO:
                return cyan;
            case ItemType.ASSISTANCE:
                return magenta;
        }
        throw new Error("unknown itemType " + itemType);
    }

    public static function top(place:uint):uint {
        switch (place) {
            case 1:
                return yellow;
            case 2:
                return magenta;
            case 3:
                return red;
            case 4:
                return grey;
            case 5:
                return cyan;
        }
        throw new Error("unknown place " + place);
    }

    // game objects colors

    public static const noOwnerColor:uint = grey;

    private static const playerColors:Vector.<uint> = new <uint>[
        yellow,
        cyan,
        magenta,
        red
    ];

    public static function playerColor(playerId:PlayerIdDTO):uint {
        return playerColors[playerId.id];
    }

    public static function playerTransform(playerId:PlayerIdDTO):ColorTransform {
        return transform(playerColor(playerId));
    }

    public static function buildingTransform(owner:BuildingOwner):ColorTransform {
        return owner.hasOwner ? playerTransform(owner.ownerId) : transform(noOwnerColor);
    }

    public static function groundBitmapDataById(playerId:PlayerIdDTO):BitmapData {
        return lightBitmapData(playerColor(playerId));
    }

    public static function groundBitmapData(owner:BuildingOwner):BitmapData {
        return owner.hasOwner ? groundBitmapDataById(owner.ownerId) : lightBitmapData(noOwnerColor);
    }
}
}
