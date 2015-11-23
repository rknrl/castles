//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game {
import flash.display.Bitmap;
import flash.display.Sprite;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.game.area.buildings.BuildingView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.loading.LoadingScreen;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.AnimatedTextField;
import ru.rknrl.core.points.Point;
import protos.BuildingLevel;
import protos.BuildingType;

public class GameSplashView extends Sprite {
    private var area:Sprite;
    private var loadingScreen:LoadingScreen;

    private var _tower1:BuildingView;

    public function get tower1():BuildingView {
        return _tower1;
    }

    private var _tower2:BuildingView;

    public function get tower2():BuildingView {
        return _tower2;
    }

    private var mouseHolder:Bitmap;
    private var textField:AnimatedTextField;

    private var _tutor:GameSplashTutorialView;

    public function get tutor():GameSplashTutorialView {
        return _tutor;
    }

    private var _arrows:ArrowsView;

    public function get arrows():ArrowsView {
        return _arrows;
    }

    private var _units:Sprite;

    public function get units():Sprite {
        return _units;
    }

    private var locale:CastlesLocale;

    public function GameSplashView(layout:Layout, locale:CastlesLocale, deviceFactory:DeviceFactory) {
        mouseChildren = false;
        this.locale = locale;

        addChild(area = new Sprite());

        area.addChild(mouseHolder = new Bitmap(Colors.transparent));
        area.addChild(_units = new Sprite());
        area.addChild(_tower1 = new BuildingView(DtoMock.buildingId(0), BuildingType.TOWER, BuildingLevel.LEVEL_3, new BuildingOwner(true, DtoMock.playerId(0)), 8, false, new Point(0, 0)));
        area.addChild(_tower2 = new BuildingView(DtoMock.buildingId(0), BuildingType.TOWER, BuildingLevel.LEVEL_3, new BuildingOwner(false), 3, false, new Point(0, 0)));

        area.addChild(_arrows = new ArrowsView());
        arrows.transform.colorTransform = Colors.transform(Colors.yellow);

        area.addChild(_tutor = new GameSplashTutorialView(layout, locale, deviceFactory));

        area.addChild(textField = new AnimatedTextField(Fonts.title));
        textField.text = locale.gameSplash;
        this.layout = layout;
    }

    private var _layout:Layout;

    public function get tower1Pos():Point {
        return new Point(_layout.screenCenterX - 100 * _layout.scale, _layout.screenHeight / 2)
    }

    public function get tower2Pos():Point {
        return new Point(_layout.screenCenterX + 100 * _layout.scale, _layout.screenHeight / 2)
    }

    public function set layout(value:Layout):void {
        _layout = value;

        mouseHolder.width = value.screenWidth;
        mouseHolder.height = value.screenHeight;

        _tower1.x = tower1Pos.x;
        _tower1.y = tower1Pos.y;
        _tower2.x = tower2Pos.x;
        _tower2.y = tower2Pos.y;

        textField.textScale = value.scale;
        textField.x = value.screenCenterX;
        textField.y = _layout.footerCenterY;

        _tutor.layout = value;
        if (loadingScreen) loadingScreen.layout = value;
    }

    public function addLoadingScreen():void {
        removeChild(area);
        addChild(loadingScreen = new LoadingScreen(locale.enterFirstGame, _layout));
    }
}
}
