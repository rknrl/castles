package ru.rknrl.castles.view.layout {

import flash.text.TextField;

import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.popups.popup.Popup;
import ru.rknrl.castles.view.popups.popup.PopupItem;
import ru.rknrl.castles.view.popups.popup.PopupLandscape;
import ru.rknrl.dto.CellSize;

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

    override public function get contentCenterY():Number {
        return titleTop + (screenHeight - titleTop - footerHeight) / 2;
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

    override public function buttonWidth(textWidth:Number):Number {
        const padding:int = 16 * scale;
        return textWidth + padding * 2;
    }

    override public function get buttonHeight():Number {
        return 64 * scale;
    }

    private function get balancePadding():Number {
        return 16 * scale;
    }

    override public function balance(width:int):Point {
        return new Point(screenWidth - width - balancePadding, balancePadding);
    }

    private function get titleTop():Number {
        const balanceHeight:Number = 24 * scale;
        const gap:Number = 8 * scale;
        return balancePadding + balanceHeight + gap;
    }

    override public function title(width:int, height:int):Point {
        return new Point(screenWidth - balancePadding - width, titleTop);
    }

    override public function gameAreaPos(areaH:int, areaV:int):Point {
        const width:Number = areaH * CellSize.SIZE.id() * scale;
        return new Point(screenCenterX - width / 2, 24 * scale);
    }

    override public function get notScaledGameAvatarSize():Number {
        return 128;
    }

    override public function get gameMagicItemsY():Number {
        return screenHeight - (96 + 24) * scale;
    }

    override public function gameAvatarPos(number:int, areaH:int, areaV:int):Point {
        const areaWidth:Number = areaH * CellSize.SIZE.id() * scale;
        const areaHeight:Number = areaV * CellSize.SIZE.id() * scale;

        const areaLeft:Number = gameAreaPos(areaH, areaV).x;
        const areaTop:Number = gameAreaPos(areaH, areaV).y;
        const areaRight:Number = areaLeft + areaWidth;
        const areaBottom:Number = areaTop + areaHeight;
        const paddingX:Number = 110 * scale;
        const paddingTop:Number = 24 * scale + gameAvatarSize / 2;
        const paddingBottom:Number = (gameAvatarTextHeight + gameAvatarTextGap) * scale + gameAvatarSize / 2;
        switch (number) {
            case 0:
                return new Point(areaLeft - paddingX, areaTop + paddingTop);
            case 1:
                return new Point(areaRight + paddingX, areaTop + paddingTop);
            case 2:
                return new Point(areaLeft - paddingX, areaBottom - paddingBottom);
            case 3:
                return new Point(areaRight + paddingX, areaBottom - paddingBottom);
        }
        throw new Error("invalid avatar number " + number);
    }

    override public function gameAvatarBitmapPos(number:int):Point {
        return new Point(0, 0);
    }

    private static const gameAvatarTextGap: Number = 8;

    override public function gameAvatarTextPos(number:int, width:Number, height:Number):Point {
        return new Point(-width / 2, notScaledGameAvatarSize / 2 + gameAvatarTextGap)
    }

    private static const gameAvatarTextWidth:int = 200;
    private static const gameAvatarTextHeight:int = 66;

    override public function createGameAvatarTextField():TextField {
        const textField:TextField = new TextField();
        textField.selectable = false;
        textField.embedFonts = true;
        textField.defaultTextFormat = Fonts.gameAvatarLandscape;
        textField.width = gameAvatarTextWidth;
        textField.wordWrap = true;
        textField.height = gameAvatarTextHeight;
        return textField;
    }

    override public function rewardText(textWidth:Number, textHeight:Number):Point {
        return balance(textWidth);
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
        if(itemsCount == 1) return 220 * scale;
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
