package ru.rknrl.castles.view.menu.top {
import flash.display.Sprite;
import flash.events.Event;

import ru.rknrl.castles.view.utils.Fly;
import ru.rknrl.castles.view.utils.Shadow;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.dto.TopUserInfoDTO;

public class TopAvatar extends Sprite {
    private var avatar:Avatar;
    private var fly:Fly;

    public function TopAvatar(userInfo:TopUserInfoDTO, layout:Layout, loadImageManager:LoadImageManager) {
        addChild(avatar = new Avatar(userInfo.photoUrl, Layout.itemSize, layout.bitmapDataScale, loadImageManager));

        const shadow:Shadow = new Shadow();
        shadow.y = Layout.shadowDistance;
        addChild(shadow);

        fly = new Fly(avatar, shadow);

        _userInfo = userInfo;

        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var _userInfo:TopUserInfoDTO;

    public function get userInfo():TopUserInfoDTO {
        return _userInfo;
    }

    public function set userInfo(value:TopUserInfoDTO):void {
        _userInfo = value;
        avatar.url = userInfo.photoUrl;
    }

    public function set layout(value:Layout):void {
        avatar.bitmapDataScale = value.bitmapDataScale;
    }

    private function onEnterFrame(event:Event):void {
        fly.onEnterFrame();
    }
}
}
