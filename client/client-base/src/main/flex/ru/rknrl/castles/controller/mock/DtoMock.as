package ru.rknrl.castles.controller.mock {
import ru.rknrl.dto.AccountConfigDTO;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticationSuccessDTO;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingPriceDTO;
import ru.rknrl.dto.BuildingPrototypeDTO;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.ItemDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.ItemsDTO;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.PlayerInfoDTO;
import ru.rknrl.dto.PointDTO;
import ru.rknrl.dto.ProductDTO;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SkillUpgradePriceDTO;
import ru.rknrl.dto.SkillsDTO;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.StartLocationDTO;
import ru.rknrl.dto.StartLocationOrientation;
import ru.rknrl.dto.StartLocationPosDTO;
import ru.rknrl.dto.TopUserInfoDTO;
import ru.rknrl.dto.UserInfoDTO;

public class DtoMock {
    public static function product():ProductDTO {
        const dto:ProductDTO = new ProductDTO();
        dto.id = 1;
        dto.title = "title";
        dto.description = "description";
        dto.photoUrl = "photoUrl";
        dto.count = 100;
        dto.price = 1;
        dto.currency = "$";
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

    public static function userInfo(firstName:String, lastName:String, photoUrl:String):UserInfoDTO {
        const dto:UserInfoDTO = new UserInfoDTO();
        dto.accountId = new AccountIdDTO();
        dto.accountId.id = "1";
        dto.accountId.type = AccountType.DEV;
        dto.firstName = firstName;
        dto.lastName = lastName;
        dto.photo256 = photoUrl;
        return dto;
    }

    public static function topUserInfo(place:int, name:String, photoUrl:String):TopUserInfoDTO {
        const dto:TopUserInfoDTO = new TopUserInfoDTO();
        dto.place = place;
        dto.info = userInfo(name, null, photoUrl);
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
        dto.info = userInfo(name, null, photoUrl);
        return dto;
    }


    public static const playerInfo1:PlayerInfoDTO = playerInfo(0, "Толя Янот", "1");
    public static const playerInfo2:PlayerInfoDTO = playerInfo(1, "Sasha Serova", "2");
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

    public static function playerIdDto(id:int):PlayerIdDTO {
        const dto:PlayerIdDTO = new PlayerIdDTO();
        dto.id = id;
        return dto;
    }

    public static function startLocationPos(playerId:int, orientation:StartLocationOrientation, i:int, j:int):StartLocationPosDTO {
        const dto:StartLocationPosDTO = new StartLocationPosDTO();
        dto.playerId = playerIdDto(playerId);
        dto.orientation = orientation;
        dto.pos = new PointDTO();
        dto.pos.x = i * CellSize.SIZE.id();
        dto.pos.y = j * CellSize.SIZE.id();
        return dto;
    }

    public static function startLocationsPosLandscape():Vector.<StartLocationPosDTO> {
        return new <StartLocationPosDTO>[
            startLocationPos(3, StartLocationOrientation.BOTTOM_RIGHT, 12, 14)
        ];
    }

    public static function startLocationsPosPortrait():Vector.<StartLocationPosDTO> {
        return new <StartLocationPosDTO>[
            startLocationPos(1, StartLocationOrientation.BOTTOM_LEFT, 2, 10)
        ];
    }

    private static var buildingId:int = 0;

    public static function building(buildingType:BuildingType, level:BuildingLevel, ownerId:int, i:int, j:int):BuildingDTO {
        const dto:BuildingDTO = new BuildingDTO();
        dto.id = new BuildingIdDTO();
        dto.id.id = buildingId++;
        dto.building = new BuildingPrototypeDTO();
        dto.building.type = buildingType;
        dto.building.level = level;
        if (ownerId > -1) dto.owner = playerIdDto(ownerId);
        dto.population = 47;
        dto.strengthened = false;
        dto.pos = new PointDTO();
        dto.pos.x = i * CellSize.SIZE.id() + CellSize.SIZE.id() / 2;
        dto.pos.y = j * CellSize.SIZE.id() + CellSize.SIZE.id() / 2;
        return dto;
    }

    public static function buildingsLandscape():Vector.<BuildingDTO> {
        return new <BuildingDTO>[
            // player 0
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, 0, 0, 0),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, 0, 2, 0),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, 0, 4, 0),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, 0, 1, 1),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_2, 0, 3, 1),

            // player 1
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, 1, 10, 0),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, 1, 12, 0),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_2, 1, 14, 0),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, 1, 11, 1),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, 1, 13, 1),

            // player 2
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, 2, 0, 14),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, 2, 2, 14),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, 2, 4, 14),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, 2, 1, 13),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, 2, 3, 13),

            // player 3
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, 3, 11, 13),

            // no owner
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, -1, 6, 6),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, -1, 6, 8),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, -1, 8, 6),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, -1, 8, 8),

            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, -1, 4, 6),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, -1, 4, 8),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, -1, 10, 6),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, -1, 10, 8),

            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, -1, 2, 6),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, -1, 2, 8),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, -1, 12, 6),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, -1, 12, 8),

            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, -1, 0, 7),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, -1, 14, 7),

            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, -1, 6, 4),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, -1, 8, 4),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, -1, 6, 10),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, -1, 8, 10),

            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, -1, 6, 2),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, -1, 8, 2),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, -1, 6, 12),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, -1, 8, 12),

            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, -1, 7, 0),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, -1, 7, 14),

            building(BuildingType.HOUSE, BuildingLevel.LEVEL_3, -1, 4, 4),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_3, -1, 10, 4),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_3, -1, 4, 10),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_3, -1, 10, 10),

            building(BuildingType.CHURCH, BuildingLevel.LEVEL_2, -1, 1, 3),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_2, -1, 13, 3),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_2, -1, 1, 11),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_2, -1, 13, 11)
        ];
    }

    public static function buildingsPortrait():Vector.<BuildingDTO> {
        return new <BuildingDTO>[
            // player 0
            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, 0, 3, 0),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, 0, 5, 0),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, 0, 7, 0),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, 0, 4, 1),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, 0, 6, 1),

            // player 1
            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, 1, 0, 10),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_1, 1, 2, 10),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, 1, 4, 10),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, 1, 1, 9),

            // no owner
            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, -1, 0, 5),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, -1, 2, 5),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_3, -1, 5, 5),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_2, -1, 7, 5),

            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, -1, 0, 7),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_2, -1, 2, 7),
            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, -1, 4, 7),

            building(BuildingType.HOUSE, BuildingLevel.LEVEL_3, -1, 6, 8),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, -1, 7, 10),

            building(BuildingType.TOWER, BuildingLevel.LEVEL_1, -1, 3, 3),
            building(BuildingType.HOUSE, BuildingLevel.LEVEL_2, -1, 5, 3),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_1, -1, 7, 3),

            building(BuildingType.HOUSE, BuildingLevel.LEVEL_3, -1, 1, 2),
            building(BuildingType.CHURCH, BuildingLevel.LEVEL_3, -1, 0, 0)

        ];
    }
}
}
