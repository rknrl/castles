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
import flash.text.TextField;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.game.area.buildings.BuildingView;
import ru.rknrl.castles.view.game.area.units.UnitsView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.loading.LoadingScreen;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.utils.createTextField;

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
    private var titleTextField:TextField;
    private var textField:TextField;
    private var tutor:GameSplashTutorialView;

    private var _arrows:ArrowsView;

    public function get arrows():ArrowsView {
        return _arrows;
    }

    private var _units:UnitsView;

    public function get units():UnitsView {
        return _units;
    }

    public function GameSplashView(layout:Layout, locale:CastlesLocale, deviceFactory:DeviceFactory) {
        mouseChildren = false;

        addChild(area = new Sprite());

        area.addChild(mouseHolder = new Bitmap(Colors.transparent));
        area.addChild(_units = new UnitsView());
        area.addChild(_tower1 = new BuildingView(DtoMock.buildingId(0), BuildingType.TOWER, BuildingLevel.LEVEL_3, new BuildingOwner(true, DtoMock.playerId(0)), 8, false, new Point(0, 0)));
        area.addChild(_tower2 = new BuildingView(DtoMock.buildingId(0), BuildingType.TOWER, BuildingLevel.LEVEL_3, new BuildingOwner(false), 3, false, new Point(0, 0)));

        area.addChild(_arrows = new ArrowsView());
        arrows.transform.colorTransform = Colors.transform(Colors.yellow);

        area.addChild(tutor = new GameSplashTutorialView(layout, locale, deviceFactory));

        area.addChild(titleTextField = createTextField(Fonts.gameSplashText));
        titleTextField.text = "Захвати башню!";
        area.addChild(textField = createTextField(Fonts.gameSplashText));
        textField.text = "Нажимай мышкой на желтую башню и не отпуская тащи на другую";
        this.layout = layout;
    }

    private var _layout:Layout;

    public function get tower1Pos():Point {
        return new Point(_layout.screenCenterX - 100 * _layout.scale, _layout.contentCenterY)
    }

    public function get tower2Pos():Point {
        return new Point(_layout.screenCenterX + 100 * _layout.scale, _layout.contentCenterY)
    }

    public function set layout(value:Layout):void {
        _layout = value;

        mouseHolder.width = value.screenWidth;
        mouseHolder.height = value.screenHeight;

        _tower1.x = tower1Pos.x;
        _tower1.y = tower1Pos.y;
        _tower2.x = tower2Pos.x;
        _tower2.y = tower2Pos.y;

        const textHeight:Number = 24 * value.scale;
        const gap:Number = 8 * value.scale;
        titleTextField.scaleX = titleTextField.scaleY = value.scale;
        titleTextField.x = value.screenCenterX - titleTextField.width / 2;
        titleTextField.y = _layout.footerCenterY - textHeight - gap / 2;

        textField.scaleX = textField.scaleY = value.scale;
        textField.x = value.screenCenterX - textField.width / 2;
        textField.y = _layout.footerCenterY + gap / 2;

        tutor.layout = value;
        if (loadingScreen) loadingScreen.layout = value;
    }

    public function playArrow():void {
        tutor.playArrow(tower1Pos, tower2Pos);
    }

    public function addLoadingScreen():void {
        removeChild(area);
        addChild(loadingScreen = new LoadingScreen("Отлично\nЗаходим в бой..", _layout));
    }
}
}
