package ru.rknrl.castles.view.layout {
import flash.geom.Point;

import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.castles.view.popups.popup.PopupItem;
import ru.rknrl.castles.view.popups.popup.PopupLandscape;

public class LayoutLandscape extends Layout {
    public static const iPadWidth:int = 1024;
    public static const iPadHeight:int = 768;

    public function LayoutLandscape(stageWidth:int, stageHeight:int, contentsScaleFactor:Number) {
        const scale:Number = getScale(stageWidth, stageHeight, iPadWidth, iPadHeight);
        super(stageWidth, stageHeight, scale, contentsScaleFactor);
    }

    override public function get footerHeight():Number {
        return 124 * scale;
    }

    override public function get needShield():Boolean {
        return false;
    }

    override public function get buttonX():Number {
        return screenCenterX;
    }

    override public function get buttonY():Number {
        return contentCenterY;
    }

    override public function get buttonWidth():Number {
        return 220 * scale;
    }

    override public function get buttonHeight():Number {
        return 64 * scale;
    }

    override public function balance(width:int):Point {
        const padding:Number = 16 * scale;
        return new Point(screenWidth - width - padding, padding);
    }

    override public function title(width:int, height:int):Point {
        const padding:Number = 16 * scale;
        const balanceHeight:Number = 24 * scale;
        const gap:Number = 8 * scale;
        return new Point(screenWidth - padding - width, padding + balanceHeight + gap);
    }

    override public function get gameAvatarSize():Number {
        throw 128 * scale;
    }

    // popup

    override public function createPopup(titleText:String, cancelText:String, items:Vector.<PopupItem>, layout:Layout):Popup {
        return new PopupLandscape(titleText, items, layout);
    }

    override public function get popupPadding():Number {
        return 16 * scale;
    }

    override public function get popupItemSize():Number {
        return 128 * scale;
    }

    override public function get popupTitleHeight():Number {
        return 44 * scale;
    }

    override public function popupWidth(itemsCount:int):Number {
        const itemsWidth:Number = (popupItemSize + popupPadding) * itemsCount - popupPadding;
        return itemsWidth + popupPadding * 2;
    }

    override public function get popupIconPos():Point {
        return new Point(popupItemSize / 2, 48 * scale);
    }

    override public function popupTextPos(textWidth:Number, textHeight:Number):Point {
        return new Point((popupItemSize - textWidth) / 2, 64 * scale);
    }

    override public function popupPricePos(priceWidth:Number, priceHeight:Number):Point {
        return new Point((popupItemSize - priceWidth) / 2, 96 * scale);
    }

    override public function popupTitleTextY(height:Number):Number {
        return 0;
    }
}
}
