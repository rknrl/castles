package ru.rknrl.castles.menu.slider {
import flash.display.Sprite;

import ru.rknrl.castles.menu.screens.MenuScreen;

public class ScreenSlider extends Sprite {
    protected var screens:Vector.<MenuScreen>;

    public function ScreenSlider(screens:Vector.<MenuScreen>) {
        this.screens = screens;
    }

    protected function getScreenById(id:String):MenuScreen {
        for each(var screen:MenuScreen in screens) {
            if (screen.id == id) return screen;
        }
        throw new Error("can't find screen " + id);
    }
}
}
