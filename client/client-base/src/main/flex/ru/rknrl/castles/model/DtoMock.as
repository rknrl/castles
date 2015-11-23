//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model {
import protos.AccountConfig;
import protos.AccountId;
import protos.AccountState;
import protos.AccountType;
import protos.Authenticated;
import protos.BuildingDTO;
import protos.BuildingId;
import protos.BuildingLevel;
import protos.BuildingPrice;
import protos.BuildingPrototype;
import protos.BuildingType;
import protos.CellSize;
import protos.Item;
import protos.ItemStateDTO;
import protos.ItemStatesDTO;
import protos.ItemType;
import protos.Player;
import protos.PlayerId;
import protos.PointDTO;
import protos.Product;
import protos.Skill;
import protos.SkillLevel;
import protos.SkillType;
import protos.SkillUpgradePrice;
import protos.Slot;
import protos.SlotId;
import protos.SlotsOrientation;
import protos.SlotsPos;
import protos.Stat;
import protos.StatAction;
import protos.Top;
import protos.TopUserInfo;
import protos.UnitId;
import protos.UserInfo;

public class DtoMock {
    public static const mockAvatars:Vector.<String> = new <String>[
        "mock_avatars/1.png",
        "mock_avatars/2.png",
        "mock_avatars/3.png",
        "mock_avatars/4.png",
        "mock_avatars/5.png"
    ];

    public static function stat(action:StatAction):Stat {
        return new Stat(action);
    }

    public static function product():Product {
        return new Product(1, "title", "description", "photoUrl", 100, 1, "$");
    }

    public static function buildingPrototype(buildingType:BuildingType, level:BuildingLevel):BuildingPrototype {
        return new BuildingPrototype(buildingType, level);
    }

    public static function slot(slotId:SlotId, buildingPrototype:BuildingPrototype):Slot {
        return new Slot(slotId, buildingPrototype);
    }

    public static function slots():Vector.<Slot> {
        return new <Slot>[
            slot(SlotId.SLOT_1, buildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_2, buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_3, buildingPrototype(BuildingType.CHURCH, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_4, buildingPrototype(BuildingType.TOWER, BuildingLevel.LEVEL_1)),
            slot(SlotId.SLOT_5, buildingPrototype(BuildingType.HOUSE, BuildingLevel.LEVEL_1))
        ];
    }

    public static function skillLevel(skillType:SkillType, level:SkillLevel):Skill {
        return new Skill(skillType, level);
    }

    public static function skills():Vector.<Skill> {
        return new <Skill>[
            skillLevel(SkillType.ATTACK, SkillLevel.SKILL_LEVEL_0),
            skillLevel(SkillType.DEFENCE, SkillLevel.SKILL_LEVEL_3),
            skillLevel(SkillType.SPEED, SkillLevel.SKILL_LEVEL_1)
        ];
    }

    public static function item(itemType:ItemType, count:int):Item {
        return new Item(itemType, count);
    }

    public static function items():Vector.<Item> {
        const dto:Vector.<Item> = new <Item>[];
        for each(var itemType:ItemType in ItemType.values) {
            dto.push(item(itemType, 2))
        }
        return dto;
    }

    public static function accountState():AccountState {
        return new AccountState(slots(), skills(), items(), 100, 0, null, null, null);
    }

    public static function buildingPrice(level:BuildingLevel, price:int):BuildingPrice {
        return new BuildingPrice(level, price);
    }

    public static function buildingPrices():Vector.<BuildingPrice> {
        const dto:Vector.<BuildingPrice> = new <BuildingPrice>[];
        dto.push(buildingPrice(BuildingLevel.LEVEL_1, 4));
        dto.push(buildingPrice(BuildingLevel.LEVEL_2, 16));
        dto.push(buildingPrice(BuildingLevel.LEVEL_3, 64));
        return dto;
    }

    public static function skillUpgradePrice(totalLevel:int, price:int):SkillUpgradePrice {
        return new SkillUpgradePrice(totalLevel, price);
    }

    public static function skillUpgradePrices():Vector.<SkillUpgradePrice> {
        const dto:Vector.<SkillUpgradePrice> = new <SkillUpgradePrice>[];
        for (var i:int = 0; i < 9; i++) {
            dto.push(skillUpgradePrice(i, Math.pow(2, i)));
        }
        return dto;
    }

    public static function accountId(type:AccountType, id:String):AccountId {
        return new AccountId(type, id);
    }

    public static function userInfo(firstName:String, lastName:String, photoUrl:String):UserInfo {
        return new UserInfo(
                accountId(AccountType.DEV, "1"),
                firstName,
                lastName,
                null,
                photoUrl
        );
    }

    public static function topUserInfo(place:int, name:String, photoUrl:String):TopUserInfo {
        return new TopUserInfo(place, userInfo(name, null, photoUrl));
    }

    public static const topUser1:TopUserInfo = topUserInfo(1, "1", "mock_avatars/1.png");
    public static const topUser2:TopUserInfo = topUserInfo(2, "2", "mock_avatars/2.png");
    public static const topUser3:TopUserInfo = topUserInfo(3, "3", "mock_avatars/3.png");
    public static const topUser4:TopUserInfo = topUserInfo(4, "4", "mock_avatars/4.png");
    public static const topUser5:TopUserInfo = topUserInfo(5, "5", "mock_avatars/5.png");

    public static function topUsers():Vector.<TopUserInfo> {
        const dto:Vector.<TopUserInfo> = new <TopUserInfo>[];
        dto.push(topUser1);
        dto.push(topUser2);
        dto.push(topUser3);
        dto.push(topUser4);
        dto.push(topUser5);
        return dto;
    }

    public static function top():Top {
        return new Top(1, topUsers());
    }

    public static function config():AccountConfig {
        return new AccountConfig(
                buildingPrices(),
                skillUpgradePrices(),
                1,
                5
        );
    }

    public static function authenticated():Authenticated {
        return new Authenticated(
                accountState(),
                config(),
                top(),
                null,
                new <Product>[product()],
                null,
                false,
                null,
                null,
                null
        );
    }

    // utils

    public static function findSkillLevel(dto:Vector.<Skill>, skillType:SkillType):Skill {
        for each(var skillLevel:Skill in dto) {
            if (skillLevel.skillType == skillType) return skillLevel;
        }
        throw new Error("can't find skill level " + skillType)
    }

    public static function findItem(dto:Vector.<Item>, itemType:ItemType):Item {
        for each(var item:Item in dto) {
            if (item.itemType == itemType) return item;
        }
        throw new Error("can't find item " + itemType)
    }

    public static function findSlot(dto:Vector.<Slot>, slotId:SlotId):Slot {
        for each(var slot:Slot in dto) {
            if (slot.id == slotId) return slot;
        }
        throw new Error("can't find slot " + slotId)
    }

    private static function player(id:int, name:String, photoUrl:String):Player {
        return new Player(new PlayerId(id), userInfo(name, null, photoUrl));
    }

    public static const playerInfo0:Player = player(0, "Толя Янот", "mock_avatars/1.png");
    public static const playerInfo1:Player = player(1, "Sasha Serova", "mock_avatars/2.png");
    public static const playerInfo2:Player = player(2, "Napoleon1769", "mock_avatars/3.png");
    public static const playerInfo3:Player = player(3, "Виктория Викторовна", "mock_avatars/4.png");

    public static function playerInfosPortrait():Vector.<Player> {
        return new <Player>[
            playerInfo0,
            playerInfo1
        ];
    }

    public static function playerInfosLandscape():Vector.<Player> {
        return new <Player>[
            playerInfo0,
            playerInfo1,
            playerInfo2,
            playerInfo3
        ];
    }

    public static function winner():Player {
        return playerInfo0;
    }

    public static function losersPortrait():Vector.<Player> {
        return new <Player>[
            playerInfo1
        ];
    }

    public static function losersLandscape():Vector.<Player> {
        return new <Player>[
            playerInfo1,
            playerInfo2,
            playerInfo3
        ];
    }

    public static function playerId(id:int):PlayerId {
        return new PlayerId(id);
    }

    public static function slotsPos(playerId:int, orientation:SlotsOrientation, i:int, j:int):SlotsPos {
        const dto:SlotsPos = new SlotsPos(
                new PlayerId(playerId),
                new PointDTO(i * CellSize.SIZE.id(), j * CellSize.SIZE.id()),
                orientation
        );
        return dto;
    }

    public static function slotsPosLandscape():Vector.<SlotsPos> {
        return new <SlotsPos>[
            slotsPos(3, SlotsOrientation.BOTTOM_RIGHT, 12, 14)
        ];
    }

    public static function slotsPosPortrait():Vector.<SlotsPos> {
        return new <SlotsPos>[
            slotsPos(1, SlotsOrientation.BOTTOM_LEFT, 2, 10)
        ];
    }

    private static var buildingIdIterator:int = 0;

    public static function buildingId(id:int):BuildingId {
        return new BuildingId(id);
    }

    public static function point(x:Number, y:Number):PointDTO {
        return new PointDTO(x, y);
    }

    public static function building(buildingType:BuildingType, level:BuildingLevel, ownerId:int, i:int, j:int):BuildingDTO {
        return new BuildingDTO(
                buildingId(buildingIdIterator++),
                buildingPrototype(buildingType, level),
                point(i * CellSize.SIZE.id() + CellSize.SIZE.id() / 2,
                        j * CellSize.SIZE.id() + CellSize.SIZE.id() / 2),
                47,
                ownerId > -1 ? playerId(ownerId) : null,
                false
        );
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

    public static function unitId(id:int):UnitId {
        return new UnitId(id);
    }

    public static function itemState(itemType:ItemType, count:int, cooldownDuration:int, millisFromStart:int):ItemStateDTO {
        return new ItemStateDTO(itemType, count, millisFromStart, cooldownDuration);
    }

    public static function itemStates(states:Vector.<ItemStateDTO>):ItemStatesDTO {
        return new ItemStatesDTO(playerId(0), states);
    }
}
}
