package ru.rknrl.castles.menu.screens.bank {
import flash.events.MouseEvent;

import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.core.social.PaymentDialogData;
import ru.rknrl.core.social.PaymentDialogEvent;
import ru.rknrl.core.social.Social;
import ru.rknrl.funnyUi.buttons.RectButton;
import ru.rknrl.castles.rmi.AccountFacadeSender;

public class BankScreen extends MenuScreen {
    private var sender:AccountFacadeSender;
    private var social:Social;
    private var locale:CastlesLocale;

    private var fastAndTrust:Circle;
    private var saleCircle:Circle;
    private var buyButton:RectButton;

    public function BankScreen(id:String, goldByDollar:int, sender:AccountFacadeSender, layout:Layout, social:Social, locale:CastlesLocale) {
        this.sender = sender;
        this.social = social;
        this.locale = locale;

        addChild(fastAndTrust = new Circle(locale.fastAndTrust, layout.bankCircleTextFormat, layout.fastAndTrustCircleRadius, Colors.randomLightColor()));
        addChild(saleCircle = new Circle(locale.sale, layout.bankCircleTextFormat, layout.saleCircleRadius, Colors.randomLightColor()));

        addChild(buyButton = layout.createRectButton(locale.buyButtonLabel(goldByDollar), Colors.randomColor()));
        buyButton.addEventListener(MouseEvent.CLICK, onClick);

        updateLayout(layout);

        social.addEventListener(PaymentDialogEvent.PAYMENT_DIALOG_CLOSED, onPaymentDialogClosed);
        social.addEventListener(PaymentDialogEvent.PAYMENT_SUCCESS, onPaymentSuccess);
        social.addEventListener(PaymentDialogEvent.PAYMENT_FAIL, onPaymentFail);

        super(id);
    }

    private var layout:Layout;

    public function updateLayout(layout:Layout):void {
        this.layout = layout;

        layout.updateRectButton(buyButton);
        buyButton.x = layout.bankButtonCenterX;
        buyButton.y = layout.bankButtonCenterY;

        saleCircle.updateLayout(layout.bankCircleTextFormat, layout.saleCircleRadius);
        saleCircle.x = layout.saleCircleX;

        fastAndTrust.updateLayout(layout.bankCircleTextFormat, layout.fastAndTrustCircleRadius);
        fastAndTrust.x = layout.fastAndTrustCircleX;

        updateTransition(_transition);
    }

    private var _transition:Number = 0;

    override public function set transition(value:Number):void {
        _transition = value;
        updateTransition(_transition);
    }

    private function updateTransition(value:Number):void {
        saleCircle.y = layout.saleCircleY + (layout.stageHeight - layout.saleCircleY) * (1 - value);
        fastAndTrust.y = layout.fastAndTrustCircleY + (layout.stageHeight - layout.fastAndTrustCircleY) * (1 - value);
    }

    public function set goldByDollar(value:int):void {
        buyButton.text = locale.buyButtonLabel(value);
    }

    private function onClick(event:MouseEvent):void {
        social.showPaymentDialog(new PaymentDialogData(1, "Звездочки", "Description", 100));

        sender.buyGold();
        buyButton.lock();
        buyButton.playBounce();
    }

    override public function changeColors():void {
        buyButton.color = Colors.randomColor();
        fastAndTrust.color = Colors.randomLightColor();
        saleCircle.color = Colors.randomLightColor();
    }

    private function onPaymentDialogClosed(event:PaymentDialogEvent):void {
        trace("payment closed");
        buyButton.unlock();
    }

    private function onPaymentSuccess(event:PaymentDialogEvent):void {
        trace("payment success");
        buyButton.unlock();
    }

    private function onPaymentFail(event:PaymentDialogEvent):void {
        trace("payment fail");
        buyButton.unlock();
    }
}
}
