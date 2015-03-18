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
import flash.utils.Dictionary;
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
import ru.rknrl.castles.view.utils.AnimatedTextField;
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
            textField = createTextField(Fonts.tutorial);
            textField.text = text;
            textField.scaleX = textField.scaleY = layout.scale;
            textField.x = layout.screenCenterX - textField.width / 2;
            const areaHeight:Number = _areaV * CellSize.SIZE.id() * layout.scale;
            textField.y = _areaPos.y + areaHeight / 2 - textField.height / 2;
            itemsLayer.addChild(textField);
        });
    }

    private var buttonTextField:AnimatedTextField;

    private function addButton(text:String):ITutorCommand {
        return exec(function ():void {
            buttonTextField = new AnimatedTextField(Fonts.tutorial);
            buttonTextField.text = text;
            buttonTextField.textScale = layout.scale;
            buttonTextField.x = layout.screenCenterX;
            buttonTextField.y = layout.gameMagicItemsY;
            buttonTextField.bounce();
            itemsLayer.addChild(buttonTextField);
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

    public function itemClick(itemType:ItemType):void {
        const pos:Point = layout.gameMagicItem(indexOf(itemType));

        play(new <ITutorCommand>[
            open,
            showCursor,
            addText(itemsText[itemType]),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    tween(screenCorner, pos),
                    wait(500),
                    click,
                    wait(500)
                ]),
                infinityWait
            ])
        ]);
    }

    private function cast(itemType:ItemType, buildingPos:Point):void {
        const pos:Point = layout.gameMagicItem(indexOf(itemType));

        play(new <ITutorCommand>[
            open,
            addText(itemsText[itemType]),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    wait(500),
                    showCursor,
                    tween(pos, toGlobal(buildingPos)),
                    wait(500),
                    click,
                    wait(500)
                ]),
                infinityWait
            ])
        ]);
    }

    private static const itemsText:Dictionary = createItemsText();

    private static function createItemsText():Dictionary {
        const itemsText:Dictionary = new Dictionary();
        itemsText[ItemType.FIREBALL] = "Запусти фаербол в противника";
        itemsText[ItemType.VOLCANO] = "Создай вулкан под башней противника";
        itemsText[ItemType.STRENGTHENING] = "Усилить свой домик";
        itemsText[ItemType.ASSISTANCE] = "Вызывай подмогу";
        itemsText[ItemType.TORNADO] = "Используй торнадо против противника";
        return itemsText;
    }

    public function playFireball(buildingPos:Point):void {
        cast(ItemType.FIREBALL, buildingPos);
    }

    public function playVolcano(buildingPos:Point):void {
        cast(ItemType.VOLCANO, buildingPos);
    }

    public function playStrengthening(buildingPos:Point):void {
        cast(ItemType.STRENGTHENING, buildingPos);
    }

    public function playAssistance(buildingPos:Point):void {
        cast(ItemType.ASSISTANCE, buildingPos);
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

        play(new <ITutorCommand>[
            hideCursor,
            open,
            exec(addAvatar),
            exec(addBuildings),
            addText("Твои домики желтого цвета"),
            wait(1500),
            addButton("Дальше"),
            waitForClick
        ]);
    }

    public function playEnemyBuildings(buildings:Vector.<Vector.<Building>>, playerInfos:Vector.<PlayerInfo>):void {
        var buildingsView:BuildingsView;
        var i:int = 0;

        // todo переписать на visible всесто add/remove
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
            if (i == playerInfos.length) i = 0; // todo
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

        const loopCommands:Vector.<ITutorCommand> = new <ITutorCommand>[];

        for each(var builds:* in buildings) {
            loopCommands.push(exec(addAvatar));
            loopCommands.push(exec(addBuildings));
            loopCommands.push(wait(1000));
            loopCommands.push(exec(removeAvatar));
            loopCommands.push(exec(removeBuildings));
        }

        play(new <ITutorCommand>[
            hideCursor,
            open,
            addText(playerInfos.length == 3 ? "У тебя 3 противника" : "Твой противник бирюзовый"),
            parallel(new <ITutorCommand>[
                loop(loopCommands),
                sequence(new <ITutorCommand>[
                    wait(2000),
                    addButton("Дальше"),
                    waitForClick
                ])
            ])
        ]);
    }

    public function playArrow(startBuildingPos:Point, endBuildingPos:Point):void {
        play(new <ITutorCommand>[
            showCursor,
            open,
            addText("Отправляй отряды и захватывай чужие домики"),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
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
                waitForClick
            ])
        ]);
    }

    public function playArrows(startBuildingPos1:Point, startBuildingPos2:Point, endBuildingPos:Point):void {
        play(new <ITutorCommand>[
            showCursor,
            open,
            addText("Можно отправлять отряды сразу из нескольких домиков"),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
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
                waitForClick
            ])
        ]);
    }

    public function playWin():void {
        play(new <ITutorCommand>[
            hideCursor,
            open,
            addText("Захвати все домики противников, чтобы выиграть"),
            wait(1500),
            addButton("В бой!"),
            waitForClick
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
            open,
            addText(itemsText[ItemType.TORNADO]),
            parallel(new <ITutorCommand>[
                loop(new <ITutorCommand>[
                    wait(400),
                    showCursor,
                    tween(pos, toGlobal(points[0])),
                    mouseDown,
                    wait(400),
                    exec(addTornadoPath),
                    tweenPath(new Points(toGlobalPoints(points))),
                    wait(400),
                    mouseUp,
                    exec(removeTornadoPath),
                    wait(400)
                ]),
                infinityWait
            ])
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

    override protected function clear():void {
        arrows.removeArrows();
        tornado = false;
        tornadoPath.clear();
        super.clear();
    }
}
}
