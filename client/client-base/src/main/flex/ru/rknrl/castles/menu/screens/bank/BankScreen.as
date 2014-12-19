package ru.rknrl.castles.menu.screens.bank {
import flash.events.MouseEvent;

import ru.rknrl.castles.menu.screens.Screen;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.core.social.PaymentDialogData;
import ru.rknrl.core.social.PaymentDialogEvent;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.ProductDTO;
import ru.rknrl.funnyUi.buttons.RectButton;

public class BankScreen extends Screen {
    private var sender:AccountFacadeSender;
    private var social:Social;
    private var locale:CastlesLocale;
    private var product:ProductDTO;

    private var buyButton:RectButton;

    public function BankScreen(products:Products, sender:AccountFacadeSender, layout:Layout, social:Social, locale:CastlesLocale) {
        this.sender = sender;
        this.social = social;
        this.locale = locale;
        product = products.product;

        addChild(buyButton = layout.createRectButton(locale.buyButtonLabel(product.count, product.price), Colors.magenta));
        buyButton.addEventListener(MouseEvent.CLICK, onClick);

        updateLayout(layout);

        social.addEventListener(PaymentDialogEvent.PAYMENT_DIALOG_CLOSED, onPaymentDialogClosed);
        social.addEventListener(PaymentDialogEvent.PAYMENT_SUCCESS, onPaymentSuccess);
        social.addEventListener(PaymentDialogEvent.PAYMENT_FAIL, onPaymentFail);
    }

    public function updateLayout(layout:Layout):void {
        layout.updateRectButton(buyButton);
        buyButton.x = layout.bankButtonCenterX;
        buyButton.y = layout.bankButtonCenterY;
    }

    private function onClick(event:MouseEvent):void {
        social.showPaymentDialog(new PaymentDialogData(product.id, product.title, product.description, product.price));

        buyButton.lock = true;
        buyButton.playBounce();
    }

    private function onPaymentDialogClosed(event:PaymentDialogEvent):void {
        trace("payment closed");
        buyButton.lock = false;
    }

    private function onPaymentSuccess(event:PaymentDialogEvent):void {
        trace("payment success");
        buyButton.lock = false;
    }

    private function onPaymentFail(event:PaymentDialogEvent):void {
        trace("payment fail");
        buyButton.lock = false;
    }
}
}
