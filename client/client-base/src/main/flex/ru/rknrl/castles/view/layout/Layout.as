package ru.rknrl.castles.view.layout {

import flash.text.TextField;

import ru.rknrl.castles.model.getSlotPos;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.castles.view.popups.popup.PopupItem;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.utils.OverrideMe;

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

    public static const startLocationScale:Number = 1.5;
    private static const slotGapX:Number = 20;
    private static const slotGapY:Number = 40;

    public static function slotPos(slotId:SlotId):Point {
        const pos:Point = getSlotPos(slotId);
        return new Point(pos.x * slotGapX, pos.y * slotGapY);
    }

    public function get startLocation():Point {
        return new Point(screenCenterX, startLocationY);
    }

    public function slotPosGlobal(slotId:SlotId):Point {
        const slot:Point = slotPos(slotId);
        return new Point(startLocation.x + slot.x * scale * startLocationScale, startLocation.y + slot.y * scale * startLocationScale);
    }

    public function get firstMagicItem():Point {
        return new Point(screenCenterX - (itemSize + itemGap) * 2 * scale, contentCenterY);
    }

    public static const flaskWidth:Number = 38;
    public static const flaskGap:Number = 16;

    public function get firstFlask():Point {
        return new Point(screenCenterX - (flaskWidth + flaskGap) * scale, contentCenterY);
    }

    public function get middleNavigationPoint():Point {
        return new Point(screenCenterX, navigationPointsY);
    }

    public function gameMagicItem(index:int):Point {
        const i:int = index - 2;
        return new Point(screenCenterX + (itemSize + itemGap) * i * scale, gameMagicItemsY);
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
        throw OverrideMe();
    }

    public function get navigationPointsY():Number {
        return screenHeight - footerHeight - 8 * scale;
    }

    public function get footerCenterY():Number {
        return screenHeight - footerHeight / 2;
    }

    public function get contentCenterY():Number {
        throw OverrideMe();
    }

    public function balance(width:int):Point {
        throw OverrideMe();
    }

    public function title(width:int, height:int):Point {
        throw OverrideMe();
    }

    public function get needShield():Boolean {
        throw OverrideMe();
    }

    public function get buttonX():Number {
        throw OverrideMe();
    }

    public function get buttonY():Number {
        throw OverrideMe();
    }

    public function buttonWidth(textWidth:Number):Number {
        throw OverrideMe();
    }

    public function get buttonHeight():Number {
        throw OverrideMe();
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
        throw OverrideMe();
    }

    public function get gameMagicItemsY():Number {
        throw OverrideMe();
    }

    public function rewardText(textWidth:Number, textHeight:Number):Point {
        throw OverrideMe();
    }

    // game avatar

    public function get notScaledGameAvatarSize():Number {
        throw OverrideMe()
    }

    public function get gameAvatarSize():Number {
        return notScaledGameAvatarSize * scale;
    }

    public function get supportedPlayersCount():int {
        throw OverrideMe;
    }

    public function gameAvatarPos(playerId:PlayerIdDTO, areaH:int, areaV:int):Point {
        throw OverrideMe()
    }

    public function gameAvatarBitmapPos(playerId:PlayerIdDTO):Point {
        throw OverrideMe()
    }

    public function gameAvatarTextPos(playerId:PlayerIdDTO, width:Number, height:Number):Point {
        throw OverrideMe()
    }

    public function createGameAvatarTextField():TextField {
        throw OverrideMe()
    }

    // popup

    public function createPopup(titleText:String, cancelText:String, items:Vector.<PopupItem>, layout:Layout):Popup {
        throw OverrideMe();
    }

    public function get popupPadding():Number {
        throw OverrideMe();
    }

    public function get popupItemSize():Number {
        throw OverrideMe()
    }

    public function get popupItemWidth():Number {
        throw OverrideMe();
    }

    public function get popupCancelHeight():Number {
        throw OverrideMe()
    }

    public function get popupTitleHeight():Number {
        throw OverrideMe()
    }

    public function popupWidth(itemsCount:int):Number {
        throw OverrideMe()
    }

    public function get popupCancelButtonGap():Number {
        throw OverrideMe()
    }

    public function get popupIconPos():Point {
        throw OverrideMe()
    }

    public function popupTextPos(textWidth:Number, textHeight:Number):Point {
        throw OverrideMe()
    }

    public function popupPricePos(priceWidth:Number, priceHeight:Number):Point {
        throw OverrideMe()
    }

    public function popupTitleTextY(height:Number):Number {
        throw OverrideMe();
    }
}
}
