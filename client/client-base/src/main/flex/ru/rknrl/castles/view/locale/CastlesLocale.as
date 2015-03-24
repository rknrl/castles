//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.locale {
import ru.rknrl.Locale;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.SkillType;

public class CastlesLocale extends Locale {
    private static const PLAY:String = "play";
    private static const BANK_BUTTON:String = "bank_button";
    private static const BALANCE:String = "balance";
    private static const SHOP_TITLE:String = "shop_title";
    private static const UPGRADES_COMPLETE:String = "upgrades_complete";
    private static const UPGRADES_TITLE:String = "upgrades_title";
    private static const TOP_TITLE:String = "top_title";
    private static const ATTACK:String = "attack";
    private static const DEFENCE:String = "defence";
    private static const SPEED:String = "speed";
    private static const HOUSE:String = "house";
    private static const TOWER:String = "tower";
    private static const CHURCH:String = "church";
    private static const BUILD:String = "build";
    private static const CANCEL:String = "cancel";
    private static const UPGRADE:String = "upgrade";
    private static const REMOVE:String = "remove";
    private static const WIN:String = "win";
    private static const LOST:String = "lost";
    private static const DEFAULT_NAME:String = "default_name";
    private static const LOADING:String = "loading";
    private static const SEARCH_OPPONENTS:String = "search_opponents";
    private static const NO_CONNECTION:String = "no_connection";
    private static const FAST_AND_TRUST:String = "fast_and_trust";
    private static const DEAD:String = "dead";
    private static const TUTOR_SELF_BUILDINGS:String = "tutor_self_buildings";
    private static const TUTOR_ENEMY_BUILDINGS_BIG:String = "tutor_enemy_buildings_big";
    private static const TUTOR_ENEMY_BUILDINGS_SMALL:String = "tutor_enemy_buildings_small";
    private static const NEXT:String = "next";
    private static const TUTOR_ARROW:String = "tutor_arrow";
    private static const TUTOR_ARROWS:String = "tutor_arrows";
    private static const TUTOR_BIG_TOWER:String = "tutor_big_tower";
    private static const TUTOR_CLICK_PREFIX:String = "tutor_click_";
    private static const TUTOR_CAST_PREFIX:String = "tutor_cast_";
    private static const TUTOR_WIN:String = "tutor_win";
    private static const GAME_SPLASH:String = "game_splash";
    private static const ENTER_FIRST_GAME:String = "enter_first_game";

    public function CastlesLocale(data:String) {
        super(data);
    }

    public function get play():String {
        return translate(PLAY);
    }

    public function bankButton(count:int, price:int, currency:String):String {
        return translate(BANK_BUTTON, count, price, currency);
    }

    public function balance(value:int):String {
        return translate(BALANCE, value);
    }

    public function shopTitle(price:int):String {
        return translate(SHOP_TITLE, price);
    }

    public function get upgradesComplete():String {
        return translate(UPGRADES_COMPLETE);
    }

    public function upgradesTitle(price:int):String {
        return translate(UPGRADES_TITLE, price);
    }

    public function get topTitle():String {
        return translate(TOP_TITLE);
    }

    public function skillName(skillType:SkillType):String {
        switch (skillType) {
            case SkillType.ATTACK:
                return translate(ATTACK);
            case SkillType.DEFENCE:
                return translate(DEFENCE);
            case SkillType.SPEED:
                return translate(SPEED);
        }
        throw new Error("unknown skillType " + skillType);
    }

    public function buildingName(buildingType:BuildingType):String {
        switch (buildingType) {
            case BuildingType.HOUSE:
                return translate(HOUSE);
            case BuildingType.CHURCH:
                return translate(CHURCH);
            case BuildingType.TOWER:
                return translate(TOWER);
        }
        throw new Error("unknown buildingType " + buildingType);
    }

    public function get build():String {
        return translate(BUILD);
    }

    public function get cancel():String {
        return translate(CANCEL);
    }

    public function get upgrade():String {
        return translate(UPGRADE);
    }

    public function get remove():String {
        return translate(REMOVE);
    }

    public function win(reward:int):String {
        return translate(WIN, reward);
    }

    public function lose(reward:int):String {
        return translate(LOST, reward);
    }

    public function get defaultName():String {
        return translate(DEFAULT_NAME);
    }

    public function get loading():String {
        return translate(LOADING);
    }

    public function get searchOpponents():String {
        return translate(SEARCH_OPPONENTS);
    }

    public function get noConnection():String {
        return translate(NO_CONNECTION);
    }

    public function get fastAndTrust():String {
        return translate(FAST_AND_TRUST);
    }

    public function get dead():String {
        return translate(DEAD);
    }

    public function get tutorSelfBuildings():String {
        return translate(TUTOR_SELF_BUILDINGS);
    }

    public function tutorEnemyBuildings(isBigGame:Boolean):String {
        return isBigGame ? translate(TUTOR_ENEMY_BUILDINGS_BIG) : translate(TUTOR_ENEMY_BUILDINGS_SMALL);
    }

    public function get next():String {
        return translate(NEXT)
    }

    public function get tutorArrow():String {
        return translate(TUTOR_ARROW);
    }

    public function get tutorArrows():String {
        return translate(TUTOR_ARROWS);
    }

    public function get tutorBigTower():String {
        return translate(TUTOR_BIG_TOWER);
    }

    public function tutorItemClick(itemType:ItemType):String {
        return translate(TUTOR_CLICK_PREFIX + itemType.name().toLowerCase());
    }

    public function tutorItemCast(itemType:ItemType):String {
        return translate(TUTOR_CAST_PREFIX + itemType.name().toLowerCase());
    }

    public function get tutorWin():String {
        return translate(TUTOR_WIN);
    }

    public function get gameSplash():String {
        return translate(GAME_SPLASH);
    }

    public function get enterFirstGame():String {
        return translate(ENTER_FIRST_GAME);
    }
}
}
