package ru.rknrl.castles.game.layout {
import flash.display.DisplayObject;
import flash.geom.Point;
import flash.text.TextFormat;

import ru.rknrl.castles.game.ui.avatar.GameAvatarData;
import ru.rknrl.castles.game.ui.avatar.AvatarLandscape;
import ru.rknrl.castles.utils.layout.Layout;

public class GameLayoutLandscape extends GameLayout {
    public function GameLayoutLandscape(w:int, h:int, stageWidth:int, stageHeight:int, scale:Number) {
        super(w, h, stageWidth, stageHeight, scale);
    }

    override public function update(stageWidth:int, stageHeight:int, scale:Number):void {
        super.update(stageWidth, stageHeight, scale);

        _avatarsHorGap = 20 * scale;
        _avatarWidth = 140 * scale;
        _avatarHeight = 100 * scale;
        _avatarTextHeight = 96 * scale;

        _avatarBitmapSize = 64 * scale;
        _avatarVerGap = 4 * scale;

        _gameItemHorGap = 24 * scale;

        _avatarTextFormat = Layout.lightCenter(24 * scale);
    }

    // game avatars

    private var _avatarTextFormat:TextFormat;

    public function get avatarTextFormat():TextFormat {
        return _avatarTextFormat;
    }

    private var _avatarsHorGap:int;

    private function get avatarsLeft():int {
        return gameLeft - _avatarsHorGap
    }

    private function get avatarsRight():int {
        return gameRight + _avatarsHorGap
    }

    private function get avatarsTop():int {
        return gameTop;
    }

    private function get avatarsBottom():int {
        return gameBottom;
    }

    private var _avatarWidth:int;

    public function get avatarWidth():int {
        return _avatarWidth;
    }

    private var _avatarHeight:int;

    public function get avatarHeight():int {
        return _avatarHeight;
    }

    public function get avatarsPos():Vector.<Point> {
        return new <Point>[
            new Point(avatarsLeft - _avatarWidth / 2, avatarsTop + _avatarHeight),
            new Point(avatarsRight + _avatarWidth / 2, avatarsTop + _avatarHeight),
            new Point(avatarsLeft - _avatarWidth / 2, avatarsBottom - _avatarHeight),
            new Point(avatarsRight + _avatarWidth / 2, avatarsBottom - _avatarHeight)
        ];
    }

    private var _avatarBitmapSize:int;

    public function get avatarBitmapSize():int {
        return _avatarBitmapSize;
    }

    private var _avatarVerGap:int;

    public function get avatarVerGap():int {
        return _avatarVerGap;
    }

    private var _avatarTextHeight:int;

    public function get avatarTextHeight():int {
        return _avatarTextHeight;
    }

    override public function createGameAvatar(i:int, data:GameAvatarData):DisplayObject {
        const avatar:AvatarLandscape = new AvatarLandscape(data, this);
        posAvatar(i, avatar);
        return avatar;
    }

    override public function updateGameAvatar(i:int, avatar:DisplayObject):void {
        const avatarLandscape:AvatarLandscape = AvatarLandscape(avatar);
        avatarLandscape.updateLayout(this);
        posAvatar(i, avatarLandscape);
    }

    private function posAvatar(i:int, avatar:DisplayObject):void {
        const pos:Point = avatarsPos[i];
        avatar.x = pos.x;
        avatar.y = pos.y;
    }

    // game items

    public var _gameItemHorGap:int;

    override public function get gameItemHorGap():int {
        return _gameItemHorGap;
    }
}
}
