//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game {
import flash.events.Event;
import flash.text.TextField;
import flash.utils.getTimer;

import ru.rknrl.castles.model.game.Building;
import ru.rknrl.castles.model.game.BuildingOwner;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.model.points.Points;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.Fonts;
import ru.rknrl.castles.view.game.area.TornadoPathView;
import ru.rknrl.castles.view.game.area.arrows.ArrowsView;
import ru.rknrl.castles.view.game.area.buildings.BuildingsView;
import ru.rknrl.castles.view.game.ui.GameAvatar;
import ru.rknrl.castles.view.layout.Layout;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.menu.factory.DeviceFactory;
import ru.rknrl.castles.view.utils.tutor.TutorialView;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.ItemType;
import ru.rknrl.dto.PlayerIdDTO;
import ru.rknrl.easers.IEaser;
import ru.rknrl.easers.Linear;
import ru.rknrl.easers.interpolate;
import ru.rknrl.loaders.ILoadImageManager;
import ru.rknrl.utils.createTextField;

public class GameTutorialView extends TutorialView {
    private var arrows:ArrowsView;
    private var tornadoPath:TornadoPathView;
    private var locale:CastlesLocale;
    private var loadImageManager:ILoadImageManager;

    public function GameTutorialView(layout:Layout, locale:CastlesLocale, deviceFactory:DeviceFactory, loadImageManager:ILoadImageManager) {
        super(layout, deviceFactory);
        this.locale = locale;
        this.loadImageManager = loadImageManager;
        addChild(arrows = new ArrowsView());
        addChild(tornadoPath = new TornadoPathView());
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var textField:TextField;

    private function addText(text:String):ITutorCommand {
        return exec(function ():void {
            textField = createTextField(Fonts.loading);
            textField.textColor = 0xffffff;
            textField.text = text;
            textField.scaleX = textField.scaleY = layout.scale;
            textField.x = layout.screenCenterX - textField.width / 2;
            const areaHeight:Number = _areaV * CellSize.SIZE.id() / 2 * layout.scale;
            textField.y = _areaPos.y + areaHeight - textField.height / 2;
            itemsLayer.addChild(textField);
        });
    }

    private function get removeText():ITutorCommand {
        return exec(function ():void {
            itemsLayer.removeChild(textField);
        });
    }

    private static function indexOf(itemType:ItemType):int {
        return ItemType.values.indexOf(itemType);
    }

    private var _areaPos:Point;
    private var _areaH:int;
    private var _areaV:int;

    public function setAreaRect(areaPos:Point, h:int, v:int):void {
        _areaPos = areaPos;
        _areaH = h;
        _areaV = v;
        arrows.scaleX = arrows.scaleY = layout.scale;
        tornadoPath.scaleX = tornadoPath.scaleY = layout.scale;
        arrows.x = tornadoPath.x = _areaPos.x;
        arrows.y = tornadoPath.y = _areaPos.y;
    }

    private function toGlobal(buildingPos:Point):Point {
        return new Point(_areaPos.x + buildingPos.x * layout.scale, _areaPos.y + buildingPos.y * layout.scale)
    }

    private function toGlobalPoints(points:Vector.<Point>):Vector.<Point> {
        const result:Vector.<Point> = new <Point>[];
        for each(var point:Point in points) result.push(toGlobal(point));
        return result;
    }

    public function clickItemAndCast(itemType:ItemType, buildingPos:Point, text:String):void {
        const pos:Point = layout.gameMagicItem(indexOf(itemType));

        play(new <ITutorCommand>[
            showCursor,
            open,
            addText(text),
            tween(screenCorner, pos),
            wait(500),
            click,
            wait(500),
            tween(pos, toGlobal(buildingPos)),
            wait(500),
            click,
            wait(500),
            removeText
        ]);
    }

    public function playFireball(buildingPos:Point):void {
        clickItemAndCast(ItemType.FIREBALL, buildingPos, "Запусти фаербол в противника");
    }

    public function playVolcano(buildingPos:Point):void {
        clickItemAndCast(ItemType.VOLCANO, buildingPos, "Создай вулкан под башней противника");
    }

    public function playStrengthening(buildingPos:Point):void {
        clickItemAndCast(ItemType.STRENGTHENING, buildingPos, "Усилить свой домик");
    }

    public function playAssistance(buildingPos:Point):void {
        clickItemAndCast(ItemType.ASSISTANCE, buildingPos, "Вызывай подмогу");
    }

    public function playSelfBuildings(buildings:Vector.<Building>, playerInfo:PlayerInfo):void {
        var buildingsView:BuildingsView;

        function addBuildings():void {
            const ownerId:PlayerIdDTO = new PlayerIdDTO();
            ownerId.id = 0;

            buildingsView = new BuildingsView();
            buildingsView.scaleX = buildingsView.scaleY = layout.scale;
            buildingsView.x = _areaPos.x;
            buildingsView.y = _areaPos.y;
            itemsLayer.addChild(buildingsView);

            for each(var b:Building in buildings) {
                buildingsView.addBuilding(b.id, b.buildingType, b.buildingLevel, new BuildingOwner(true, ownerId), b.population, b.strengthened, b.pos);
            }
        }

        function removeBuildings():void {
            itemsLayer.removeChild(buildingsView);
        }

        var avatar:GameAvatar;

        function addAvatar():void {
            avatar = new GameAvatar(playerInfo, layout, locale, loadImageManager);
            avatar.bitmapDataScale = layout.bitmapDataScale;
            avatar.scaleX = avatar.scaleY = layout.scale;
            const avatarPos:Point = layout.gameAvatarPos(avatar.playerId, _areaH, _areaV);
            avatar.x = avatarPos.x;
            avatar.y = avatarPos.y;
            itemsLayer.addChild(avatar);
        }

        function removeAvatar():void {
            itemsLayer.removeChild(avatar);
        }

        play(new <ITutorCommand>[
            hideCursor,
            open,
            exec(addAvatar),
            exec(addBuildings),
            addText("Твои домики желтого цвета"),
            waitForClick,
            exec(removeAvatar),
            exec(removeBuildings),
            removeText
        ]);
    }

    public function playEnemyBuildings(buildings:Vector.<Vector.<Building>>, playerInfos:Vector.<PlayerInfo>):void {
        var buildingsView:BuildingsView;
        var i:int = 0;

        function addBuildings():void {
            const ownerId:PlayerIdDTO = new PlayerIdDTO();
            ownerId.id = i + 1;

            buildingsView = new BuildingsView();
            buildingsView.scaleX = buildingsView.scaleY = layout.scale;
            buildingsView.x = _areaPos.x;
            buildingsView.y = _areaPos.y;
            itemsLayer.addChild(buildingsView);

            for each(var b:Building in buildings[i]) {
                buildingsView.addBuilding(b.id, b.buildingType, b.buildingLevel, new BuildingOwner(true, ownerId), b.population, b.strengthened, b.pos);
            }
            i++;
            if (i == 3) i = 0; // todo
        }

        function removeBuildings():void {
            itemsLayer.removeChild(buildingsView);
        }

        var avatar:GameAvatar;

        function addAvatar():void {
            avatar = new GameAvatar(playerInfos[i], layout, locale, loadImageManager);
            avatar.bitmapDataScale = layout.bitmapDataScale;
            avatar.scaleX = avatar.scaleY = layout.scale;
            const avatarPos:Point = layout.gameAvatarPos(avatar.playerId, _areaH, _areaV);
            avatar.x = avatarPos.x;
            avatar.y = avatarPos.y;
            itemsLayer.addChild(avatar);
        }

        function removeAvatar():void {
            itemsLayer.removeChild(avatar);
        }

        const loop:Vector.<ITutorCommand> = new <ITutorCommand>[];

        for each(var builds:* in buildings) {
            loop.push(exec(addAvatar));
            loop.push(exec(addBuildings));
            loop.push(wait(1000));
            loop.push(exec(removeAvatar));
            loop.push(exec(removeBuildings));
        }

        play(new <ITutorCommand>[
            hideCursor,
            open,
            addText("У тебя 3 противника"),
            loopUntilClick(loop),
            removeText,
            clearItemsLayer
        ]);
    }

    public function playArrow(startBuildingPos:Point, endBuildingPos:Point):void {
        play(new <ITutorCommand>[
            showCursor,
            open,
            addText("Отправляй отряды и захватывай чужие домики"),
            loopUntilClick(new <ITutorCommand>[
                tween(screenCorner, toGlobal(startBuildingPos)),
                mouseDown,
                wait(400),
                exec(function ():void {
                    arrows.addArrow(startBuildingPos);
                }),
                tween(toGlobal(startBuildingPos), toGlobal(endBuildingPos)),
                wait(400),
                mouseUp,
                exec(arrows.removeArrows),
                wait(400)
            ]),
            exec(arrows.removeArrows),
            removeText
        ]);
    }

    public function playArrows(startBuildingPos1:Point, startBuildingPos2:Point, endBuildingPos:Point):void {
        play(new <ITutorCommand>[
            showCursor,
            open,
            addText("Можно отправлять отряды сразу из нескольких домиков"),
            loopUntilClick(new <ITutorCommand>[
                tween(screenCorner, toGlobal(startBuildingPos1)),
                mouseDown,
                wait(400),
                exec(function ():void {
                    arrows.addArrow(startBuildingPos1);
                }),
                tween(toGlobal(startBuildingPos1), toGlobal(startBuildingPos2)),
                wait(400),
                exec(function ():void {
                    arrows.addArrow(startBuildingPos2);
                }),
                tween(toGlobal(startBuildingPos2), toGlobal(endBuildingPos)),
                wait(400),
                mouseUp,
                exec(arrows.removeArrows),
                wait(400)
            ]),
            exec(arrows.removeArrows),
            removeText
        ]);
    }

    public function playWin():void {
        play(new <ITutorCommand>[
            hideCursor,
            open,
            addText("Захвати все домики противников, чтобы выиграть"),
            waitForClick,
            removeText
        ]);
    }

    private var tornado:Boolean;
    private var tornadoStartTime:int;
    private var tornadoPoints:Points;

    public function playTornado(points:Vector.<Point>):void {
        tornadoPoints = new Points(points);

        const pos:Point = layout.gameMagicItem(indexOf(ItemType.TORNADO));

        function addTornadoPath():void {
            tornadoStartTime = getTimer();
            tornado = true;
        }

        function removeTornadoPath():void {
            tornado = false;
            tornadoPath.clear();
        }

        play(new <ITutorCommand>[
            showCursor,
            open,
            addText("Используй торнадо против противника"),
            tween(screenCorner, pos),
            click,
            wait(400),
            tween(pos, toGlobal(points[0])),
            mouseDown,
            wait(400),
            exec(addTornadoPath),
            tweenPath(new Points(toGlobalPoints(points))),
            wait(400),
            mouseUp,
            exec(removeTornadoPath),
            wait(400),
            removeText
        ]);
    }

    private static const easer:IEaser = new Linear(0, 1);

    private function onEnterFrame(event:Event):void {
        arrows.orientArrows(new Point((cursor.x - _areaPos.x) / layout.scale, (cursor.y - _areaPos.y) / layout.scale));
        if (tornado) {
            const progress:Number = interpolate(0, 1, getTimer(), tornadoStartTime, 500, easer);
            tornadoPath.drawPath(tornadoPoints, tornadoPoints.totalDistance * progress);
        }
    }
}
}
