package ru.rknrl.castles.view.menu.main {
import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.Screen;
import ru.rknrl.castles.view.utils.createTextField;
import ru.rknrl.utils.centerize;

public class MainScreen extends Screen {
    private static const mouseHolderW:Number = 200;
    private static const mouseHolderH:Number = 64;

    private var startLocationView:StartLocationView;

    private var playHolder:Sprite;

    public function MainScreen(startLocation:StartLocation, layout:Layout, locale:CastlesLocale) {
        addChild(startLocationView = new StartLocationView(startLocation));

        addChild(playHolder = new Sprite());
        playHolder.addEventListener(MouseEvent.MOUSE_DOWN, onClick);

        const mouseHolder:Bitmap = new Bitmap(Colors.transparent);
        mouseHolder.width = mouseHolderW;
        mouseHolder.height = mouseHolderH;
        mouseHolder.x = -mouseHolderW / 2;
        mouseHolder.y = -mouseHolderH / 2;
        playHolder.addChild(mouseHolder);

        const playTextField:TextField = createTextField(Fonts.play);
        playTextField.text = locale.play;
        centerize(playTextField);
        playHolder.addChild(playTextField);

        this.layout = layout;
    }

    public function set startLocation(value:StartLocation):void {
        startLocationView.startLocation = value;
    }

    override public function set layout(value:Layout):void {
        startLocationView.scaleX = startLocationView.scaleY = value.scale * Layout.startLocationScale;
        startLocationView.x = value.startLocation.x;
        startLocationView.y = value.startLocation.y;

        playHolder.scaleX = playHolder.scaleY = value.scale;
        playHolder.x = value.screenCenterX;
        playHolder.y = value.footerCenterY;
    }

    override public function get titleContent():DisplayObject {
        return playHolder;
    }

    override public function set lock(value:Boolean):void {
        startLocationView.lock = value;
    }

    private function onClick(event:MouseEvent):void {
        event.stopImmediatePropagation();
        dispatchEvent(new Event(ViewEvents.PLAY, true));
    }
}
}
