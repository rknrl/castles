package ru.rknrl.castles.tools {
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.utils.Dictionary;

import ru.rknrl.castles.castlesTest;
import ru.rknrl.castles.controller.mock.DtoMock;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.MenuView;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.dto.PlayerInfoDTO;
import ru.rknrl.dto.SlotId;

public class CreateScreenshots extends Sprite {
    private var html:String;

    public function CreateScreenshots() {
        html = "<html>";
        html += "<head>";
        html += "<style>";
        html += "  body {";
        html += "    background-color: #707070;";
        html += "    color: white;";
        html += "    font-family: Helvetica, Arial, sans-serif;";
        html += "  }";
        html += "  img {";
        html += "    padding: 10px;";
        html += "  }";
        html += "</style>";

        html += "</head>";
        html += "<body>";

        const layouts:Dictionary = new Dictionary();
        layouts["iPhone3G"] = new LayoutPortrait(320, 480, 1);
        layouts["iPhone4"] = new LayoutPortrait(640, 960, 1);
        layouts["iPhone5"] = new LayoutPortrait(640, 1136, 1);
        layouts["iPad"] = new LayoutLandscape(1024, 768, 1);
        layouts["iPad2"] = new LayoutLandscape(2048, 1536, 1);

        const locale:CastlesLocale = new CastlesLocale("");
        for (var name:String in layouts) {
            html += "<p>" + name + "</p>";
            render(name + "/", layouts[name], locale);
        }

        html += "</body>";
        html += "</html>";

        const fileStream:FileStream = new FileStream();
        fileStream.open(new File(File.applicationDirectory.nativePath + "/html.html"), FileMode.WRITE);
        fileStream.writeUTFBytes(html);
        fileStream.close();
    }

    private function render(name:String, layout:Layout, locale:CastlesLocale):void {
        use namespace castlesTest;

        const loadImageManager:LoadImageManager = new LoadImageManager();
        const bg:BitmapData = new BitmapData(layout.screenWidth, layout.screenHeight, false, 0xffffff);

        const view:View = new View(layout, locale, loadImageManager);
        addChild(view);

        view.addLoadingScreen();
        screenshot(name + "loading", bg);

        view.removeLoadingScreen();
        const menuView:MenuView = view.addMenu(new MenuModel(DtoMock.authenticationSuccess()));
        menuView.setScreen(0);
        const mainBg:BitmapData = screenshot(name + "main", bg);

        menuView.openBuildPopup(SlotId.SLOT_1, 4);
        menuView.openPopupImmediate();
        screenshot(name + "build", mainBg);

        menuView.closePopup();
        menuView.closePopupImmediate();

        menuView.openUpgradePopup(SlotId.SLOT_1, true, true, 16);
        menuView.openPopupImmediate();
        screenshot(name + "upgrade", mainBg);

        menuView.closePopup();
        menuView.closePopupImmediate();

        menuView.setScreen(1);
        screenshot(name + "top", bg);

        menuView.setScreen(2);
        screenshot(name + "shop", bg);

        menuView.setScreen(3);
        screenshot(name + "skills", bg);

        menuView.setScreen(4);
        screenshot(name + "bank", bg);

        view.hideMenu();
        view.addSearchOpponentScreen();
        screenshot(name + "searchOpponents", bg);

        const w:int = layout is LayoutPortrait ? 8 : 15;
        const h:int = layout is LayoutPortrait ? 11 : 15;
        const playerInfos:Vector.<PlayerInfoDTO> = layout is LayoutPortrait ? DtoMock.playerInfosPortrait() : DtoMock.playerInfosLandscape();
        const losers:Vector.<PlayerInfoDTO> = layout is LayoutPortrait ? DtoMock.losersPortrait() : DtoMock.losersLandscape();

        view.removeSearchOpponentsScreen();
        const gameView:GameView = view.addGame(playerInfos, w, h);
        screenshot(name + "game", bg);

        gameView.openGameOverScreen(DtoMock.winner(), losers, true, 2);
        screenshot(name + "gameOver", bg);

        view.removeGame();
        view.addNoConnectionScreen();
        screenshot(name + "noConnection", bg);

        removeChild(view);

        function screenshot(name:String, bg:BitmapData):BitmapData {
            const width: int = layout is LayoutPortrait ? 160 : 512;
            html += '<img src = "' + name + '.png" width="'+width+'px"/>';
            return savePng(name, view, bg);
        }
    }
}
}
