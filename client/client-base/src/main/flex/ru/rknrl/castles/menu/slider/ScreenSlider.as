package ru.rknrl.castles.menu.slider {
import flash.display.Sprite;

import ru.rknrl.castles.menu.screens.Screen;

public class ScreenSlider extends Sprite {
    protected var screens:Vector.<Screen>;

    public function ScreenSlider(screens:Vector.<Screen>) {
        this.screens = screens;
    }
}
}
