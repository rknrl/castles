package ru.rknrl.castles.view {
import flash.display.DisplayObject;

import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.ItemType;

public class Fla {
    public static function createItem(itemType:ItemType):DisplayObject {
        switch (itemType) {
            case ItemType.FIREBALL:
                return new FireballIconMC();
            case ItemType.STRENGTHENING:
                return new StrengtheningIconMC();
            case ItemType.VOLCANO:
                return new VolcanoIconMC();
            case ItemType.TORNADO:
                return new TornadoIconMC();
            case ItemType.ASSISTANCE:
                return new AssistanceIconMC();
        }
        throw new Error("unknown item type " + itemType);
    }

    public static function createBuilding(buildingType:BuildingType, buildingLevel:BuildingLevel):DisplayObject {
        const building:DisplayObject = getBuilding(buildingType);
        building.scaleX = building.scaleY = buildingLevelToScale(buildingLevel);
        return building;
    }

    private static function getBuilding(buildingType:BuildingType):DisplayObject {
        switch (buildingType) {
            case BuildingType.HOUSE:
                return new House();
            case BuildingType.TOWER:
                return new Tower();
            case BuildingType.CHURCH:
                return new Church();
        }
        throw new Error("unknown building type " + buildingType);
    }

    public static const strengtheningScale:Number = 1.2;

    public static function buildingLevelToScale(buildingLevel:BuildingLevel):Number {
        switch (buildingLevel) {
            case BuildingLevel.LEVEL_1:
                return 0.6;
            case BuildingLevel.LEVEL_2:
                return 0.8;
            case BuildingLevel.LEVEL_3:
                return 1;
        }
        throw new Error("unknown building level " + buildingLevel);
    }

    public static function unit(buildingType:BuildingType):DisplayObject {
        return new UnitMC();
    }
}
}
