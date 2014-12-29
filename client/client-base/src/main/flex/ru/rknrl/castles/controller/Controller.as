package ru.rknrl.castles.controller {
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.system.Security;

import ru.rknrl.castles.controller.game.GameController;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.MenuModel;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.rmi.EnterGameFacadeReceiver;
import ru.rknrl.castles.rmi.EnterGameFacadeSender;
import ru.rknrl.castles.rmi.GameFacadeReceiver;
import ru.rknrl.castles.rmi.GameFacadeSender;
import ru.rknrl.castles.rmi.IAccountFacade;
import ru.rknrl.castles.rmi.IEnterGameFacade;
import ru.rknrl.castles.view.MenuView;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.core.rmi.Connection;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.AuthenticationSuccessDTO;
import ru.rknrl.dto.CellSize;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.NodeLocator;
import ru.rknrl.log.Log;

public class Controller implements IAccountFacade, IEnterGameFacade {
    private var view:View;
    private var connection:Connection;
    private var policyPort:int;
    private var sender:AccountFacadeSender;
    private var log:Log;
    private var social:Social;

    private var menu:MenuController;

    public function Controller(view:View,
                               authenticationSuccess:AuthenticationSuccessDTO,
                               connection:Connection,
                               policyPort:int,
                               sender:AccountFacadeSender,
                               log:Log,
                               social:Social) {
        this.view = view;
        this.connection = connection;
        this.policyPort = policyPort;
        this.sender = sender;
        this.log = log;
        this.social = social;

        view.addEventListener(ViewEvents.PLAY, onPlay);

        const model:MenuModel = new MenuModel(authenticationSuccess);
        const menuView:MenuView = view.addMenu(model);
        menu = new MenuController(menuView, sender, model, social);

        if (authenticationSuccess.enterGame) {
            view.hideMenu();
            view.addSearchOpponentScreen();
        } else if (authenticationSuccess.hasGame) {
            view.hideMenu();
            view.addLoadingScreen();
            onEnteredGame(authenticationSuccess.game);
        }
    }

    public function onAccountStateUpdated(dto:AccountStateDTO):void {
        menu.onAccountStateUpdated(dto);
    }

    private function onPlay(event:Event):void {
        view.hideMenu();
        view.addSearchOpponentScreen();
        sender.enterGame();
    }

    private var game:GameController;
    private var gameConnection:Connection;
    private var gameFacadeReceiver:GameFacadeReceiver;

    public function onEnteredGame(nodeLocator:NodeLocator):void {
        log.add("onEnteredGame");

        if (connection.host == nodeLocator.host && connection.port == nodeLocator.port) {
            gameConnection = connection;
            onGameConnect()
        } else {
            Security.loadPolicyFile("xmlsocket://" + connection.host + ":" + policyPort);

            gameConnection = new Connection();
            gameConnection.addEventListener(Event.CONNECT, onGameConnect);
            gameConnection.addEventListener(SecurityErrorEvent.SECURITY_ERROR, onGameConnectionError);
            gameConnection.addEventListener(IOErrorEvent.IO_ERROR, onGameConnectionError);
            gameConnection.addEventListener(Event.CLOSE, onGameConnectionError);
            gameConnection.connect(nodeLocator.host, nodeLocator.port);
        }
    }

    private var enterGameFacadeSender:EnterGameFacadeSender;
    private var enterGameFacadeReceiver:EnterGameFacadeReceiver;

    private function onGameConnect(event:Event = null):void {
        log.add("onGameConnect");

        enterGameFacadeSender = new EnterGameFacadeSender(gameConnection);
        enterGameFacadeReceiver = new EnterGameFacadeReceiver(this);
        gameConnection.registerReceiver(enterGameFacadeReceiver);

        enterGameFacadeSender.join();
    }

    public function onJoinGame(gameState:GameStateDTO):void {
        log.add("onJoinGame");

        view.removeAnyLoadingScreen();

        const h:int = Math.round(gameState.width / CellSize.SIZE.id());
        const v:int = Math.round(gameState.height / CellSize.SIZE.id());
        const gameView:GameView = view.addGame(h, v);
        game = new GameController(gameView, new GameFacadeSender(connection), gameState);

        gameFacadeReceiver = new GameFacadeReceiver(game);
        gameConnection.registerReceiver(gameFacadeReceiver);
    }

    public function onLeaveGame():void {
        log.add("onLeaveGame");

        view.removeGame();
        view.showMenu();

        game.destroy();
        game = null;

        gameConnection.unregisterReceiver(gameFacadeReceiver);
        gameConnection.unregisterReceiver(enterGameFacadeReceiver);

        gameConnection.removeEventListener(Event.CONNECT, onGameConnect);
        gameConnection.removeEventListener(SecurityErrorEvent.SECURITY_ERROR, onGameConnectionError);
        gameConnection.removeEventListener(IOErrorEvent.IO_ERROR, onGameConnectionError);
        gameConnection.removeEventListener(Event.CLOSE, onGameConnectionError);

        if (gameConnection.host != connection.host || gameConnection.port != connection.port) {
            gameConnection.close();
        }

        gameConnection = null;
    }

    private function onGameConnectionError(event:Event):void {
        throw new Error(event.toString);
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
