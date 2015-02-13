//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.menu.navigate.navigator {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.model.events.ScreenChangedEvent;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.navigate.*;
import ru.rknrl.castles.view.menu.navigate.points.NavigationPoints;
import ru.rknrl.castles.view.utils.AnimatedTextField;
import ru.rknrl.utils.OverrideMe;

public class ScreenNavigator extends Sprite {
    private var locale:CastlesLocale;

    private var _holder:Sprite;

    protected function get holder():Sprite {
        return _holder;
    }

    private var _balanceTextField:AnimatedTextField;

    protected function get balanceTextField():AnimatedTextField {
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
        _layout = layout;

        addChild(_holder = new Sprite());

        for each(var screen:DisplayObject in screens) {
            _holder.addChild(screen);
        }

        addChild(_balanceTextField = new AnimatedTextField(Fonts.balance));
        this.gold = gold;

        addChild(_navigationPoints = new NavigationPoints(screens));

        currentScreenIndex = 0;
        this.layout = layout;
    }

    public function set gold(value:int):void {
        const newText:String = locale.balance(value);
        if (_balanceTextField.text != newText) {
            _balanceTextField.text = newText;
            alignBalance();
            _balanceTextField.bounce();
        }
    }

    public function animatePrice():void {
        screens[currentScreenIndex].animatePrice();
        _balanceTextField.elastic();
    }

    private var _layout:Layout;

    public function get layout():Layout {
        return _layout;
    }

    public function set layout(value:Layout):void {
        _layout = value;

        _navigationPoints.scaleX = _navigationPoints.scaleY = layout.scale * navigationPointsScale;
        _navigationPoints.x = layout.screenCenterX - _navigationPoints.pointsWidth / 2;
        _navigationPoints.y = layout.navigationPointsY;

        alignBalance();

        for each(var screen:Screen in _screens) {
            screen.layout = value;
        }

        updateScreensPos();
    }

    protected function get navigationPointsScale():Number {
        throw OverrideMe;
    }

    private function alignBalance():void {
        _balanceTextField.textScale = _layout.scale;
        const balancePos:Point = _layout.balance(_balanceTextField.textWidth);
        _balanceTextField.x = balancePos.x + _balanceTextField.textWidth / 2;
        _balanceTextField.y = balancePos.y + _balanceTextField.textHeight / 2;
    }

    private var _currentScreenIndex:int;

    public function get currentScreenIndex():int {
        return _currentScreenIndex;
    }

    public function set currentScreenIndex(index:int):void {
        const changed:Boolean = _currentScreenIndex != index;

        _currentScreenIndex = index;

        for (var i:int = 0; i < _screens.length; i++) {
            _screens[i].visible = false;
        }

        updateScreensPos();

        navigationPoints.selected = _screens[index];

        if (_title) removeChild(_title);
        _title = _screens[index].titleContent;
        if (_title) addChild(_title);

        if (changed) dispatchEvent(new ScreenChangedEvent(index));
    }

    protected function updateScreensPos():void {
        throw OverrideMe();
    }

    public function set lock(value:Boolean):void {
        for each(var screen:Screen in screens) screen.lock = value;
    }
}
}
