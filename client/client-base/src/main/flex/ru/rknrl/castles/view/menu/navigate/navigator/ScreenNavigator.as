package ru.rknrl.castles.view.menu.navigate.navigator {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.geom.Point;
import flash.text.TextField;

import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.*;
import ru.rknrl.castles.view.menu.navigate.points.NavigationPoints;
import ru.rknrl.castles.view.utils.createTextField;

public class ScreenNavigator extends Sprite {
    private var locale:CastlesLocale;

    private var _holder:Sprite;

    protected function get holder():Sprite {
        return _holder;
    }

    private var _balanceTextField:TextField;

    protected function get balanceTextField():TextField {
        return _balanceTextField;
    }

    private var _title:DisplayObject;

    protected function get title():DisplayObject {
        return _title;
    }

    private var _screens:Vector.<Screen>;

    protected function get screens():Vector.<Screen> {
        return _screens;
    }

    private var _navigationPoints:NavigationPoints;

    protected function get navigationPoints():NavigationPoints {
        return _navigationPoints;
    }

    public function ScreenNavigator(screens:Vector.<Screen>, gold:int, layout:Layout, locale:CastlesLocale) {
        this._screens = screens;
        this.locale = locale;

        addChild(_holder = new Sprite());

        for each(var screen:DisplayObject in screens) {
            _holder.addChild(screen);
        }

        addChild(_balanceTextField = createTextField(Fonts.balance));
        this.gold = gold;

        addChild(_navigationPoints = new NavigationPoints(screens));

        _layout = layout;
        currentScreenIndex = 0;
        this.layout = layout;
    }

    public function set gold(value:int):void {
        _balanceTextField.text = locale.balance(value);
    }

    private var _layout:Layout;

    public function get layout():Layout {
        return _layout;
    }

    public function set layout(value:Layout):void {
        _layout = value;

        _navigationPoints.scaleX = _navigationPoints.scaleY = value.scale;
        _navigationPoints.x = (value.screenWidth - _navigationPoints.width) / 2;
        _navigationPoints.y = value.navigationPointsY;

        _balanceTextField.scaleX = _balanceTextField.scaleY = value.scale;
        const balancePos:Point = value.balance(_balanceTextField.width);
        _balanceTextField.x = balancePos.x;
        _balanceTextField.y = balancePos.y;

        for each(var screen:Screen in _screens) {
            screen.layout = value;
        }

        updateScreensPos();
    }

    private var _currentScreenIndex:int;

    protected function get currentScreenIndex():int {
        return _currentScreenIndex;
    }

    protected function set currentScreenIndex(index:int):void {
        _currentScreenIndex = index;

        for (var i:int = 0; i < _screens.length; i++) {
            _screens[i].visible = false;
        }

        updateScreensPos();

        navigationPoints.selected = _screens[index];

        if (_title) removeChild(_title);
        _title = _screens[index].titleContent;
        if (_title) addChild(_title);
    }

    private function updateScreensPos():void {
        const prevIndex:int = getNextIndex(_currentScreenIndex);
        const nextIndex:int = getPrevIndex(_currentScreenIndex);

        _screens[_currentScreenIndex].x = 0;
        _screens[_currentScreenIndex].visible = true;

        _screens[prevIndex].x = -_layout.screenWidth;
        _screens[prevIndex].visible = true;

        _screens[nextIndex].x = _layout.screenWidth;
        _screens[nextIndex].visible = true;
    }

    protected function getNextIndex(index:int):int {
        return index > 0 ? index - 1 : _screens.length - 1;
    }

    protected function getPrevIndex(index:int):int {
        return index < _screens.length - 1 ? index + 1 : 0;
    }

    public function set lock(value:Boolean):void {
        for each(var screen:Screen in screens) screen.lock = value;
    }
}
}
