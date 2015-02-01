package ru.rknrl.castles.tools {
import flash.display.BitmapData;
import flash.display.Sprite;
import flash.events.Event;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.geom.ColorTransform;
import flash.geom.Matrix;
import flash.utils.Dictionary;

import ru.rknrl.castles.controller.mock.DtoMock;
import ru.rknrl.castles.controller.mock.LoadImageManagerMock;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.MenuView;
import ru.rknrl.castles.view.menu.factory.MobileFactory;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.PlayerInfoDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.SlotsPosDTO;
import ru.rknrl.loaders.BitmapLoader;
import ru.rknrl.loaders.ILoader;
import ru.rknrl.loaders.ParallelLoader;
import ru.rknrl.loaders.TextLoader;
import ru.rknrl.test;

public class ViewReport extends Sprite {
    private static function htmlBegin():String {
        var html:String;
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
        html += "<p>" + new Date().toDateString() + "</p>";
        return html;
    }

    private static function htmlEnd():String {
        var html:String;
        html = "</body>";
        html += "</html>";
        return html;
    }

    public static const devices:Vector.<String> = new <String>[
        "iPad2",
        "iPhone5"
    ];

    private static const names:Vector.<String> = new <String>[
        "BankScreen",
        "BuildPopup",
        "GameOverScreen",
        "GameScreen",
        "LoadingScreen",
        "MagicItemsScreen",
        "MainScreen",
        "NoConnectionScreen",
        "SearchOpponentsScreen",
        "SkillsScreen",
        "TopScreen",
        "Upgrade1Popup",
        "UpgradePopup"
    ];

    private static const layouts:Dictionary = new Dictionary();
    layouts["iPhone3G"] = new LayoutPortrait(320, 480, 1);
    layouts["iPhone4"] = new LayoutPortrait(640, 960, 1);
    layouts["iPhone5"] = new LayoutPortrait(640, 1136, 1);
    layouts["iPad"] = new LayoutLandscape(1024, 768, 1);
    layouts["iPad2"] = new LayoutLandscape(2048, 1536, 1);

    private var localeLoader:TextLoader;

    public function ViewReport() {
        const loaders:Vector.<ILoader> = new <ILoader>[];
        for each(var device:String in devices) {
            for each(var name:String in names) {
                loaders.push(new BitmapLoader("reference/" + device + "/" + name + ".png"))
            }
        }
        loaders.push(localeLoader = new TextLoader("castles - RU.tsv"));
        const loader:ParallelLoader = new ParallelLoader(loaders);
        loader.addEventListener(Event.COMPLETE, onComplete);
        loader.load();
    }

    private var references:Dictionary;
    private var loadImageManager:LoadImageManagerMock;

    private function onComplete(event:Event):void {
        references = ParallelLoader(event.target).data;

        loadImageManager = new LoadImageManagerMock(0);
        loadImageManager.addEventListener(Event.COMPLETE, onLoadImageManagerComplete);
    }

    private function onLoadImageManagerComplete(event:Event):void {
        createViewReport();
        createViewMergers();
        trace("complete");
    }

    private var html:String;

    private function createViewReport():void {
        html = htmlBegin();
        const locale:CastlesLocale = new CastlesLocale(localeLoader.text);
        for (var name:String in layouts) {
            html += "<p>" + name + "</p>";
            render("report/", name + "/", layouts[name], locale, false);
        }
        html += htmlEnd();

        const fileStream:FileStream = new FileStream();
        fileStream.open(new File(File.applicationDirectory.nativePath + "/report/report.html"), FileMode.WRITE);
        fileStream.writeUTFBytes(html);
        fileStream.close();
    }

    private function createViewMergers():void {
        html = htmlBegin();
        const locale:CastlesLocale = new CastlesLocale(localeLoader.text);
        render("merge/", "iPhone5/", layouts["iPhone5"], locale, true);
        render("merge/", "iPad2/", layouts["iPad2"], locale, true);
        html += htmlEnd();

        const fileStream:FileStream = new FileStream();
        fileStream.open(new File(File.applicationDirectory.nativePath + "/merge/merge.html"), FileMode.WRITE);
        fileStream.writeUTFBytes(html);
        fileStream.close();
    }

    private function render(folder:String, device:String, layout:Layout, locale:CastlesLocale, merge:Boolean):void {
        use namespace test;

        const bg:BitmapData = new BitmapData(layout.screenWidth, layout.screenHeight, false, 0xffffff);
        const view:View = new View(layout, locale, loadImageManager, new MobileFactory());
        addChild(view);

        view.addLoadingScreen();
        screenshot(folder, device, "LoadingScreen", bg);

        view.removeLoadingScreen();
        const menuView:MenuView = view.addMenu(new MenuModel(DtoMock.authenticationSuccess()));
        menuView.setScreen(0);
        const mainBg:BitmapData = screenshot(folder, device, "MainScreen", bg);

        menuView.openBuildPopup(SlotId.SLOT_1, 4);
        menuView.openPopupImmediate();
        screenshot(folder, device, "BuildPopup", mainBg);

        menuView.closePopup();
        menuView.closePopupImmediate();

        menuView.openUpgradePopup(SlotId.SLOT_1, BuildingType.CHURCH, true, true, 16);
        menuView.openPopupImmediate();
        screenshot(folder, device, "UpgradePopup", mainBg);

        menuView.closePopup();
        menuView.closePopupImmediate();

        menuView.openUpgradePopup(SlotId.SLOT_1, BuildingType.CHURCH, true, false, 16);
        menuView.openPopupImmediate();
        screenshot(folder, device, "Upgrade1Popup", mainBg);

        menuView.closePopup();
        menuView.closePopupImmediate();

        menuView.setScreen(1);
        screenshot(folder, device, "TopScreen", bg);

        menuView.setScreen(2);
        screenshot(folder, device, "MagicItemsScreen", bg);

        menuView.setScreen(3);
        screenshot(folder, device, "SkillsScreen", bg);

        menuView.setScreen(4);
        screenshot(folder, device, "BankScreen", bg);

        view.hideMenu();
        view.addSearchOpponentScreen();
        screenshot(folder, device, "SearchOpponentsScreen", bg);

        const w:int = layout is LayoutPortrait ? 8 : 15;
        const h:int = layout is LayoutPortrait ? 11 : 15;
        const playerInfos:Vector.<PlayerInfoDTO> = layout is LayoutPortrait ? DtoMock.playerInfosPortrait() : DtoMock.playerInfosLandscape();
        const losers:Vector.<PlayerInfoDTO> = layout is LayoutPortrait ? DtoMock.losersPortrait() : DtoMock.losersLandscape();

        view.removeSearchOpponentsScreen();
        const gameView:GameView = view.addGame(PlayerInfo.fromDtoVector(playerInfos), w, h);
        const buildings:Vector.<BuildingDTO> = layout is LayoutPortrait ? DtoMock.buildingsPortrait() : DtoMock.buildingsLandscape();
        for each(var b:BuildingDTO in  buildings) {
            gameView.area.addBuilding(b.id, b.building.type, b.building.level, new BuildingOwner(b.hasOwner, b.owner), b.population, b.strengthened, new Point(b.pos.x, b.pos.y));
        }
        const slotsPos:Vector.<SlotsPosDTO> = layout is LayoutPortrait ? DtoMock.slotsPosPortrait() : DtoMock.slotsPosLandscape();
        for each(var s:SlotsPosDTO in slotsPos) {
            gameView.area.addSlots(s);
        }

        screenshot(folder, device, "GameScreen", bg);

        gameView.openGameOverScreen(new <PlayerInfo>[PlayerInfo.fromDto(DtoMock.winner())], PlayerInfo.fromDtoVector(losers), true, 2);
        screenshot(folder, device, "GameOverScreen", bg);

        view.removeGame();
        view.addNoConnectionScreen();
        screenshot(folder, device, "NoConnectionScreen", bg);

        removeChild(view);

        function screenshot(folder:String, device:String, name:String, bg:BitmapData):BitmapData {
            const bitmapData:BitmapData = bg.clone();
            bitmapData.draw(view);

            if (merge) {
                const referenceBitmapData:BitmapData = references["reference/" + device + name + ".png"];
                const matrix:Matrix = new Matrix();
                matrix.scale(2, 2);
                const colorTransform:ColorTransform = new ColorTransform();
                colorTransform.alphaMultiplier = 0.5;
                bitmapData.draw(referenceBitmapData, matrix, colorTransform);
            }

            const width:int = layout is LayoutPortrait ? 160 : 512;
            html += '<img src = "' + device + name + '.png" width="' + width + 'px"/>';
            return savePng(folder + device + name, bitmapData);
        }
    }
}
}
