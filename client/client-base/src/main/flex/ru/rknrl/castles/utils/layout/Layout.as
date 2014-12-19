package ru.rknrl.castles.utils.layout {
import flash.display.DisplayObject;
import flash.text.Font;
import flash.text.TextFormat;
import flash.text.TextFormatAlign;

import ru.rknrl.castles.game.layout.GameLayout;
import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.menu.screens.main.popup.popup.Popup;
import ru.rknrl.castles.menu.screens.main.popup.popup.item.PopupItem;
import ru.rknrl.castles.menu.slider.ScreenSlider;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.funnyUi.buttons.RectButton;
import ru.rknrl.utils.OverrideMe;

// todo Points
public class Layout {
    protected static function getScale(stageWidth:int, stageHeight:int, originalWidth:int, originalHeight:int):Number {
        const widthScale:Number = stageWidth / originalWidth;
        const heightScale:Number = stageHeight / originalHeight;
        return Math.min(widthScale, heightScale);
    }

    private var _scale:Number;

    public function get scale():Number {
        return _scale;
    }

    public function Layout(stageWidth:int, stageHeight:int, scale:Number) {
        _stageWidth = stageWidth;
        _stageHeight = stageHeight;
        _stageCenterX = _stageWidth / 2;
        _stageCenterY = _stageHeight / 2;
        _scale = scale;

        _gap = 10 * scale;
        _minigap = 4 * scale;
        rectButtonCorner = 8 * scale;

        _titleY = 40 * scale;

        _shopItemGap = 8 * scale;

        _popupPadding = 10 * scale;
        _popupGap = 20 * scale;
        _popupTitleHeight = 40 * scale;
        _popupIconSize = 64 * scale;
        _popupIconGap = 10 * scale;
        _popupCornerRadius = 20 * scale;

        _progressBarSize = 64 * scale;

        _topAvatarBitmapSize = 32 * scale;
        _topAvatarHeight = 64 * scale;

        initTextFormats(scale);
    }


    // fonts

    private static const lightFont:Font = new LightFont();
    private static const boldFont:Font = new BoldFont();

    public static function light(size:Number, color:Object = null):TextFormat {
        return new TextFormat(lightFont.fontName, size, color);
    }

    public static function bold(size:Number, color:Object = null):TextFormat {
        return new TextFormat(boldFont.fontName, size, color, true);
    }

    public static function lightCenter(size:Number, color:Object = null):TextFormat {
        return new TextFormat(lightFont.fontName, size, color, null, null, null, null, null, TextFormatAlign.CENTER);
    }

    // static fonts

    public static const unitCounterTextFormat:TextFormat = bold(18);
    public static const buildingCounterTextFormat:TextFormat = bold(18, 0xffffff);
    public static const gameItemCounterTextFormat:TextFormat = bold(40, 0xffffff);
    public static const shopItemCountTextFormat:TextFormat = bold(18, 0xffffff);
    public static const skillNameTextFormat:TextFormat = light(24);

    // dynamic fonts

    private function initTextFormats(scale:Number):void {
        _rectButtonTextFormat = lightCenter(18 * scale, 0xffffff);
        _noConnectionTitleTextFormat = lightCenter(56 * scale);
        _shopTitleTextFormat = light(18 * scale);
        _skillsTitleTextFormat = light(18 * scale);
        _playTextFormat = light(48 * scale);
        _enterGameTextFormat = light(32 * scale);
        _loadingTextFormat = light(48 * scale);
        _headerTextFormat = light(20 * scale);
        _gameOverTitleTextFormat = light(72 * scale);
        _gameOverRewardTextFormat = light(54 * scale);
        _topAvatarTextFormat = lightCenter(32 * scale, 0);
    }

    private var _rectButtonTextFormat:TextFormat;

    private var _noConnectionTitleTextFormat:TextFormat;

    public final function get noConnectionTitleTextFormat():TextFormat {
        return _noConnectionTitleTextFormat;
    }

    private var _shopTitleTextFormat:TextFormat;

    public final function get shopTitleTextFormat():TextFormat {
        return _shopTitleTextFormat;
    }

    private var _skillsTitleTextFormat:TextFormat;

    public final function get skillsTitleTextFormat():TextFormat {
        return _skillsTitleTextFormat;
    }

    private var _playTextFormat:TextFormat;

    public final function get playTextFormat():TextFormat {
        return _playTextFormat;
    }

    private var _enterGameTextFormat:TextFormat;

    public final function get enterGameTextFormat():TextFormat {
        return _enterGameTextFormat;
    }

    private var _loadingTextFormat:TextFormat;

    public final function get loadingTextFormat():TextFormat {
        return _loadingTextFormat;
    }

    private var _headerTextFormat:TextFormat;

    public final function get headerTextFormat():TextFormat {
        return _headerTextFormat;
    }

    private var _gameOverTitleTextFormat:TextFormat;

    public final function get gameOverTitleTextFormat():TextFormat {
        return _gameOverTitleTextFormat;
    }

    private var _gameOverRewardTextFormat:TextFormat;

    public final function get gameOverRewardTextFormat():TextFormat {
        return _gameOverRewardTextFormat;
    }

    private var _topAvatarTextFormat:TextFormat;

    public final function get topAvatarTextFormat():TextFormat {
        return _topAvatarTextFormat;
    }

    // stage

    private var _stageWidth:int;

    public final function get stageWidth():int {
        return _stageWidth;
    }

    private var _stageHeight:int;

    public final function get stageHeight():int {
        return _stageHeight;
    }

    private var _stageCenterX:int;

    public final function get stageCenterX():int {
        return _stageCenterX;
    }

    private var _stageCenterY:int;

    public final function get stageCenterY():int {
        return _stageCenterY;
    }

    // gap

    private var _gap:int;


    public final function get gap():int {
        return _gap;
    }

    private var _minigap:int;

    public final function get minigap():int {
        return _minigap;
    }

    // body

    public function get bodyTop():int {
        throw OverrideMe();
    }

    public function get bodyHeight():int {
        throw OverrideMe();
    }

    public final function get bodyCenterY():int {
        return bodyTop + bodyHeight / 2;
    }

    // footer

    public final function get footerTop():int {
        return stageHeight - footerHeight;
    }

    public function get footerHeight():int {
        throw OverrideMe;
    }

    public final function get footerCenterY():int {
        return footerTop + footerHeight / 2;
    }

    // info

    private var _titleY:int;

    public final function get titleY():int {
        return _titleY;
    }

    // screen slider

    public function createSlider(screens:Vector.<Screen>, locale:CastlesLocale):ScreenSlider {
        throw OverrideMe();
    }

    public function updateSlider(slider:ScreenSlider):void {
        throw OverrideMe();
    }

    // points

    public function get pointRadius():int {
        throw OverrideMe();
    }

    public function get pointY():int {
        throw OverrideMe();
    }

    public function get pointGap():int {
        throw OverrideMe();
    }

    // menu start location

    public function get locationCenterX():int {
        throw OverrideMe();
    }

    public function get locationCenterY():int {
        throw OverrideMe();
    }

    // shop

    private var _shopItemGap:int;

    public function get shopItemGap():int {
        return _shopItemGap;
    }

    // rect button

    protected function get rectButtonWidth():int {
        throw OverrideMe();
    }

    protected function get rectButtonHeight():int {
        throw OverrideMe();
    }

    private var rectButtonCorner:int;

    public final function createRectButton(text:String, color:uint):RectButton {
        return new RectButton(rectButtonWidth, rectButtonHeight, rectButtonCorner, text, _rectButtonTextFormat, color);
    }

    public final function updateRectButton(rectButton:RectButton):void {
        rectButton.updateLayout(rectButtonWidth, rectButtonHeight, rectButtonCorner, _rectButtonTextFormat);
    }

    // bank button

    public function get bankButtonCenterX():int {
        throw OverrideMe();
    }

    public function get bankButtonCenterY():int {
        throw OverrideMe();
    }

    // no connection screen

    public function get noConnectionTitleCenterX():int {
        throw OverrideMe();
    }

    public function get noConnectionTitleCenterY():int {
        throw OverrideMe();
    }

    public function get noConnectionButtonCenterX():int {
        throw OverrideMe();
    }

    public function get noConnectionButtonCenterY():int {
        throw OverrideMe();
    }

    // game over screen

    public function get gameOverTitleCenterX():int {
        throw OverrideMe();
    }

    public function get gameOverTitleCenterY():int {
        throw OverrideMe();
    }

    public function get gameOverRewardCenterX():int {
        throw OverrideMe();
    }

    public function get gameOverRewardCenterY():int {
        throw OverrideMe();
    }

    public function get gameOverToMenuButtonCenterX():int {
        throw OverrideMe();
    }

    public function get gameOverToMenuButtonCenterY():int {
        throw OverrideMe();
    }

    public function get gameOverAgainButtonCenterX():int {
        throw OverrideMe();
    }

    public function get gameOverAgainButtonCenterY():int {
        throw OverrideMe();
    }

    // popup item

    public function createPopupItem(icon:DisplayObject, name:String, info:String, price:int, color:uint):PopupItem {
        throw OverrideMe();
    }

    public function updatePopupItem(item:PopupItem):void {
        throw OverrideMe();
    }

    // popup

    private var _popupPadding:int;

    public final function get popupPadding():int {
        return _popupPadding;
    }

    private var _popupGap:int;

    public final function get popupGap():int {
        return _popupGap;
    }

    private var _popupTitleHeight:int;

    public final function get popupTitleHeight():int {
        return _popupTitleHeight;
    }

    private var _popupIconSize:int;

    public final function get popupIconSize():int {
        return _popupIconSize;
    }

    private var _popupIconGap:int;

    public final function get popupIconGap():int {
        return _popupIconGap;
    }

    private var _popupCornerRadius:int;

    public function get popupCornerRadius():int {
        return _popupCornerRadius;
    }

    public function createPopup(title:String, items:Vector.<DisplayObject>):Popup {
        throw OverrideMe();
    }

    public function updatePopup(popup:Popup):void {
        throw OverrideMe();
    }

    // top

    private var _topAvatarBitmapSize:int;

    public function get topAvatarBitmapSize():int {
        return _topAvatarBitmapSize;
    }

    private var _topAvatarHeight:int;

    public function get topAvatarHeight():int {
        return _topAvatarHeight;
    }

    // other

    public function get menuBuildingScale():Number {
        throw OverrideMe();
    }

    private var _progressBarSize:int;

    public function get progressBarSize():int {
        return _progressBarSize;
    }

    public function createGameLayout(w:int, h:int):GameLayout {
        throw OverrideMe();
    }
}
}
