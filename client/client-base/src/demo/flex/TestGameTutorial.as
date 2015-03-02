//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package {
import flash.display.Sprite;
import flash.display.StageAlign;
import flash.display.StageScaleMode;
import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.ui.Keyboard;

import ru.rknrl.castles.model.DtoMock;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.layout.LayoutLandscape;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.CanvasFactory;
import ru.rknrl.dto.BuildingDTO;
import ru.rknrl.dto.PlayerDTO;
import ru.rknrl.dto.SlotsPosDTO;
import ru.rknrl.loaders.ILoader;
import ru.rknrl.loaders.LoadImageManagerMock;
import ru.rknrl.loaders.ParallelLoader;
import ru.rknrl.loaders.TextLoader;

[SWF(width="1024", height="768", frameRate="60")]
public class TestGameTutorial extends Sprite {
    private var localeLoader:TextLoader;
    private var gameView:GameView;
    private var buildings:Vector.<BuildingDTO>;
    private var playerInfos:Vector.<PlayerDTO>;

    public function TestGameTutorial() {
        stage.align = StageAlign.TOP_LEFT;
        stage.scaleMode = StageScaleMode.NO_SCALE;

        const loaders:Vector.<ILoader> = new <ILoader>[];
        loaders.push(localeLoader = new TextLoader("castles - RU.tsv"));

        const loader:ParallelLoader = new ParallelLoader(loaders);
        loader.addEventListener(Event.COMPLETE, onComplete);
        loader.load();
    }

    private var loadImageManager:LoadImageManagerMock;

    private function onComplete(event:Event):void {
        loadImageManager = new LoadImageManagerMock(DtoMock.mockAvatars, 0);
        loadImageManager.addEventListener(Event.COMPLETE, onLoadImageManagerComplete);
    }

    private function onLoadImageManagerComplete(event:Event):void {
        const locale:CastlesLocale = new CastlesLocale(localeLoader.text);
        const layout:Layout = new LayoutLandscape(1024, 768, 2);

        const view:View = new View(layout, locale, loadImageManager, new CanvasFactory());
        addChild(view);

        const w:int = layout is LayoutPortrait ? 8 : 15;
        const h:int = layout is LayoutPortrait ? 11 : 15;
        playerInfos = layout is LayoutPortrait ? DtoMock.playerInfosPortrait() : DtoMock.playerInfosLandscape();

        gameView = view.addGame(PlayerInfo.fromDtoVector(playerInfos), w, h);
        buildings = layout is LayoutPortrait ? DtoMock.buildingsPortrait() : DtoMock.buildingsLandscape();
        for each(var b:BuildingDTO in  buildings) {
            gameView.area.addBuilding(b.id, b.building.type, b.building.level, new BuildingOwner(b.hasOwner, b.owner), b.population, b.strengthened, new Point(b.pos.x, b.pos.y));
        }
        const slotsPos:Vector.<SlotsPosDTO> = layout is LayoutPortrait ? DtoMock.slotsPosPortrait() : DtoMock.slotsPosLandscape();
        for each(var s:SlotsPosDTO in slotsPos) {
            gameView.area.addSlots(s);
        }

        stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    private function getBuilding(id:int):BuildingDTO {
        for each(var b:BuildingDTO in buildings) {
            if (b.id.id == id) return b;
        }
        throw new Error("can't find building " + id);
    }

    private function buildingsByOwner(ownerId:int):Vector.<BuildingDTO> {
        const result:Vector.<BuildingDTO> = new <BuildingDTO>[];
        for each(var b:BuildingDTO in buildings) {
            if (b.hasOwner && b.owner.id == ownerId) result.push(b);
        }
        return result;
    }

    private function buildingPos(id:int):Point {
        return Point.fromDto(getBuilding(id).pos);
    }

    private function onKeyDown(event:KeyboardEvent):void {
        switch (event.keyCode) {
            case Keyboard.NUMBER_1:
                gameView.tutor.playSelfBuildings(buildingsByOwner(0), PlayerInfo.fromDto(playerInfos[0]));
                break;
            case Keyboard.NUMBER_2:
                gameView.tutor.playEnemyBuildings(
                        new <Vector.<BuildingDTO>>[
                            buildingsByOwner(1),
                            buildingsByOwner(2),
                            buildingsByOwner(3)
                        ],
                        new <PlayerInfo>[
                            PlayerInfo.fromDto(playerInfos[1]),
                            PlayerInfo.fromDto(playerInfos[2]),
                            PlayerInfo.fromDto(playerInfos[3])
                        ]);
                break;
            case Keyboard.NUMBER_3:
                gameView.tutor.playArrow(buildingPos(1),
                        buildingPos(44));
                break;
            case Keyboard.NUMBER_4:
                gameView.tutor.playArrows(buildingPos(1),
                        buildingPos(44),
                        buildingPos(40));
                break;
            case Keyboard.NUMBER_5:
                gameView.tutor.playFireball(buildingPos(11));
                break;
            case Keyboard.NUMBER_6:
                gameView.tutor.playAssistance(buildingPos(1));
                break;
            case Keyboard.NUMBER_7:
                gameView.tutor.playStrengthening(buildingPos(1));
                break;
            case Keyboard.NUMBER_8:
                gameView.tutor.playVolcano(buildingPos(11));
                break;
            case Keyboard.NUMBER_9:
                gameView.tutor.playTornado(new <Point>[buildingPos(11), buildingPos(11), buildingPos(11)]);
                break;
        }
    }
}
}
