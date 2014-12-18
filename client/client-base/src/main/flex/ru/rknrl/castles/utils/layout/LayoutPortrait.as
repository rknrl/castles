package ru.rknrl.castles.utils.layout {
import flash.display.DisplayObject;
import flash.text.TextFormat;

import ru.rknrl.castles.game.layout.GameLayout;
import ru.rknrl.castles.game.layout.GameLayoutPortrait;
import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.menu.screens.main.popup.popup.Popup;
import ru.rknrl.castles.menu.screens.main.popup.popup.PopupPortrait;
import ru.rknrl.castles.menu.screens.main.popup.popup.item.PopupItem;
import ru.rknrl.castles.menu.screens.main.popup.popup.item.PopupItemPortrait;
import ru.rknrl.castles.menu.slider.ScreenSlider;
import ru.rknrl.castles.menu.slider.ScreenSliderPortrait;
import ru.rknrl.castles.utils.locale.CastlesLocale;

public class LayoutPortrait extends Layout {

    public function LayoutPortrait(stageWidth:int, stageHeight:int) {
        const scale:Number = getScale(stageWidth, stageHeight, 320, 568); // iphone 5

        super(stageWidth, stageHeight, scale);

        initTextFormats(scale);

        _headerHeight = 48 * scale;

        _titleHeight = 64 * scale;

        _footerHeight = 96 * scale;

        _shopItemWidth = 64 * scale;

        _menuBuildingScale = 1.6;

        _fastAndTrustCircleRadius = 70 * scale;
        _fastAndTrustCircleX = stageWidth - 100 * scale;
        _fastAndTrustCircleY = 350 * scale;

        _saleCircleRadius = 100 * scale;
        _saleCircleX = 110 * scale;
        _saleCircleY = 200 * scale;
    }

    // fonts

    private function initTextFormats(scale:Number):void {
        _popupTitleTextFormat = lightCenter(24 * scale, 0x555555);
        _popupItemNameTextFormat = light(24 * scale);
        _popupItemInfoTextFormat = light(18 * scale, 0x555555);
        _popupItemPriceTextFormat = light(24 * scale);
    }

    private var _popupTitleTextFormat:TextFormat;

    public function get popupTitleTextFormat():TextFormat {
        return _popupTitleTextFormat;
    }

    private var _popupItemNameTextFormat:TextFormat;

    public function get popupItemNameTextFormat():TextFormat {
        return _popupItemNameTextFormat;
    }

    private var _popupItemInfoTextFormat:TextFormat;

    public function get popupItemInfoTextFormat():TextFormat {
        return _popupItemInfoTextFormat;
    }

    private var _popupItemPriceTextFormat:TextFormat;


    public function get popupItemPriceTextFormat():TextFormat {
        return _popupItemPriceTextFormat;
    }

    // title

    private var _headerHeight:int;

    public function get headerHeight():int {
        return _headerHeight;
    }

    override public function get titleTop():int {
        return _headerHeight;
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
        return _headerHeight + titleHeight;
    }

    override public function get bodyWidth():int {
        return stageWidth;
    }

    override public function get bodyHeight():int {
        return stageHeight - bodyTop;
    }

    // footer
    public function get footerTop():int {
        return stageHeight - footerHeight;
    }

    public function get footerWidth():int {
        return stageWidth;
    }

    private var _footerHeight:int;

    public function get footerHeight():int {
        return _footerHeight;
    }

    public final function get footerCenterX():int {
        return footerWidth / 2;
    }

    public final function get footerCenterY():int {
        return footerTop + footerHeight / 2;
    }

    // screen slider

    override public function createSlider(screens:Vector.<MenuScreen>, locale:CastlesLocale):ScreenSlider {
        return new ScreenSliderPortrait(screens, this);
    }

    override public function updateSlider(slider:ScreenSlider):void {
        ScreenSliderPortrait(slider).updateLayout(this);
    }

    // menu start location

    override public function get onlyStartLocationInMainScreen():Boolean {
        return false;
    }

    override public function get locationCenterX():int {
        return stageCenterX;
    }

    override public function get locationCenterY():int {
        return bodyTop + (bodyHeight - footerHeight) / 2;
    }

    override public function get playCenterY():int {
        return footerCenterY;
    }

    // shop

    private var _shopItemWidth:int;

    override public function get shopItemWidth():int {
        return _shopItemWidth;
    }

    // skills


    override public function get skillPlusSize():int {
        return footerHeight;
    }

    // bank button

    override protected function get rectButtonWidth():int {
        return stageWidth - minigap * 2;
    }

    override protected function get rectButtonHeight():int {
        return footerHeight - minigap * 2;
    }

    override public function get bankButtonCenterX():int {
        return footerCenterX;
    }

    override public function get bankButtonCenterY():int {
        return footerCenterY;
    }

    // bank circles

    private var _fastAndTrustCircleRadius:int;

    override public function get fastAndTrustCircleRadius():int {
        return _fastAndTrustCircleRadius;
    }

    private var _fastAndTrustCircleX:int;

    override public function get fastAndTrustCircleX():int {
        return _fastAndTrustCircleX;
    }

    private var _fastAndTrustCircleY:int;

    override public function get fastAndTrustCircleY():int {
        return _fastAndTrustCircleY;
    }

    private var _saleCircleRadius:int;

    override public function get saleCircleRadius():int {
        return _saleCircleRadius;
    }

    private var _saleCircleX:int;

    override public function get saleCircleX():int {
        return _saleCircleX;
    }

    private var _saleCircleY:int;

    override public function get saleCircleY():int {
        return _saleCircleY;
    }

    // no connection screen

    override public function get noConnectionTitleCenterX():int {
        return stageCenterX;
    }

    override public function get noConnectionTitleCenterY():int {
        return bodyTop;
    }

    override public function get noConnectionButtonCenterX():int {
        return bankButtonCenterX;
    }

    override public function get noConnectionButtonCenterY():int {
        return bankButtonCenterY;
    }

    // game over screen

    override public function get gameOverTitleCenterX():int {
        return titleCenterX;
    }

    override public function get gameOverTitleCenterY():int {
        return titleCenterY;
    }

    override public function get gameOverRewardCenterX():int {
        return stageCenterX;
    }

    override public function get gameOverRewardCenterY():int {
        return bodyTop + (bodyHeight - rectButtonHeight * 2) / 2;
    }

    override public function get gameOverToMenuButtonCenterX():int {
        return bankButtonCenterX;
    }

    override public function get gameOverToMenuButtonCenterY():int {
        return footerTop - footerHeight / 2;
    }

    override public function get gameOverAgainButtonCenterX():int {
        return bankButtonCenterX;
    }

    override public function get gameOverAgainButtonCenterY():int {
        return bankButtonCenterY;
    }

    // popup item

    override public function createPopupItem(icon:DisplayObject, name:String, info:String, price:int, color:uint):PopupItem {
        return new PopupItemPortrait(icon, name, info, price, color, stageWidth, this);
    }

    override public function updatePopupItem(item:PopupItem):void {
        PopupItemPortrait(item).updateLayout(stageWidth, this);
    }

    override public function createPopup(title:String, items:Vector.<DisplayObject>):Popup {
        return new PopupPortrait(title, items, this);
    }

    override public function updatePopup(popup:Popup):void {
        PopupPortrait(popup).updateLayout(this);
    }

    // other

    public var _menuBuildingScale:Number;

    override public function get menuBuildingScale():Number {
        return _menuBuildingScale;
    }

    override public function createGameLayout(w:int, h:int):GameLayout {
        return new GameLayoutPortrait(w, h, stageWidth, stageHeight, scale);
    }
}
}
