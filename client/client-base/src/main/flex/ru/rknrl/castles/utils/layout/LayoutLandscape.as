package ru.rknrl.castles.utils.layout {
import flash.display.DisplayObject;
import flash.text.TextFormat;

import ru.rknrl.castles.game.layout.GameLayout;
import ru.rknrl.castles.game.layout.GameLayoutLandscape;
import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.menu.screens.main.popup.popup.Popup;
import ru.rknrl.castles.menu.screens.main.popup.popup.PopupLandscape;
import ru.rknrl.castles.menu.screens.main.popup.popup.item.PopupItem;
import ru.rknrl.castles.menu.screens.main.popup.popup.item.PopupItemLandscape;
import ru.rknrl.castles.menu.slider.ScreenSlider;
import ru.rknrl.castles.menu.slider.ScreenSliderLandscape;
import ru.rknrl.castles.utils.locale.CastlesLocale;

public class LayoutLandscape extends Layout {
    public function LayoutLandscape(stageWidth:int, stageHeight:int) {
        const scale:Number = getScale(stageWidth, stageHeight, 1024, 768); // ipad

        super(stageWidth, stageHeight, scale);

        initTextFormats(scale);

        _titleTop = 100 * scale;
        _titleHeight = 64 * scale;

        _shopItemWidth = 48 * scale;
        _skillPlusSize = 64 * scale;

        _popupItemNameY = popupIconSize;
        _popupItemInfoY = popupIconSize + 24 * scale;
        _popupItemPriceY = popupIconSize + 48 * scale;

        _bankButtonWidth = 450 * scale;
        _bankButtonHeight = 96 * scale;

        _noConnectionTitleY = stageCenterY - 100 * scale;
        _noConnectionButtonCenterY = stageCenterY + 100 * scale;

        _popupItemWidth = 200 * scale;

        _menuBuildingScale = 3;

        _pointRadius = 8 * scale;
        _pointGap = 8 * scale;
        _pointY = stageHeight - 96 * scale;
    }

    // fonts

    private function initTextFormats(scale:Number):void {
        _popupTitleTextFormat = lightCenter(24 * scale, 0x555555);
        _popupItemPriceTextFormat = light(24 * scale);
        _popupItemInfoTextFormat = light(18 * scale, 0x555555);
        _popupItemNameTextFormat = light(24 * scale);
    }

    private var _popupTitleTextFormat:TextFormat;

    public function get popupTitleTextFormat():TextFormat {
        return _popupTitleTextFormat;
    }

    private var _popupItemPriceTextFormat:TextFormat;

    public function get popupItemPriceTextFormat():TextFormat {
        return _popupItemPriceTextFormat;
    }

    private var _popupItemInfoTextFormat:TextFormat;

    public function get popupItemInfoTextFormat():TextFormat {
        return _popupItemInfoTextFormat;
    }

    private var _popupItemNameTextFormat:TextFormat;

    public function get popupItemNameTextFormat():TextFormat {
        return _popupItemNameTextFormat;
    }

    // title

    private var _titleTop:int;

    override public function get titleTop():int {
        return _titleTop;
    }

    override public function get titleWidth():int {
        return stageWidth;
    }

    private var _titleHeight:int;

    override public function get titleHeight():int {
        return _titleHeight;
    }

    // body

    override public function get bodyTop():int {
        return 0;
    }


    override public function get bodyWidth():int {
        return stageWidth;
    }

    override public function get bodyHeight():int {
        return stageHeight;
    }

    // screen slider

    override public function createSlider(screens:Vector.<MenuScreen>, locale:CastlesLocale):ScreenSlider {
        return new ScreenSliderLandscape(screens, this);
    }

    override public function updateSlider(slider:ScreenSlider):void {
        ScreenSliderLandscape(slider).updateLayout(this);
    }

    // points

    private var _pointRadius: int;

    override public function get pointRadius():int {
        return _pointRadius;
    }

    private var _pointY: int;

    override public function get pointY():int {
        return _pointY;
    }

    private var _pointGap: int;

    override public function get pointGap():int {
        return _pointGap;
    }

    // menu start location


    override public function get onlyStartLocationInMainScreen():Boolean {
        return true;
    }

    override public function get locationCenterX():int {
        return bodyCenterX;
    }

    override public function get locationCenterY():int {
        return bodyCenterY;
    }

    override public function get playCenterY():int {
        return 0; // todo
    }

    // shop

    private var _shopItemWidth:int;

    override public function get shopItemWidth():int {
        return _shopItemWidth;
    }

    // skills

    private var _skillPlusSize:int;

    override public function get skillPlusSize():int {
        return _skillPlusSize;
    }

    // bank button

    private var _bankButtonWidth:int;

    override protected function get rectButtonWidth():int {
        return _bankButtonWidth;
    }

    private var _bankButtonHeight:int;

    override protected function get rectButtonHeight():int {
        return _bankButtonHeight;
    }

    override public function get bankButtonCenterX():int {
        return bodyCenterX;
    }

    override public function get bankButtonCenterY():int {
        return bodyCenterY;
    }

    // no connection screen

    override public function get noConnectionTitleCenterX():int {
        return stageCenterX;
    }

    private var _noConnectionTitleY:int;

    override public function get noConnectionTitleCenterY():int {
        return _noConnectionTitleY;
    }

    override public function get noConnectionButtonCenterX():int {
        return stageCenterX;
    }

    private var _noConnectionButtonCenterY:int;

    override public function get noConnectionButtonCenterY():int {
        return _noConnectionButtonCenterY;
    }

    // game over screen

    override public function get gameOverTitleCenterX():int {
        return stageCenterX;
    }

    override public function get gameOverTitleCenterY():int {
        return titleCenterY;
    }

    override public function get gameOverRewardCenterX():int {
        return stageCenterX;
    }

    override public function get gameOverRewardCenterY():int {
        return titleCenterY + (gameOverAgainButtonCenterTop - titleCenterY) / 2;
    }

    override public function get gameOverToMenuButtonCenterX():int {
        return stageCenterX;
    }

    override public function get gameOverToMenuButtonCenterY():int {
        return stageHeight - rectButtonHeight - rectButtonHeight / 2;
    }

    override public function get gameOverAgainButtonCenterX():int {
        return stageCenterX;
    }

    public function get gameOverAgainButtonCenterTop():int {
        return gameOverToMenuButtonCenterY - rectButtonHeight / 2 - gap - rectButtonHeight;
    }

    override public function get gameOverAgainButtonCenterY():int {
        return gameOverAgainButtonCenterTop + rectButtonHeight / 2;
    }

    // popup

    private var _popupItemWidth:int;

    public function get popupItemWidth():int {
        return _popupItemWidth;
    }

    private var _popupItemNameY:int;

    public function get popupItemNameY():int {
        return _popupItemNameY;
    }

    private var _popupItemInfoY:int;

    public function get popupItemInfoY():int {
        return _popupItemInfoY;
    }

    private var _popupItemPriceY:int;

    public function get popupItemPriceY():int {
        return _popupItemPriceY;
    }

    override public function createPopupItem(icon:DisplayObject, name:String, info:String, price:int, color:uint):PopupItem {
        return new PopupItemLandscape(icon, name, info, price, color, this);
    }

    override public function updatePopupItem(item:PopupItem):void {
        PopupItemLandscape(item).updateLayout(this);
    }

    override public function createPopup(title:String, items:Vector.<DisplayObject>):Popup {
        return new PopupLandscape(title, items, this);
    }

    override public function updatePopup(popup:Popup):void {
        PopupLandscape(popup).updateLayout(this);
    }

    // other

    public var _menuBuildingScale:Number;

    override public function get menuBuildingScale():Number {
        return _menuBuildingScale;
    }

    override public function createGameLayout(w:int, h:int):GameLayout {
        return new GameLayoutLandscape(w, h, stageWidth, stageHeight, scale);
    }
}
}
