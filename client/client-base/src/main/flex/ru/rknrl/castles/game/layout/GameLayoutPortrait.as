package ru.rknrl.castles.game.layout {
import flash.display.DisplayObject;
import flash.geom.Point;
import flash.text.TextFormat;

import ru.rknrl.castles.game.ui.avatar.AvatarData;
import ru.rknrl.castles.game.ui.avatar.AvatarPortrait;
import ru.rknrl.castles.utils.layout.Layout;

public class GameLayoutPortrait extends GameLayout {
    public function GameLayoutPortrait(w:int, h:int, stageWidth:int, stageHeight:int, scale:Number) {
        super(w, h, stageWidth, stageHeight, scale);
    }

    override public function update(stageWidth:int, stageHeight:int, scale:Number):void {
        super.update(stageWidth, stageHeight, scale);

        _avatarsHorPadding = 4 * scale;
        _avatarsVerPadding = 8 * scale;

        _avatarBitmapSize = 32 * scale;
        _avatarHorGap = 4 * scale;

        _gameItemHorGap = 12 * scale;

        _avatarTextFormat = Layout.light(18 * scale);
    }

    // game avatars

    private var _avatarsHorPadding:int;
    private var _avatarsVerPadding:int;

    private var _avatarTextFormat:TextFormat;

    public function get avatarTextFormat():TextFormat {
        return _avatarTextFormat;
    }

    private var _avatarBitmapSize:int;

    public function get avatarBitmapSize():int {
        return _avatarBitmapSize;
    }

    private var _avatarHorGap:int;

    public function get avatarHorGap():int {
        return _avatarHorGap;
    }

    override public function createGameAvatar(i:int, data:AvatarData):DisplayObject {
        const left:Boolean = i == 0;
        const avatar:AvatarPortrait = new AvatarPortrait(data, left, this);
        posAvatar(i, avatar);
        return avatar;
    }

    override public function updateGameAvatar(i:int, avatar:DisplayObject):void {
        const avatarPortrait:AvatarPortrait = AvatarPortrait(avatar);
        avatarPortrait.updateLayout(this);
        posAvatar(i, avatarPortrait);
    }

    private function posAvatar(i:int, avatar:DisplayObject):void {
        const pos:Point = i == 0 ? new Point(_avatarsHorPadding, _avatarsVerPadding) : new Point(stageWidth - _avatarsHorPadding - avatar.width, _avatarsVerPadding);
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
