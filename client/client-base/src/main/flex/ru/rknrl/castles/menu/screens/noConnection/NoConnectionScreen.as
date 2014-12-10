package ru.rknrl.castles.menu.screens.noConnection {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.menu.screens.*;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.funnyUi.buttons.RectButton;
import ru.rknrl.utils.centerize;
import ru.rknrl.utils.changeTextFormat;

public class NoConnectionScreen extends Screen {
    public static const TRY_CONNECT:String = "tryConnect";

    private var titleHolder:Sprite;
    private var title:Label;
    private var button:RectButton;

    public function NoConnectionScreen(layout:Layout, locale:CastlesLocale) {
        addChild(titleHolder = new Sprite());

        titleHolder.addChild(title = createTextField(layout.noConnectionTitleTextFormat, locale.noConnection));
        title.textColor = Colors.randomColor();

        addChild(button = layout.createRectButton(locale.tryConnect, Colors.randomColor()));
        button.addEventListener(MouseEvent.CLICK, onClick);

        updateLayout(layout);

        super();
    }

    public function updateLayout(layout:Layout):void {
        titleHolder.x = layout.noConnectionTitleCenterX;
        titleHolder.y = layout.noConnectionTitleCenterY;

        changeTextFormat(title, layout.noConnectionTitleTextFormat);
        centerize(title);

        layout.updateRectButton(button);
        button.x = layout.noConnectionButtonCenterX;
        button.y = layout.noConnectionButtonCenterY;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(TRY_CONNECT));
    }

    override protected function set inTransition(value:Number):void {
        titleHolder.scaleX = titleHolder.scaleY = value;
        button.scaleX = button.scaleY = value;
    }
}
}