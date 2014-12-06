package ru.rknrl.castles.game.layout {
import flash.display.DisplayObject;

import ru.rknrl.castles.game.ui.avatar.AvatarData;
import ru.rknrl.castles.game.view.GameConstants;
import ru.rknrl.dto.ItemType;
import ru.rknrl.utils.OverrideMe;

public class GameLayout {
    public function GameLayout(w:int, h:int, stageWidth:int, stageHeight:int, scale:Number) {
        _w = w;
        _h = h;
        _originalGameWidth = w * GameConstants.cellSize;
        _originalGameHeight = h * GameConstants.cellSize;

        update(stageWidth, stageHeight, scale)
    }

    public function update(stageWidth:int, stageHeight:int, scale:Number):void {
        _stageWidth = stageWidth;
        _stageHeight = stageHeight;

        _gameWidth = _originalGameWidth * scale;
        _gameHeight = _originalGameHeight * scale;

        _gameLeft = (_stageWidth - _gameWidth) / 2;
        _gameTop = 48 * scale;

        _gameItemSize = 48 * scale;
        gameItemOpticallyCompensation = -4 * scale;
    }

    private var _w:int;

    public final function get w():int {
        return _w;
    }

    private var _h:int;

    public final function get h():int {
        return _h;
    }

    private var _stageWidth:int;

    public final function get stageWidth():int {
        return _stageWidth;
    }

    private var _stageHeight:int;

    public final function get stageHeight():int {
        return _stageHeight;
    }

    // game

    private var _originalGameWidth:int;

    public final function get originalGameWidth():int {
        return _originalGameWidth;
    }

    private var _originalGameHeight:int;

    public final function get originalGameHeight():int {
        return _originalGameHeight;
    }

    private var _gameWidth:int;

    public final function get gameWidth():int {
        return _gameWidth;
    }

    private var _gameHeight:int;

    public final function get gameHeight():int {
        return _gameHeight;
    }

    private var _gameLeft:int;

    public final function get gameLeft():int {
        return _gameLeft;
    }

    private var _gameTop:int;

    public final function get gameTop():int {
        return _gameTop;
    }

    public final function get gameRight():int {
        return gameLeft + gameWidth;
    }

    public final function get gameBottom():int {
        return gameTop + gameHeight;
    }

    // game avatars size

    public function createGameAvatar(i:int, data:AvatarData):DisplayObject {
        throw OverrideMe();
    }

    public function updateGameAvatar(i:int, avatar:DisplayObject):void {
        throw OverrideMe();
    }

    // game items

    private var _gameItemSize:int;

    public final function get gameItemSize():int {
        return _gameItemSize;
    }

    public function get gameItemHorGap():int {
        throw OverrideMe();
    }

    public final function get gameItemHorPadding():int {
        return (stageWidth - (gameItemSize + gameItemHorGap) * ItemType.values.length + gameItemHorGap) / 2;
    }

    private var gameItemOpticallyCompensation:int;

    public final function get gameItemLeft():int {
        return gameItemHorPadding + gameItemSize / 2 - gameItemOpticallyCompensation;
    }

    public final function get gameItemTop():int {
        return stageHeight - (stageHeight - gameBottom) / 2;
    }
}
}
