package ru.rknrl.castles.menu.slider {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.LayoutLandscape;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;
import ru.rknrl.utils.changeTextFormat;

public class ScreenSliderLandscape extends ScreenSlider {
    private var screensHolder:Sprite;
    private var buttonsHolder:Sprite;

    private var buttons:Vector.<MenuButton> = new <MenuButton>[];

    private function getButtonById(id:String):MenuButton {
        for each(var button:MenuButton in buttons) {
            if (button.id == id) return button;
        }
        throw new Error("can't find screen " + id);
    }

    public function ScreenSliderLandscape(screens:Vector.<MenuScreen>, layout:LayoutLandscape, locale:CastlesLocale) {
        super(screens);

        addChild(screensHolder = new Sprite());

        for each(var screen:MenuScreen in screens) {
            screensHolder.addChild(screen);
            screen.changeColors();
        }

        addChild(buttonsHolder = new Sprite());

        const ids:Vector.<String> = new <String>[Utils.BUTTON_PLAY];
        for each(var screen:MenuScreen in screens) {
            ids.push(screen.id);
        }

        for (var i:int = 0; i < ids.length; i++) {
            const button:MenuButton = new MenuButton(
                    ids[i],
                    layout.panelButtonsTextFormat,
                    locale.screenName(ids[i]),
                    Colors.randomColor()
            );
            button.addEventListener(MouseEvent.CLICK, onMenuButtonClick);
            buttons.push(button);
            buttonsHolder.addChild(button);
        }

        addEventListener(Event.ENTER_FRAME, onEnterFrame);

        currentScreen = getScreenById(Utils.SCREEN_CASTLE);

        updateLayout(layout);
    }

    private var layout:LayoutLandscape;

    public function updateLayout(layout:LayoutLandscape):void {
        this.layout = layout;

        for (var i:int = 0; i < buttons.length; i++) {
            const button:MenuButton = buttons[i];
            changeTextFormat(button, layout.panelButtonsTextFormat);
            button.x = layout.stageWidth - layout.gap - button.width;
            button.y = (button.height + layout.panelButtonsGap) * i;
        }

        buttonsHolder.y = layout.stageCenterY - buttonsHolder.height / 2;

        posScreens();
        updateScreenHolderPosition();
    }

    private function onMenuButtonClick(event:MouseEvent):void {
        const menuButton:MenuButton = MenuButton(event.target);

        if (menuButton.id == Utils.BUTTON_PLAY) {
            dispatchEvent(new Event(Utils.PLAY));
            return;
        }

        const newScreen:MenuScreen = getScreenById(menuButton.id);

        if (newScreen != currentScreen) {
            oldScreen = currentScreen;
            newScreen.changeColors();
            currentScreen = newScreen;
            startEaser();
        }
    }

    // current screen

    private var oldScreen:MenuScreen;

    private var _currentScreen:MenuScreen;

    public function get currentScreen():MenuScreen {
        return _currentScreen;
    }

    public function set currentScreen(value:MenuScreen):void {
        if (_currentScreen) {
            getButtonById(_currentScreen.id).selected = false;
        }
        _currentScreen = value;

        getButtonById(_currentScreen.id).selected = true;
    }

    // posScreens

    private function posScreens():void {
        for each(var screen:MenuScreen in screens) {
            screen.visible = false;
        }

        currentScreen.y = -layout.stageHeight;
        currentScreen.visible = true;

        if (oldScreen) {
            oldScreen.y = 0;
            oldScreen.visible = true;
        }
    }

    // screenHolder easer

    private static const easer:IEaser = new Linear(0, 1);
    private static const duration:int = 500;

    private var easerStartTime:int;

    private function startEaser():void {
        posScreens();
        easerStartTime = getTimer();
        updateScreenHolderPosition();
    }

    private function onEnterFrame(event:Event):void {
        if (getTimer() <= easerStartTime + duration) {
            updateScreenHolderPosition();
        }
    }

    private function updateScreenHolderPosition():void {
        screensHolder.y = interpolate(0, layout.stageHeight, getTimer(), easerStartTime, duration, easer);

        var p:Number = Math.abs(screensHolder.y / layout.stageHeight);
        currentScreen.transition = p;
        if (oldScreen) oldScreen.transition = 1 - p;
    }
}
}
