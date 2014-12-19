package ru.rknrl.castles.utils {

import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.display.Shape;
import flash.display.Sprite;
import flash.geom.Point;
import flash.utils.Dictionary;

import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SlotId;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;

public class Utils {
    public static const popupDuration:int = 100;
    public static const popupEaser:IEaser = new Linear(0, 1);

    public static const strengtheningScale:Number = 1.5;

    public static const transparent:BitmapData = new BitmapData(1, 1, true, 0);

    public static const popupScreen:BitmapData = new BitmapData(1, 1, true, 0xcccccccc);

    public static const flaskFill:BitmapData = new BitmapData(1, 1, false, 0xcccccc);

    public static const PLAY:String = "Play";
    public static const NOT_ENOUGH_GOLD:String = "notEnoughGold";

    public static const slotsPositions:Dictionary = new Dictionary();
    slotsPositions[SlotId.SLOT_1] = new Point(-2, 0);
    slotsPositions[SlotId.SLOT_2] = new Point(-1, -1);
    slotsPositions[SlotId.SLOT_3] = new Point(0, 0);
    slotsPositions[SlotId.SLOT_4] = new Point(1, -1);
    slotsPositions[SlotId.SLOT_5] = new Point(2, 0);

    public static function getItemIcon(itemType:ItemType):Sprite {
        switch (itemType) {
            case ItemType.FIREBALL:
                return new Fireball();
            case ItemType.STRENGTHENING:
                return new Strengthening();
            case ItemType.VOLCANO:
                return new Volcano();
            case ItemType.TORNADO:
                return new Tornado();
            case ItemType.ASSISTANCE:
                return new Assistance();
        }
        throw new Error("Unknown item type " + itemType);
    }

    public static function getUnitBody(buildingType:BuildingType):DisplayObject {
        const shape:Shape = new Shape();
        shape.graphics.beginFill(0xffffff);
        shape.graphics.moveTo(-1.25, 0);
        shape.graphics.lineTo(0, -5);
        shape.graphics.lineTo(1.25, 0);
        shape.graphics.endFill();
        return shape;
    }

    public static function getBuildingBody(buildingType:BuildingType):Sprite {
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

    public static function getScaleByLevel(level:BuildingLevel):Number {
        switch (level) {
            case BuildingLevel.LEVEL_1:
                return 0.66;
            case BuildingLevel.LEVEL_2:
                return 0.88;
            case BuildingLevel.LEVEL_3:
                return 1.0;
        }
        throw new Error("unknown building level " + level)
    }

    public static function nextSkillLevel(level:SkillLevel):SkillLevel {
        switch (level) {
            case SkillLevel.SKILL_LEVEL_0:
                return SkillLevel.SKILL_LEVEL_1;
            case SkillLevel.SKILL_LEVEL_1:
                return SkillLevel.SKILL_LEVEL_2;
            case SkillLevel.SKILL_LEVEL_2:
                return SkillLevel.SKILL_LEVEL_3;
        }
        throw new Error("hasn't next level " + level);
    }

    public static function nextBuildingLevel(level:BuildingLevel):BuildingLevel {
        switch (level) {
            case BuildingLevel.LEVEL_1:
                return BuildingLevel.LEVEL_2;
            case BuildingLevel.LEVEL_2:
                return BuildingLevel.LEVEL_3;
        }
        throw new Error("hasn't next level " + level);
    }
}
}
