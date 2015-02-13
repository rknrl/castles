//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view {
import flash.display.Sprite;

import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.loading.LoadingScreen;
import ru.rknrl.castles.view.loading.NoConnectionScreen;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.MenuView;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.loaders.ILoadImageManager;

public class View extends Sprite {
    private var locale:CastlesLocale;
    private var loadImageManager:ILoadImageManager;
    private var deviceFactory:DeviceFactory;

    public function View(layout:Layout, locale:CastlesLocale, loadImageManager:ILoadImageManager, deviceFactory:DeviceFactory) {
        _layout = layout;
        this.locale = locale;
        this.loadImageManager = loadImageManager;
        this.deviceFactory = deviceFactory;
    }

    private var _layout:Layout;

    public function set layout(value:Layout):void {
        _layout = value;
        if (menu) menu.layout = _layout;
        if (loadingScreen) loadingScreen.layout = _layout;
        if (game) game.layout = _layout;
    }

    private var menu:MenuView;

    public function addMenu(model:MenuModel):MenuView {
        if (menu) throw new Error("menu already exists");
        addChild(menu = new MenuView(_layout, locale, loadImageManager, model, deviceFactory));
        return menu;
    }

    public function showMenuAndLock():void {
        menu.visible = true;
        menu.lock = true;
    }

    public function hideMenu():void {
        menu.visible = false;
    }

    public function removeMenu():void {
        if (!menu) throw new Error("menu don't exists");
        removeChild(menu);
        menu = null;
    }

    private var game:GameView;

    public function addGame(playerInfos:Vector.<PlayerInfo>, h:int, v:int):GameView {
        if (game) throw new Error("game already exists");
        addChild(game = new GameView(playerInfos, h, v, _layout, locale, loadImageManager, deviceFactory));
        return game;
    }

    public function removeGame():void {
        if (!game) throw new Error("game don't exists");
        game.removeListeners();
        removeChild(game);
        game = null;
    }

    private var loadingScreen:LoadingScreen;

    private function _addLoadingScreen(text:String):void {
        if (loadingScreen) throw new Error("loadingScreen already exists");
        addChild(loadingScreen = new LoadingScreen(text, _layout));
    }

    private function _removeLoadingScreen():void {
        if (!loadingScreen) throw new Error("loadingScreen don't exists");
        removeChild(loadingScreen);
        loadingScreen = null;
    }

    public function addLoadingScreen():void {
        _addLoadingScreen(locale.loading);
    }

    public function removeLoadingScreen():void {
        _removeLoadingScreen();
    }

    public function removeLoadingScreenIfExists():void {
        if (loadingScreen) _removeLoadingScreen();
    }

    public function addSearchOpponentScreen():void {
        _addLoadingScreen(locale.searchOpponents);
    }

    public function removeSearchOpponentsScreen():void {
        _removeLoadingScreen();
    }

    public function addNoConnectionScreen():void {
        if (loadingScreen) throw new Error("loadingScreen already exists");
        addChild(loadingScreen = new NoConnectionScreen(locale.noConnection, _layout));
    }

    public function removeNoConnectionScreen():void {
        _removeLoadingScreen();
    }

    public function removeAnyLoadingScreen():void {
        _removeLoadingScreen();
    }
}
}
