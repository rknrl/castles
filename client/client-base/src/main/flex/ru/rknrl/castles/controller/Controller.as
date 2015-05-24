//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller {
import flash.events.Event;

import ru.rknrl.Log;
import ru.rknrl.asocial.ISocial;
import ru.rknrl.castles.controller.game.GameController;
import ru.rknrl.castles.controller.game.GameSplash;
import ru.rknrl.castles.model.events.AcceptTopEvent;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.model.menu.top.Top;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.menu.MenuView;
import ru.rknrl.dto.AuthenticatedDTO;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.DeviceType;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.NodeLocator;
import ru.rknrl.dto.PlaceDTO;
import ru.rknrl.dto.TopDTO;
import ru.rknrl.dto.WeekNumberDTO;
import ru.rknrl.rmi.EnteredGameEvent;
import ru.rknrl.rmi.JoinedGameEvent;
import ru.rknrl.rmi.LeavedGameEvent;
import ru.rknrl.rmi.Server;

public class Controller {
    private var view:View;
    private var server:Server;
    private var social:ISocial;

    private var menu:MenuController;
    private var isFirstGame:Boolean;
    private var deviceType:DeviceType;

    public function Controller(view:View,
                               authenticated:AuthenticatedDTO,
                               server:Server,
                               social:ISocial,
                               deviceType:DeviceType) {
        this.view = view;
        this.server = server;
        this.social = social;
        this.deviceType = deviceType;

        isFirstGame = authenticated.accountState.gamesCount == 0;

        server.addEventListener(EnteredGameEvent.ENTEREDGAME, onEnteredGame);
        view.addEventListener(ViewEvents.PLAY, onPlay);

        const model:MenuModel = new MenuModel(authenticated);
        const menuView:MenuView = view.addMenu(model);
        menu = new MenuController(menuView, server, model, social, authenticated.tutor, authenticated.accountState.gamesCount);

        if (authenticated.searchOpponents) {
            view.hideMenu();
            if (!addGameSplash()) view.addSearchOpponentScreen();
        } else if (authenticated.hasGame) {
            view.hideMenu();
            if (!addGameSplash()) view.addLoadingScreen();
            joinGame(authenticated.game);
        } else if (authenticated.hasLastWeekTop) {
            view.hideMenu();
            addLastWeekTop(authenticated.lastWeekTop, authenticated.lastWeekPlace);
        }
    }

    private function addLastWeekTop(lastWeekTop:TopDTO, lastWeekPlace:PlaceDTO):void {
        view.addEventListener(AcceptTopEvent.ACCEPT_TOP, onAcceptLastWeekTop);
        view.addLastWeekTop(new Top(lastWeekTop), lastWeekPlace.place.toNumber());
    }

    private function onAcceptLastWeekTop(e:AcceptTopEvent):void {
        view.removeEventListener(AcceptTopEvent.ACCEPT_TOP, onAcceptLastWeekTop);
        const dto:WeekNumberDTO = new WeekNumberDTO();
        dto.weekNumber = e.top.weekNumber;
//        server.acceptWeekTop(dto);
        view.removeLastWeekTop();
        view.showMenu();
    }

    private function addGameSplash():Boolean {
        if (isFirstGame && deviceType == DeviceType.PC) {
            gameSplash = new GameSplash(view.addGameSplash());
            gameSplash.addEventListener(GameSplash.GAME_SPLASH_COMPLETE, onGameSplashComplete);
            return true;
        }
        return false;
    }

    private function onPlay(event:Event):void {
        view.hideMenu();
        if (!addGameSplash()) view.addSearchOpponentScreen();
        server.enterGame();
    }

    private function onGameSplashComplete(e:Event):void {
        gameSplash.removeEventListener(GameSplash.GAME_SPLASH_COMPLETE, onGameSplashComplete);
        view.removeGameSplash();
        gameSplash = null;
        if (game) {
            view.addGame();
        } else {
            view.addLoadingScreen();
        }
    }

    private var gameSplash:GameSplash;
    private var game:GameController;

    private function onEnteredGame(e:EnteredGameEvent):void {
        joinGame(e.node);
    }

    private function joinGame(nodeLocator:NodeLocator):void {
        server.addEventListener(JoinedGameEvent.JOINEDGAME, onJoinedGame);
        server.joinGame();
    }

    private function onJoinedGame(e:JoinedGameEvent):void {
        server.removeEventListener(JoinedGameEvent.JOINEDGAME, onJoinedGame);
        server.addEventListener(LeavedGameEvent.LEAVEDGAME, onLeavedGame);

        const gameState:GameStateDTO = e.gameState;

        const h:int = Math.round(gameState.width / CellSize.SIZE.id());
        const v:int = Math.round(gameState.height / CellSize.SIZE.id());

        const playerInfos:Vector.<PlayerInfo> = PlayerInfo.fromDtoVector(gameState.players);
        const gameView:GameView = view.createGame(playerInfos, h, v);
        game = new GameController(gameView, server, gameState, isFirstGame);

        if (!gameSplash) {
            view.removeAnyLoadingScreen();
            view.addGame();
        }
    }

    private function onLeavedGame(e:LeavedGameEvent):void {
        view.removeGame();
        view.showMenuAndLock();

        game.destroy();
        game = null;
        isFirstGame = false;

        server.removeEventListener(JoinedGameEvent.JOINEDGAME, onJoinedGame);
        server.removeEventListener(LeavedGameEvent.LEAVEDGAME, onLeavedGame);
    }

    public function destroy():void {
        Log.info("controller destroy");
        if (game) {
            view.removeGame();
            game.destroy();
            game = null;
        }
    }
}
}
