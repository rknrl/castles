//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
import mx.collections.ArrayCollection;

import protos.AccountType;
import protos.BuildingLevel;
import protos.BuildingType;
import protos.SkillLevel;

public class Utils {
    public static const NO_BUILDING:BuildingType = new BuildingType(666, "NONE");

    public static const accountTypes:ArrayCollection = vectorToCollection(Vector.<Object>(AccountType.values));
    public static const buildingTypes:ArrayCollection = vectorToCollection(Vector.<Object>(BuildingType.values).concat(new <Object>[NO_BUILDING]));
    public static const buildingLevels:ArrayCollection = vectorToCollection(Vector.<Object>(BuildingLevel.values));
    public static const skillLevels:ArrayCollection = vectorToCollection(Vector.<Object>(SkillLevel.values));

    public static function enumLabelFunction(enum:*):String {
        return enum.name();
    }

    public static function vectorToCollection(vector:Vector.<Object>):ArrayCollection {
        const result:ArrayCollection = new ArrayCollection();
        for each(var item:* in vector) result.addItem(item);
        return result;
    }
}
}
