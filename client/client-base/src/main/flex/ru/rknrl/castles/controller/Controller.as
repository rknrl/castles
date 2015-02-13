//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller {
import flash.events.Event;

import ru.rknrl.castles.controller.game.GameController;
import ru.rknrl.castles.model.CastlesLocalStorage;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.castles.view.menu.MenuView;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.AuthenticatedDTO;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.NodeLocator;
import ru.rknrl.log.Log;
import ru.rknrl.rmi.EnteredGameEvent;
import ru.rknrl.rmi.JoinedGameEvent;
import ru.rknrl.rmi.LeavedGameEvent;
import ru.rknrl.rmi.Server;

public class Controller {
    private var view:View;
    private var server:Server;
    private var log:Log;
    private var social:Social;

    private var menu:MenuController;
    private var localStorage:CastlesLocalStorage;

    public function Controller(view:View,
                               authenticated:AuthenticatedDTO,
                               server:Server,
                               log:Log,
                               social:Social) {
        this.view = view;
        this.server = server;
        this.log = log;
        this.social = social;

        localStorage = new CastlesLocalStorage(log);

        server.addEventListener(EnteredGameEvent.ENTEREDGAME, onEnteredGame);
        view.addEventListener(ViewEvents.PLAY, onPlay);

        const model:MenuModel = new MenuModel(authenticated);
        const menuView:MenuView = view.addMenu(model);
        menu = new MenuController(menuView, server, model, social, localStorage);

        if (authenticated.searchOpponents) {
            view.hideMenu();
            view.addSearchOpponentScreen();
        } else if (authenticated.hasGame) {
            view.hideMenu();
            view.addLoadingScreen();
            joinGame(authenticated.game);
        }
    }

    private function onPlay(event:Event):void {
        view.hideMenu();
        view.addSearchOpponentScreen();
        server.enterGame();
    }

    private var game:GameController;

    private function onEnteredGame(e:EnteredGameEvent):void {
        log.add("onEnteredGame");
        joinGame(e.node);
    }

    private function joinGame(nodeLocator:NodeLocator):void {
        server.addEventListener(JoinedGameEvent.JOINEDGAME, onJoinedGame);
        server.joinGame();
    }

    private function onJoinedGame(e:JoinedGameEvent):void {
        log.add("onJoinedGame");
        server.removeEventListener(JoinedGameEvent.JOINEDGAME, onJoinedGame);
        server.addEventListener(LeavedGameEvent.LEAVEDGAME, onLeavedGame);

        const gameState:GameStateDTO = e.gameState;

        view.removeAnyLoadingScreen();

        const h:int = Math.round(gameState.width / CellSize.SIZE.id());
        const v:int = Math.round(gameState.height / CellSize.SIZE.id());

        const playerInfos:Vector.<PlayerInfo> = PlayerInfo.fromDtoVector(gameState.players);
        const gameView:GameView = view.addGame(playerInfos, h, v);
        game = new GameController(gameView, server, gameState, localStorage);
    }

    private function onLeavedGame(e:LeavedGameEvent):void {
        log.add("onLeavedGame");

        view.removeGame();
        view.showMenuAndLock();

        game.destroy();
        game = null;

        server.removeEventListener(JoinedGameEvent.JOINEDGAME, onJoinedGame);
        server.removeEventListener(LeavedGameEvent.LEAVEDGAME, onLeavedGame);
    }

    public function destroy():void {
        if (game) {
            view.removeGame();
            game.destroy();
            game = null;
        }
    }
}
}
