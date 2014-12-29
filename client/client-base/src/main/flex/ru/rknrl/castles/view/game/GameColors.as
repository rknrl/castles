package ru.rknrl.castles.view.game {
import flash.display.BitmapData;
import flash.geom.ColorTransform;

import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.dto.PlayerIdDTO;

public class GameColors {
    public static function bitmapData(owner:BuildingOwner):BitmapData {
        return owner.hasOwner ? bitmapDataById(owner.ownerId) : Colors.noOwnerGroundColor;
    }

    public static function bitmapDataById(playerId:PlayerIdDTO):BitmapData {
        return Colors.groundColors[playerId.id];
    }

    public static function transform(owner:BuildingOwner):ColorTransform {
        return owner.hasOwner ? transformById(owner.ownerId) : Colors.noOwnerTransform;
    }

    public static function transformById(playerId:PlayerIdDTO):ColorTransform {
        return Colors.playerTransforms[playerId.id];
    }

    public static function colorById(playerId:PlayerIdDTO):uint {
        return Colors.playerColors[playerId.id];
    }
}
}
