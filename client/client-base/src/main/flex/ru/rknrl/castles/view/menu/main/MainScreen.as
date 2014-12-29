package ru.rknrl.castles.view.menu.main {
import flash.display.DisplayObject;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.utils.createTextField;

public class MainScreen extends Screen {
    private static const startLocationScale:Number = 1.5;

    private var startLocationView:StartLocationView;
    private var playTextField:TextField;

    public function MainScreen(startLocation:StartLocation, layout:Layout, locale:CastlesLocale) {
        addChild(startLocationView = new StartLocationView(startLocation));

        playTextField = createTextField(Fonts.play);
        playTextField.text = locale.play;
        playTextField.addEventListener(MouseEvent.CLICK, onClick);
        this.layout = layout;
    }

    public function set startLocation(value:StartLocation):void {
        startLocationView.startLocation = value;
    }

    override public function set layout(value:Layout):void {
        startLocationView.scaleX = startLocationView.scaleY = value.scale * startLocationScale;
        startLocationView.x = value.screenCenterX;
        startLocationView.y = value.startLocationY;

        playTextField.scaleX = playTextField.scaleY = value.scale;
        playTextField.x = value.screenCenterX - playTextField.width / 2;
        playTextField.y = value.footerCenterY - playTextField.height / 2;
    }

    override public function get titleContent():DisplayObject {
        return playTextField;
    }

    override public function set lock(value:Boolean):void {
        startLocationView.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        dispatchEvent(new Event(ViewEvents.PLAY, true));
    }
}
}
