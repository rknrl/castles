package ru.rknrl.castles.view.locale {
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.SkillType;

public class CastlesLocale {
    public function CastlesLocale(data:String) {
    }

    public function get play():String {
        return "Играть";
    }

    public function bankButton(count:int, price:int, currency:String):String {
        return "Купить " + count + "★ за " + price + " " + currency;
    }

    public function balance(value:int):String {
        return "У вас " + value + "★";
    }

    public function shopTitle(price:int):String {
        return "Любой предмет за " + price + "★";
    }

    public function get upgradesComplete():String {
        return "Собраны все улучшения";
    }

    public function upgradesTitle(price:int):String {
        return "Любое улучшение за " + price + "★";
    }

    public function get topTitle():String {
        return "Лучшие за прошлую неделю";
    }

    public function skillName(skillType:SkillType):String {
        switch (skillType) {
            case SkillType.ATTACK:
                return "Атака";
            case SkillType.DEFENCE:
                return "Защита";
            case SkillType.SPEED:
                return "Скорость";
        }
        throw new Error("unknown skillType " + skillType);
    }

    public function buildingName(buildingType:BuildingType):String {
        switch (buildingType) {
            case BuildingType.HOUSE:
                return "Домик";
            case BuildingType.CHURCH:
                return "Церковь";
            case BuildingType.TOWER:
                return "Башня";
        }
        throw new Error("unknown buildingType " + buildingType);
    }

    public function get build():String {
        return "Построить";
    }

    public function get cancel():String {
        return "Отмена";
    }

    public function get upgrade():String {
        return "Улучшить";
    }

    public function get remove():String {
        return "Удалить";
    }

    public function win(reward:int):String {
        return "Победа! Награда " + reward + "★";
    }

    public function lose(reward:int):String {
        return "Поражение! Награда " + reward + "★";
    }

    public function get defaultName():String {
        return "Гость";
    }

    public function get loading():String {
        return "Загрузка";
    }

    public function get searchOpponents():String {
        return "Ищем противников";
    }

    public function get noConnection():String {
        return "Нет интернет соединения";
    }

    public function get fastAndTrust():String {
        return "Быстро\nи\nнадежно";
    }
}
}
