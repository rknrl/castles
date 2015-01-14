package ru.rknrl.castles.view.menu.bank {
import flash.display.DisplayObject;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.bank.Products;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;

public class BankScreen extends Screen {
    private var locale:CastlesLocale;

    private var shield:ShieldView;
    private var button:Button;

    public function BankScreen(products:Products, layout:Layout, locale:CastlesLocale) {
        this.locale = locale;

        if (layout.needShield) addChild(shield = new ShieldView());

        button = new Button(layout);
        button.addEventListener(MouseEvent.CLICK, onClick);

        this.products = products;
        this.layout = layout;
    }

    public function set products(value:Products):void {
        button.text = locale.bankButton(value.product.count, value.product.price);
    }

    override public function set layout(value:Layout):void {
        if (value.needShield) {
            shield.scaleX = shield.scaleY = value.scale;
            shield.x = value.screenCenterX;
            shield.y = value.contentCenterY;
        }

        button.layout = value;
        button.x = value.buttonX;
        button.y = value.buttonY;
    }

    override public function get titleContent():DisplayObject {
        return button;
    }

    override public function set transition(value:Number):void {
        button.scaleX = button.scaleY = value;
    }

    override public function set lock(value:Boolean):void {
        button.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        button.animate();
        dispatchEvent(new Event(ViewEvents.BUY, true));
    }
}
}
