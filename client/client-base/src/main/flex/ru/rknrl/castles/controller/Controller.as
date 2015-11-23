//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller {
import flash.events.Event;

import protos.Authenticated;
import protos.CellSize;
import protos.DeviceType;
import protos.EnteredGameEvent;
import protos.GameState;
import protos.GameStateEvent;
import protos.LeavedGameEvent;
import protos.NodeLocator;
import protos.Place;
import protos.Top;
import protos.WeekNumber;

import ru.rknrl.asocial.ISocial;
import ru.rknrl.castles.controller.game.GameController;
import ru.rknrl.castles.controller.game.GameSplash;
import ru.rknrl.castles.model.MutableTutorState;
import ru.rknrl.castles.model.events.AcceptTopEvent;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.menu.MenuView;
import ru.rknrl.log.Log;

public class Controller {
    private var view:View;
    private var server:Server;
    private var social:ISocial;

    private var menu:MenuController;
    private var isFirstGame:Boolean;
    private var deviceType:DeviceType;

    public function Controller(view:View,
                               authenticated:Authenticated,
                               server:Server,
                               social:ISocial,
                               deviceType:DeviceType) {
        this.view = view;
        this.server = server;
        this.social = social;
        this.deviceType = deviceType;

        isFirstGame = authenticated.accountState.gamesCount == 0;

        server.addEventListener(EnteredGameEvent.ENTERED_GAME, onEnteredGame);
        view.addEventListener(ViewEvents.PLAY, onPlay);

        const model:MenuModel = new MenuModel(authenticated);
        const menuView:MenuView = view.addMenu(model);
        menu = new MenuController(menuView, server, model, social, MutableTutorState.fromDto(authenticated.tutor), authenticated.accountState.gamesCount);

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

    private function addLastWeekTop(lastWeekTop:Top, lastWeekPlace:Place):void {
        view.addEventListener(AcceptTopEvent.ACCEPT_TOP, onAcceptLastWeekTop);
        view.addLastWeekTop(lastWeekTop, lastWeekPlace);
    }

    private function onAcceptLastWeekTop(e:AcceptTopEvent):void {
        view.removeEventListener(AcceptTopEvent.ACCEPT_TOP, onAcceptLastWeekTop);
        server.acceptWeekTop(new WeekNumber(e.top.weekNumber));
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
        menu.cancelAdvert();
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
        server.addEventListener(GameStateEvent.GAME_STATE, onJoinedGame);
        server.joinGame();
    }

    private function onJoinedGame(e:GameStateEvent):void {
        server.removeEventListener(GameStateEvent.GAME_STATE, onJoinedGame);
        server.addEventListener(LeavedGameEvent.LEAVED_GAME, onLeavedGame);

        const gameState:GameState = e.getGameState();

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

        server.removeEventListener(GameStateEvent.GAME_STATE, onJoinedGame);
        server.removeEventListener(LeavedGameEvent.LEAVED_GAME, onLeavedGame);
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
