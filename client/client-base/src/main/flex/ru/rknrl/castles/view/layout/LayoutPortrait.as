package ru.rknrl.castles.view.layout {

import flash.text.TextField;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.castles.view.popups.popup.PopupItem;
import ru.rknrl.castles.view.popups.popup.PopupPortrait;
import ru.rknrl.castles.view.utils.createTextField;

public class LayoutPortrait extends Layout {
    public static const iPhone5Width:int = 320;
    public static const iPhone5Height:int = 568;

    public function LayoutPortrait(stageWidth:int, stageHeight:int, contentsScaleFactor:Number) {
        const scale:Number = getScale(stageWidth, stageHeight, iPhone5Width, iPhone5Height);
        super(stageWidth, stageHeight, scale, contentsScaleFactor);
        _footerHeight = 96 * scale;
    }

    private var _footerHeight:Number;

    override public function get footerHeight():Number {
        return _footerHeight;
    }

    override public function balance(width:int):Point {
        const padding:Number = 8 * scale;
        return new Point(screenWidth - width - padding, padding);
    }

    override public function title(width:int, height:int):Point {
        const padding:Number = 16 * scale;
        return new Point(screenCenterX - width / 2, screenHeight - padding - height);
    }

    override public function get needShield():Boolean {
        return true;
    }

    override public function get buttonX():Number {
        return screenCenterX;
    }

    override public function get buttonY():Number {
        return footerCenterY;
    }

    override public function get buttonWidth():Number {
        const padding:int = 8 * scale;
        return screenWidth - padding * 2;
    }

    override public function get buttonHeight():Number {
        const padding:int = 8 * scale;
        return footerHeight - padding * 2;
    }

    override public function gameAreaPos(width:Number, height:Number):Point {
        return new Point(screenCenterX - width / 2, 40 * scale);
    }

    override public function get notScaledGameAvatarSize():Number {
        return 32;
    }

    override public function gameAvatarBitmapPos(number:int):Point {
        switch (number) {
            case 0:
                return new Point(0, 0);
            case 1:
                return new Point(0, 0);
        }
        throw new Error("invalid avatar number " + number);
    }

    override public function gameAvatarTextPos(number:int, width:Number, height:Number):Point {
        const gap:Number = 4;
        const y:Number = -height / 2;
        switch (number) {
            case 0:
                return new Point(-notScaledGameAvatarSize / 2 - gap - width, y);
            case 1:
                return new Point(notScaledGameAvatarSize / 2 + gap, y);
        }
        throw new Error("invalid avatar number " + number);
    }

    override public function gameAvatarPos(number:int, areaWidth:Number, areaHeight:Number):Point {
        const areaLeft:Number = gameAreaPos(areaWidth, areaHeight).x;
        const areaTop:Number = gameAreaPos(areaWidth, areaHeight).y;
        const areaRight:Number = areaLeft + areaWidth;
        const areaBottom:Number = areaTop + areaHeight;

        switch (number) {
            case 0:
                return new Point(areaRight - gameAvatarSize / 2, areaTop - 4 - gameAvatarSize / 2);
            case 1:
                return new Point(areaLeft + gameAvatarSize / 2, areaBottom + 4 + gameAvatarSize / 2);
        }
        throw new Error("invalid avatar number " + number);
    }

    override public function createGameAvatarTextField():TextField {
        return createTextField(Fonts.gameAvatarPortrait);
    }

    override public function get gameMagicItemsY():Number {
        return screenHeight - 4 * scale - itemSize / 2 * scale;
    }

    // popup

    override public function createPopup(titleText:String, cancelText:String, items:Vector.<PopupItem>, layout:Layout):Popup {
        return new PopupPortrait(titleText, cancelText, items, layout);
    }

    override public function get popupPadding():Number {
        return 8 * scale;
    }

    override public function get popupItemSize():Number {
        return 88 * scale;
    }

    override public function get popupCancelHeight():Number {
        return 44 * scale;
    }

    override public function get popupTitleHeight():Number {
        return 44 * scale;
    }

    override public function popupWidth(itemsCount:int):Number {
        return popupWidthImpl;
    }

    private function get popupWidthImpl():Number {
        return screenWidth - popupPadding * 2;
    }

    override public function get popupCancelButtonGap():Number {
        return 8 * scale;
    }

    override public function get popupIconPos():Point {
        return new Point(48 * scale, 64 * scale);
    }

    override public function popupTextPos(textWidth:Number, textHeight:Number):Point {
        return new Point(88 * scale, (popupItemSize - textHeight) / 2);
    }

    override public function popupPricePos(priceWidth:Number, priceHeight:Number):Point {
        const gap:Number = 16 * scale;
        return new Point(popupWidthImpl - gap - priceWidth, (popupItemSize - priceHeight) / 2);
    }

    override public function popupTitleTextY(height:Number):Number {
        return (popupTitleHeight - height) / 2;
    }
}
}
