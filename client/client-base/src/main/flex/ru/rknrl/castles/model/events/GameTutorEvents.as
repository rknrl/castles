//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model.events {
import protos.ItemType;

public class GameTutorEvents {
    public static const ARROW_SENDED:String = "arrowSended";
    public static const ARROWS_SENDED:String = "arrowsSended";
    public static const BUILDING_CAPTURED:String = "buildingCaptured";

    public static function selected(itemType:ItemType):String {
        return itemType.name + "selected";
    }

    public static function casted(itemType:ItemType):String {
        return itemType.name + "casted";
    }
}
}
