package ru.rknrl.castles.controller.mock {
import ru.rknrl.dto.AccountConfigDTO;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.AuthenticationSuccessDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingPriceDTO;
import ru.rknrl.dto.BuildingPrototypeDTO;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.ItemDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.ItemsDTO;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.PlayerInfoDTO;
import ru.rknrl.dto.ProductDTO;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SkillUpgradePriceDTO;
import ru.rknrl.dto.SkillsDTO;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.StartLocationDTO;
import ru.rknrl.dto.TopUserInfoDTO;

public class DtoMock {
    public static function product():ProductDTO {
        const dto:ProductDTO = new ProductDTO();
        dto.id = 1;
        dto.title = "title";
        dto.description = "description";
        dto.photoUrl = "photoUrl";
        dto.count = 100;
        dto.price = 1;
        return dto;
    }

    public static function buildingPrototype(buildingType:BuildingType, level:BuildingLevel):BuildingPrototypeDTO {
        const dto:BuildingPrototypeDTO = new BuildingPrototypeDTO();
        dto.type = buildingType;
        dto.level = level;
        return dto;
    }

    public static function slot(slotId:SlotId, buildingPrototype:BuildingPrototypeDTO):SlotDTO {
        const dto:SlotDTO = new SlotDTO();
        dto.id = slotId;
        dto.buildingPrototype = buildingPrototype;
        return dto;
    }

    public static function startLocation():StartLocationDTO {
        const dto:StartLocationDTO = new StartLocationDTO();
        dto.slots.push(slot(SlotId.SLOT_1, buildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)));
        dto.slots.push(slot(SlotId.SLOT_2, buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)));
        dto.slots.push(slot(SlotId.SLOT_3, buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1)));
        dto.slots.push(slot(SlotId.SLOT_4, buildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)));
        dto.slots.push(slot(SlotId.SLOT_5, buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)));
        return dto;
    }

    public static function skillLevel(skillType:SkillType, level:SkillLevel):SkillLevelDTO {
        const dto:SkillLevelDTO = new SkillLevelDTO();
        dto.type = skillType;
        dto.level = level;
        return dto;
    }

    public static function skills():SkillsDTO {
        const dto:SkillsDTO = new SkillsDTO();
        dto.levels.push(skillLevel(SkillType.ATTACK, SkillLevel.SKILL_LEVEL_0));
        dto.levels.push(skillLevel(SkillType.DEFENCE, SkillLevel.SKILL_LEVEL_3));
        dto.levels.push(skillLevel(SkillType.SPEED, SkillLevel.SKILL_LEVEL_1));
        return dto;
    }

    public static function item(itemType:ItemType, count:int):ItemDTO {
        const dto:ItemDTO = new ItemDTO();
        dto.type = itemType;
        dto.count = count;
        return dto;
    }

    public static function items():ItemsDTO {
        const dto:ItemsDTO = new ItemsDTO();
        for each(var itemType:ItemType in ItemType.values) {
            dto.items.push(item(itemType, 2))
        }
        return dto;
    }

    public static function accountState():AccountStateDTO {
        const dto:AccountStateDTO = new AccountStateDTO();
        dto.startLocation = startLocation();
        dto.skills = skills();
        dto.items = items();
        dto.gold = 100;
        return dto;
    }

    public static function buildingPrice(level:BuildingLevel, price:int):BuildingPriceDTO {
        const dto:BuildingPriceDTO = new BuildingPriceDTO();
        dto.level = level;
        dto.price = price;
        return dto;
    }

    public static function buildingPrices():Vector.<BuildingPriceDTO> {
        const dto:Vector.<BuildingPriceDTO> = new <BuildingPriceDTO>[];
        dto.push(buildingPrice(BuildingLevel.LEVEL_1, 4));
        dto.push(buildingPrice(BuildingLevel.LEVEL_2, 16));
        dto.push(buildingPrice(BuildingLevel.LEVEL_3, 64));
        return dto;
    }

    public static function skillUpgradePrice(totalLevel:int, price:int):SkillUpgradePriceDTO {
        const dto:SkillUpgradePriceDTO = new SkillUpgradePriceDTO();
        dto.totalLevel = totalLevel;
        dto.price = price;
        return dto;
    }

    public static function skillUpgradePrices():Vector.<SkillUpgradePriceDTO> {
        const dto:Vector.<SkillUpgradePriceDTO> = new <SkillUpgradePriceDTO>[];
        for (var i:int = 0; i < 9; i++) {
            dto.push(skillUpgradePrice(i, Math.pow(2, i)));
        }
        return dto;
    }

    public static function topUserInfo(place:int, name:String, photoUrl:String):TopUserInfoDTO {
        const dto:TopUserInfoDTO = new TopUserInfoDTO();
        dto.place = place;
        dto.name = name;
        dto.photoUrl = photoUrl;
        return dto;
    }

    public static function top():Vector.<TopUserInfoDTO> {
        const dto:Vector.<TopUserInfoDTO> = new <TopUserInfoDTO>[];
        dto.push(topUserInfo(1, "1", "1"));
        dto.push(topUserInfo(2, "2", "2"));
        dto.push(topUserInfo(3, "3", "3"));
        dto.push(topUserInfo(4, "4", "4"));
        dto.push(topUserInfo(5, "5", "5"));
        return dto;
    }

    public static function config():AccountConfigDTO {
        const dto:AccountConfigDTO = new AccountConfigDTO();
        dto.buildings = buildingPrices();
        dto.skillUpgradePrices = skillUpgradePrices();
        dto.itemPrice = 1;
        return dto;
    }

    public static function authenticationSuccess():AuthenticationSuccessDTO {
        const dto:AuthenticationSuccessDTO = new AuthenticationSuccessDTO();
        dto.accountState = accountState();
        dto.config = config();
        dto.top = top();
        dto.products = new <ProductDTO>[product()];
        dto.enterGame = false;
        return dto;
    }

    // utils

    public static function findSkillLevel(dto:SkillsDTO, skillType:SkillType):SkillLevelDTO {
        for each(var skillLevel:SkillLevelDTO in dto.levels) {
            if (skillLevel.type == skillType) return skillLevel;
        }
        throw new Error("can't find skill level " + skillType)
    }

    public static function findItem(dto:ItemsDTO, itemType:ItemType):ItemDTO {
        for each(var item:ItemDTO in dto.items) {
            if (item.type == itemType) return item;
        }
        throw new Error("can't find item " + itemType)
    }

    public static function findSlot(dto:StartLocationDTO, slotId:SlotId):SlotDTO {
        for each(var slot:SlotDTO in dto.slots) {
            if (slot.id == slotId) return slot;
        }
        throw new Error("can't find slot " + slotId)
    }

    private static function playerInfo(id:int, name:String, photoUrl:String):PlayerInfoDTO {
        const dto:PlayerInfoDTO = new PlayerInfoDTO();
        dto.id = new PlayerIdDTO();
        dto.id.id = id;
        dto.name = name;
        dto.photoUrl = photoUrl;
        return dto;
    }


    private static const playerInfo1:PlayerInfoDTO = playerInfo(0, "Толя Янот", "1");
    private static const playerInfo2:PlayerInfoDTO = playerInfo(1, "Sasha Serova", "2");
    private static const playerInfo3:PlayerInfoDTO = playerInfo(2, "Napoleon1769", "3");
    private static const playerInfo4:PlayerInfoDTO = playerInfo(3, "Виктория Викторовна", "4");


    public static function playerInfosPortrait():Vector.<PlayerInfoDTO> {
        return new <PlayerInfoDTO>[
            playerInfo1,
            playerInfo2
        ];
    }

    public static function playerInfosLandscape():Vector.<PlayerInfoDTO> {
        return new <PlayerInfoDTO>[
            playerInfo1,
            playerInfo2,
            playerInfo3,
            playerInfo4
        ];
    }

    public static function winner():PlayerInfoDTO {
        return playerInfo1;
    }

    public static function losersPortrait():Vector.<PlayerInfoDTO> {
        return new <PlayerInfoDTO>[
            playerInfo2
        ];
    }

    public static function losersLandscape():Vector.<PlayerInfoDTO> {
        return new <PlayerInfoDTO>[
            playerInfo2,
            playerInfo3,
            playerInfo4
        ];
    }
}
}
