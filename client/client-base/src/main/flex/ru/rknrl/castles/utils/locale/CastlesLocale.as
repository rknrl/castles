package ru.rknrl.castles.utils.locale {
import ru.rknrl.Locale;
import ru.rknrl.castles.utils.*;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.SkillType;

public class CastlesLocale extends Locale {
    private static const SALE:String = "sale";
    private static const FAST_AND_TRUST:String = "fast_and_trust";
    private static const BUY_BUTTON:String = "buy_button";
    private static const WIN:String = "win";
    private static const DEFEAT:String = "defeat";
    private static const REWARD:String = "reward";
    private static const NO_REWARD:String = "no_reward";
    private static const TO_MENU:String = "to_menu";
    private static const PLAY_AGAIN:String = "play_again";
    private static const NO_CONNECTION:String = "no_connection";
    private static const TRY_CONNECT:String = "try_connect";
    private static const FIREBALL:String = "fireball";
    private static const STRENGTHENING:String = "strengthening";
    private static const VOLCANO:String = "volcano";
    private static const TORNADO:String = "tornado";
    private static const ASSISTANCE:String = "assistance";
    private static const SHOP_TITLE:String = "shop_title";
    private static const SKILLS_TITLE:String = "skills_title";
    private static const ATTACK:String = "attack";
    private static const DEFENCE:String = "defence";
    private static const SPEED:String = "speed";
    private static const UPGRADE:String = "upgrade";
    private static const UPGRADE_INFO:String = "upgrade_info";
    private static const REMOVE:String = "remove";
    private static const BUILD:String = "build";
    private static const HOUSE:String = "house";
    private static const TOWER:String = "tower";
    private static const CHURCH:String = "church";
    private static const HOUSE_INFO:String = "house_info";
    private static const TOWER_INFO:String = "tower_info";
    private static const CHURCH_INFO:String = "church_info";
    private static const MAIN_TITLE:String = "main_title";
    private static const PLAY:String = "play";
    private static const ENTER_GAME:String = "enter_game";
    private static const BUTTON_CASTLE:String = "button_castle";
    private static const BUTTON_SKILLS:String = "button_skills";
    private static const BUTTON_SHOP:String = "button_shop";
    private static const BUTTON_BANK:String = "button_bank";
    private static const LOADING:String = "loading";

    public function CastlesLocale(data:String) {
        super(data);
    }

    public function get sale():String {
        return translate(SALE);
    }

    public function get fastAndTrust():String {
        return translate(FAST_AND_TRUST);
    }

    public function buyButtonLabel(goldByDollar:int):String {
        return translate(BUY_BUTTON).replace("$1", goldByDollar).replace("$2", 1);
    }

    public function gameOverTitle(win:Boolean):String {
        return win ? translate(WIN) : translate(DEFEAT);
    }

    public function gameOverReward(reward:int):String {
        return reward > 0 ? translate(REWARD).replace("$1", reward) : translate(NO_REWARD);
    }

    public function get toMenu():String {
        return translate(TO_MENU);
    }

    public function get playAgain():String {
        return translate(PLAY_AGAIN);
    }

    public function get noConnection():String {
        return translate(NO_CONNECTION);
    }

    public function get tryConnect():String {
        return translate(TRY_CONNECT);
    }

    public function getItemName(itemType:ItemType):String {
        switch (itemType) {
            case ItemType.FIREBALL:
                return translate(FIREBALL);
            case ItemType.STRENGTHENING:
                return translate(STRENGTHENING);
            case ItemType.VOLCANO:
                return translate(VOLCANO);
            case ItemType.TORNADO:
                return translate(TORNADO);
            case ItemType.ASSISTANCE:
                return translate(ASSISTANCE);
        }
        throw new Error("Unknown item type " + itemType);
    }

    public function get shopTitle():String {
        return translate(SHOP_TITLE) + " ";
    }

    public function get skillsTitle():String {
        return translate(SKILLS_TITLE) + " ";
    }

    public function getSkillName(skillType:SkillType):String {
        switch (skillType) {
            case SkillType.ATTACK:
                return translate(ATTACK);
            case SkillType.DEFENCE:
                return translate(DEFENCE);
            case SkillType.SPEED:
                return translate(SPEED);
        }
        throw new Error("unknown skill type " + skillType);
    }

    public function get upgrade():String {
        return translate(UPGRADE);
    }

    public function get upgradeInfo():String {
        return translate(UPGRADE_INFO);
    }

    public function get remove():String {
        return translate(REMOVE);
    }

    public function get build():String {
        return translate(BUILD);
    }

    public function getBuildingName(buildingType:BuildingType):String {
        switch (buildingType) {
            case BuildingType.HOUSE:
                return translate(HOUSE);
            case BuildingType.TOWER:
                return translate(TOWER);
            case BuildingType.CHURCH:
                return translate(CHURCH);
        }
        throw new Error("unknown building type " + buildingType);
    }

    public function getBuildingInfo(buildingType:BuildingType):String {
        switch (buildingType) {
            case BuildingType.HOUSE:
                return translate(HOUSE_INFO);
            case BuildingType.TOWER:
                return translate(TOWER_INFO);
            case BuildingType.CHURCH:
                return translate(CHURCH_INFO);
        }
        throw new Error("unknown building type " + buildingType);
    }

    public function get mainTitle():String {
        return translate(MAIN_TITLE);
    }

    public function get play():String {
        return translate(PLAY);
    }

    public function get enterGame():String {
        return translate(ENTER_GAME);
    }

    public function screenName(screen:String):String {
        switch (screen) {
            case Utils.BUTTON_PLAY:
                return play;
            case Utils.SCREEN_CASTLE:
                return translate(BUTTON_CASTLE);
            case Utils.SCREEN_SKILLS:
                return translate(BUTTON_SKILLS);
            case Utils.SCREEN_SHOP:
                return translate(BUTTON_SHOP);
            case Utils.SCREEN_BANK:
                return translate(BUTTON_BANK);
        }
        throw new Error("unknown screen " + screen);
    }

    public function get loading():String {
        return translate(LOADING);
    }
}
}
