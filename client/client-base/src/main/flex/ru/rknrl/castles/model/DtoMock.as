//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model {
import ru.rknrl.dto.AccountConfigDTO;
import ru.rknrl.dto.AccountIdDTO;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.AccountType;
import ru.rknrl.dto.AuthenticatedDTO;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingIdDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingPriceDTO;
import ru.rknrl.dto.BuildingPrototypeDTO;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.ItemDTO;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.PlayerDTO;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.PointDTO;
import ru.rknrl.dto.ProductDTO;
import ru.rknrl.dto.SkillLevel;
import ru.rknrl.dto.SkillLevelDTO;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.SkillUpgradePriceDTO;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.SlotsOrientation;
import ru.rknrl.dto.SlotsPosDTO;
import ru.rknrl.dto.TopDTO;
import ru.rknrl.dto.TopUserInfoDTO;
import ru.rknrl.dto.UserInfoDTO;

public class DtoMock {
    public static const mockAvatars:Vector.<String> = new <String>[
        "mock_avatars/1.png",
        "mock_avatars/2.png",
        "mock_avatars/3.png",
        "mock_avatars/4.png",
        "mock_avatars/5.png"
    ];

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

    public static function slots():Vector.<SlotDTO> {
        return new <SlotDTO>[
            slot(SlotId.SLOT_1, buildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_2, buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_3, buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_4, buildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_5, buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1))
        ];
    }

    public static function skillLevel(skillType:SkillType, level:SkillLevel):SkillLevelDTO {
        const dto:SkillLevelDTO = new SkillLevelDTO();
        dto.type = skillType;
        dto.level = level;
        return dto;
    }

    public static function skills():Vector.<SkillLevelDTO> {
        return new <SkillLevelDTO>[
            skillLevel(SkillType.ATTACK, SkillLevel.SKILL_LEVEL_0),
            skillLevel(SkillType.DEFENCE, SkillLevel.SKILL_LEVEL_3),
            skillLevel(SkillType.SPEED, SkillLevel.SKILL_LEVEL_1)
        ];
    }

    public static function item(itemType:ItemType, count:int):ItemDTO {
        const dto:ItemDTO = new ItemDTO();
        dto.type = itemType;
        dto.count = count;
        return dto;
    }

    public static function items():Vector.<ItemDTO> {
        const dto:Vector.<ItemDTO> = new <ItemDTO>[];
        for each(var itemType:ItemType in ItemType.values) {
            dto.push(item(itemType, 2))
        }
        return dto;
    }

    public static function accountState():AccountStateDTO {
        const dto:AccountStateDTO = new AccountStateDTO();
        dto.slots = slots();
        dto.skills = skills();
        dto.items = items();
        dto.gold = 100;
        dto.rating = 1400;
        dto.gamesCount = 0;
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

    public static function accountId(type:AccountType, id:String):AccountIdDTO {
        const dto:AccountIdDTO = new AccountIdDTO();
        dto.id = id;
        dto.type = type;
        return dto;
    }

    public static function userInfo(firstName:String, lastName:String, photoUrl:String):UserInfoDTO {
        const dto:UserInfoDTO = new UserInfoDTO();
        dto.accountId = accountId(AccountType.DEV, "1");
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

    public static function topUsers():Vector.<TopUserInfoDTO> {
        const dto:Vector.<TopUserInfoDTO> = new <TopUserInfoDTO>[];
        dto.push(topUserInfo(1, "1", "mock_avatars/1.png"));
        dto.push(topUserInfo(2, "2", "mock_avatars/2.png"));
        dto.push(topUserInfo(3, "3", "mock_avatars/3.png"));
        dto.push(topUserInfo(4, "4", "mock_avatars/4.png"));
        dto.push(topUserInfo(5, "5", "mock_avatars/5.png"));
        return dto;
    }

    public static function top():TopDTO {
        const dto: TopDTO = new TopDTO();
        dto.users = topUsers();
        return dto;
    }

    public static function config():AccountConfigDTO {
        const dto:AccountConfigDTO = new AccountConfigDTO();
        dto.buildings = buildingPrices();
        dto.skillUpgradePrices = skillUpgradePrices();
        dto.itemPrice = 1;
        return dto;
    }

    public static function authenticated():AuthenticatedDTO {
        const dto:AuthenticatedDTO = new AuthenticatedDTO();
        dto.accountState = accountState();
        dto.config = config();
        dto.top = top();
        dto.products = new <ProductDTO>[product()];
        dto.searchOpponents = false;
        return dto;
    }

    // utils

    public static function findSkillLevel(dto:Vector.<SkillLevelDTO>, skillType:SkillType):SkillLevelDTO {
        for each(var skillLevel:SkillLevelDTO in dto) {
            if (skillLevel.type == skillType) return skillLevel;
        }
        throw new Error("can't find skill level " + skillType)
    }

    public static function findItem(dto:Vector.<ItemDTO>, itemType:ItemType):ItemDTO {
        for each(var item:ItemDTO in dto) {
            if (item.type == itemType) return item;
        }
        throw new Error("can't find item " + itemType)
    }

    public static function findSlot(dto:Vector.<SlotDTO>, slotId:SlotId):SlotDTO {
        for each(var slot:SlotDTO in dto) {
            if (slot.id == slotId) return slot;
        }
        throw new Error("can't find slot " + slotId)
    }

    private static function player(id:int, name:String, photoUrl:String):PlayerDTO {
        const dto:PlayerDTO = new PlayerDTO();
        dto.id = new PlayerIdDTO();
        dto.id.id = id;
        dto.info = userInfo(name, null, photoUrl);
        return dto;
    }

    public static const playerInfo1:PlayerDTO = player(0, "Толя Янот", "mock_avatars/1.png");
    public static const playerInfo2:PlayerDTO = player(1, "Sasha Serova", "mock_avatars/2.png");
    private static const playerInfo3:PlayerDTO = player(2, "Napoleon1769", "mock_avatars/3.png");
    private static const playerInfo4:PlayerDTO = player(3, "Виктория Викторовна", "mock_avatars/4.png");

    public static function playerInfosPortrait():Vector.<PlayerDTO> {
        return new <PlayerDTO>[
            playerInfo1,
            playerInfo2
        ];
    }

    public static function playerInfosLandscape():Vector.<PlayerDTO> {
        return new <PlayerDTO>[
            playerInfo1,
            playerInfo2,
            playerInfo3,
            playerInfo4
        ];
    }

    public static function winner():PlayerDTO {
        return playerInfo1;
    }

    public static function losersPortrait():Vector.<PlayerDTO> {
        return new <PlayerDTO>[
            playerInfo2
        ];
    }

    public static function losersLandscape():Vector.<PlayerDTO> {
        return new <PlayerDTO>[
            playerInfo2,
            playerInfo3,
            playerInfo4
        ];
    }

    public static function playerId(id:int):PlayerIdDTO {
        const dto:PlayerIdDTO = new PlayerIdDTO();
        dto.id = id;
        return dto;
    }

    public static function slotsPos(playerId:int, orientation:SlotsOrientation, i:int, j:int):SlotsPosDTO {
        const dto:SlotsPosDTO = new SlotsPosDTO();
        dto.playerId = DtoMock.playerId(playerId);
        dto.orientation = orientation;
        dto.pos = new PointDTO();
        dto.pos.x = i * CellSize.SIZE.id();
        dto.pos.y = j * CellSize.SIZE.id();
        return dto;
    }

    public static function slotsPosLandscape():Vector.<SlotsPosDTO> {
        return new <SlotsPosDTO>[
            slotsPos(3, SlotsOrientation.BOTTOM_RIGHT, 12, 14)
        ];
    }

    public static function slotsPosPortrait():Vector.<SlotsPosDTO> {
        return new <SlotsPosDTO>[
            slotsPos(1, SlotsOrientation.BOTTOM_LEFT, 2, 10)
        ];
    }

    private static var buildingIdIterator:int = 0;

    public static function buildingId(id:int):BuildingIdDTO {
        const dto:BuildingIdDTO = new BuildingIdDTO();
        dto.id = id;
        return dto;
    }

    public static function point(x:Number, y:Number):PointDTO {
        const dto:PointDTO = new PointDTO();
        dto.x = x;
        dto.y = y;
        return dto;
    }

    public static function building(buildingType:BuildingType, level:BuildingLevel, ownerId:int, i:int, j:int):BuildingDTO {
        const dto:BuildingDTO = new BuildingDTO();
        dto.id = buildingId(buildingIdIterator++);
        dto.building = buildingPrototype(buildingType, level);
        if (ownerId > -1) dto.owner = playerId(ownerId);
        dto.population = 47;
        dto.strengthened = false;
        dto.pos = point(i * CellSize.SIZE.id() + CellSize.SIZE.id() / 2,
                j * CellSize.SIZE.id() + CellSize.SIZE.id() / 2);
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
