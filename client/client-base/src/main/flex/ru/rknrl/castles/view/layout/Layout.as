package ru.rknrl.castles.view.layout {

import flash.text.TextField;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.castles.view.popups.popup.PopupItem;

public class Layout {
    public static const navigationPointSize:int = 8;
    public static const navigationPointGap:int = 8;
    public static const itemSize:int = 48;
    public static const itemGap:int = 12;
    public static const shadowDistance:int = 36;
    public static const popupFlyIconShadowY:Number = 8;

    protected static function getScale(stageWidth:int, stageHeight:int, originalWidth:int, originalHeight:int):Number {
        const widthScale:Number = stageWidth / originalWidth;
        const heightScale:Number = stageHeight / originalHeight;
        return Math.min(widthScale, heightScale);
    }

    public function Layout(stageWidth:int, stageHeight:int, scale:Number, contentsScaleFactor:Number) {
        _screenWidth = stageWidth;
        _screenHeight = stageHeight;
        _scale = scale;
        _contentsScaleFactor = contentsScaleFactor;
    }

    private var _contentsScaleFactor:Number;

    public function get contentsScaleFactor():Number {
        return _contentsScaleFactor;
    }

    private var _scale:Number;

    public function get scale():Number {
        return _scale;
    }

    public function get bitmapDataScale():Number {
        return _scale * _contentsScaleFactor;
    }

    private var _screenWidth:Number;

    public function get screenWidth():Number {
        return _screenWidth;
    }

    private var _screenHeight:Number;

    public function get screenHeight():Number {
        return _screenHeight;
    }

    public function get screenCenterX():Number {
        return screenWidth / 2;
    }

    public function get footerHeight():Number {
        throw new Error();
    }

    public function get navigationPointsY():Number {
        return screenHeight - footerHeight - 8 * scale;
    }

    public function get footerCenterY():Number {
        return screenHeight - footerHeight / 2;
    }

    public function get contentCenterY():Number {
        throw new Error();
    }

    public function balance(width:int):Point {
        throw new Error();
    }

    public function title(width:int, height:int):Point {
        throw new Error();
    }

    public function get needShield():Boolean {
        throw new Error();
    }

    public function get buttonX():Number {
        throw new Error();
    }

    public function get buttonY():Number {
        throw new Error();
    }

    public function buttonWidth(textWidth: Number):Number {
        throw new Error();
    }

    public function get buttonHeight():Number {
        throw new Error();
    }

    public function get corner():Number {
        return 8 * scale;
    }

    public function get topAvatarSize():int {
        return itemSize * scale;
    }

    public function get startLocationY():Number {
        return contentCenterY + shadowDistance * scale;
    }

    public function gameAreaPos(areaH:int, areaV:int):Point {
        throw new Error();
    }

    public function get gameMagicItemsY():Number {
        throw new Error();
    }

    // game avatar

    public function get notScaledGameAvatarSize():Number {
        throw new Error()
    }

    public function get gameAvatarSize():Number {
        return notScaledGameAvatarSize * scale;
    }

    public function gameAvatarPos(number:int, areaH:int, areaV:int):Point {
        throw new Error()
    }

    public function gameAvatarBitmapPos(number:int):Point {
        throw new Error()
    }

    public function gameAvatarTextPos(number:int, width:Number, height:Number):Point {
        throw new Error()
    }

    public function createGameAvatarTextField():TextField {
        throw new Error()
    }

    // popup

    public function createPopup(titleText:String, cancelText:String, items:Vector.<PopupItem>, layout:Layout):Popup {
        throw new Error();
    }

    public function get popupPadding():Number {
        throw new Error();
    }

    public function get popupItemSize():Number {
        throw new Error()
    }

    public function get popupCancelHeight():Number {
        throw new Error()
    }

    public function get popupTitleHeight():Number {
        throw new Error()
    }

    public function popupWidth(itemsCount:int):Number {
        throw new Error()
    }

    public function get popupCancelButtonGap():Number {
        throw new Error()
    }

    public function get popupIconPos():Point {
        throw new Error()
    }

    public function popupTextPos(textWidth:Number, textHeight:Number):Point {
        throw new Error()
    }

    public function popupPricePos(priceWidth:Number, priceHeight:Number):Point {
        throw new Error()
    }

    public function popupTitleTextY(height:Number):Number {
        throw new Error();
    }
}
}
