package ru.rknrl.castles.controller {
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;
import flash.system.Security;

import ru.rknrl.castles.controller.game.GameController;
import ru.rknrl.castles.model.Model;
import ru.rknrl.castles.model.events.BuildEvent;
import ru.rknrl.castles.model.events.MagicItemClickEvent;
import ru.rknrl.castles.model.events.RemoveBuildingEvent;
import ru.rknrl.castles.model.events.SlotClickEvent;
import ru.rknrl.castles.model.events.SlotSwapEvent;
import ru.rknrl.castles.model.events.UpgradeBuildingEvent;
import ru.rknrl.castles.model.events.UpgradeClickEvent;
import ru.rknrl.castles.model.events.ViewEvents;
import ru.rknrl.castles.model.menu.bank.Products;
import ru.rknrl.castles.model.menu.main.StartLocation;
import ru.rknrl.castles.model.menu.shop.ItemsCount;
import ru.rknrl.castles.model.menu.skills.SkillLevels;
import ru.rknrl.castles.model.menu.skills.SkillUpgradePrices;
import ru.rknrl.castles.model.menu.top.Top;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.rmi.EnterGameFacadeReceiver;
import ru.rknrl.castles.rmi.EnterGameFacadeSender;
import ru.rknrl.castles.rmi.GameFacadeReceiver;
import ru.rknrl.castles.rmi.GameFacadeSender;
import ru.rknrl.castles.rmi.IAccountFacade;
import ru.rknrl.castles.rmi.IEnterGameFacade;
import ru.rknrl.castles.view.View;
import ru.rknrl.castles.view.game.GameView;
import ru.rknrl.core.rmi.Connection;
import ru.rknrl.core.social.PaymentDialogData;
import ru.rknrl.core.social.PaymentDialogEvent;
import ru.rknrl.core.social.Social;
import ru.rknrl.dto.AccountStateDTO;
import ru.rknrl.dto.AuthenticationSuccessDTO;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuyBuildingDTO;
import ru.rknrl.dto.BuyItemDTO;
import ru.rknrl.dto.GameStateDTO;
import ru.rknrl.dto.NodeLocator;
import ru.rknrl.dto.ProductDTO;
import ru.rknrl.dto.RemoveBuildingDTO;
import ru.rknrl.dto.SlotDTO;
import ru.rknrl.dto.SwapSlotsDTO;
import ru.rknrl.dto.UpgradeBuildingDTO;
import ru.rknrl.dto.UpgradeSkillDTO;
import ru.rknrl.log.Log;

public class Controller implements IAccountFacade, IEnterGameFacade {
    private var view:View;
    private var connection:Connection;
    private var policyPort:int;
    private var sender:AccountFacadeSender;
    private var log:Log;
    private var social:Social;
    private var model:Model;

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

        social.addEventListener(PaymentDialogEvent.PAYMENT_DIALOG_CLOSED, onPaymentDialogClosed);
        social.addEventListener(PaymentDialogEvent.PAYMENT_SUCCESS, onPaymentSuccess);
        social.addEventListener(PaymentDialogEvent.PAYMENT_FAIL, onPaymentFail);

        model = new Model(authenticationSuccess);

        view.addEventListener(SlotClickEvent.SLOT_CLICK, onSlotClick);
        view.addEventListener(SlotSwapEvent.SLOT_SWAP, onSlotSwap);

        view.addEventListener(BuildEvent.BUILD, onBuild);
        view.addEventListener(UpgradeBuildingEvent.UPGRADE_BUILDING, onUpgradeBuilding);
        view.addEventListener(RemoveBuildingEvent.REMOVE_BUILDING, onRemoveBuilding);

        view.addEventListener(MagicItemClickEvent.MAGIC_ITEM_CLICK, onMagicItemClick);
        view.addEventListener(UpgradeClickEvent.UPGRADE_CLICK, onUpgradeClick);

        view.addEventListener(ViewEvents.PLAY, onPlay);
        view.addEventListener(ViewEvents.BUY, onBuy);

        const startLocation:StartLocation = new StartLocation(authenticationSuccess.accountState.startLocation);
        const gold:int = authenticationSuccess.accountState.gold;
        const top:Top = new Top(authenticationSuccess.config.top);
        const itemsCount:ItemsCount = new ItemsCount(authenticationSuccess.accountState.items);
        const itemPrice:int = authenticationSuccess.config.itemPrice;
        const skillLevels:SkillLevels = new SkillLevels(authenticationSuccess.accountState.skills);
        const upgradePrices:SkillUpgradePrices = new SkillUpgradePrices(authenticationSuccess.config.skillUpgradePrices);
        const products:Products = new Products(authenticationSuccess.products);

        view.addMenu(startLocation, gold, top, itemsCount, itemPrice, skillLevels, upgradePrices, products)

        if (authenticationSuccess.enterGame) {
            view.addSearchOpponentScreen();
        } else if (authenticationSuccess.hasGame) {
            onEnteredGame(authenticationSuccess.game);
        }
    }

    public function onAccountStateUpdated(accountState:AccountStateDTO):void {
        model.accountStateDto = accountState;
        view.startLocation = model.startLocation;
        view.gold = model.gold;
        view.itemsCount = model.itemsCount;
        view.skillLevels = model.skillLevels;
    }

    private function onSlotClick(event:SlotClickEvent):void {
        const slot:SlotDTO = model.startLocation.getSlot(event.slotId);
        if (slot.hasBuildingPrototype) {
            const canUpgrade:Boolean = slot.buildingPrototype.level != BuildingLevel.LEVEL_3;
            const canRemove:Boolean = model.startLocation.buildingsCount > 1;
            const nextLevel:BuildingLevel = getNextLevel(slot.buildingPrototype.level);
            const upgradePrice:int = model.buildingPrices.getPrice(nextLevel);
            if (canUpgrade || canRemove) {
                view.openUpgradePopup(event.slotId, canUpgrade, canRemove, upgradePrice);
            }

        } else {
            view.openBuildPopup(event.slotId, model.buildingPrices.buildPrice);
        }
    }

    private function onSlotSwap(event:SlotSwapEvent):void {
        const dto:SwapSlotsDTO = new SwapSlotsDTO();
        dto.id1 = event.slotId1;
        dto.id2 = event.slotId2;
        sender.swapSlots(dto);

        view.lock = true;
    }

    private function onBuild(event:BuildEvent):void {
        if (model.gold < model.buildingPrices.buildPrice) {
            // no money
        } else {
            const dto:BuyBuildingDTO = new BuyBuildingDTO();
            dto.id = event.slotId;
            dto.buildingType = event.buildingType;
            sender.buyBuilding(dto);

            view.lock = true;
        }
    }

    private function onUpgradeBuilding(event:UpgradeBuildingEvent):void {
        const slot:SlotDTO = model.startLocation.getSlot(event.slotId);
        if (!slot.hasBuildingPrototype) throw new Error();
        const nextLevel:BuildingLevel = getNextLevel(slot.buildingPrototype.level);
        const price:int = model.buildingPrices.getPrice(nextLevel);

        if (model.gold < price) {
            // no money
        } else {
            const dto:UpgradeBuildingDTO = new UpgradeBuildingDTO();
            dto.id = event.slotId;
            sender.upgradeBuilding(dto);

            view.lock = true;
        }
    }

    private static function getNextLevel(buildingLevel:BuildingLevel):BuildingLevel {
        switch (buildingLevel) {
            case BuildingLevel.LEVEL_1:
                return BuildingLevel.LEVEL_2;
            case BuildingLevel.LEVEL_2:
                return BuildingLevel.LEVEL_3;
        }
        throw new Error(buildingLevel + " hasn't next level");
    }

    private function onRemoveBuilding(event:RemoveBuildingEvent):void {
        const dto:RemoveBuildingDTO = new RemoveBuildingDTO();
        dto.id = event.slotId;
        sender.removeBuilding(dto);

        view.lock = true;
    }

    private function onMagicItemClick(event:MagicItemClickEvent):void {
        if (model.gold < model.itemPrice) {
            // no money
        } else {
            const dto:BuyItemDTO = new BuyItemDTO();
            dto.type = event.itemType;
            sender.buyItem(dto);

            view.lock = true;
        }
    }

    private function onUpgradeClick(event:UpgradeClickEvent):void {
        if (model.gold < model.upgradePrices.getPrice(model.skillLevels.totalLevel + 1)) {
            // no money
        } else {
            const dto:UpgradeSkillDTO = new UpgradeSkillDTO();
            dto.type = event.skillType;
            sender.upgradeSkill(dto);

            view.lock = true;
        }
    }

    // payment

    private function onBuy(event:Event):void {
        const product:ProductDTO = model.products.product;
        social.showPaymentDialog(new PaymentDialogData(product.id, product.title, product.description, product.price));
        view.lock = true;
    }

    private function onPaymentDialogClosed(event:PaymentDialogEvent):void {
        trace("payment closed");
        view.lock = false;
    }

    private function onPaymentSuccess(event:PaymentDialogEvent):void {
        trace("payment success");
        view.lock = false;
    }

    private function onPaymentFail(event:PaymentDialogEvent):void {
        trace("payment fail");
        view.lock = false;
    }

    // ENTER GAME

    private function onPlay(event:Event):void {
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

        const gameView:GameView = view.addGame();
        game = new GameController(gameView, new GameFacadeSender(connection), gameState);

        gameFacadeReceiver = new GameFacadeReceiver(game);
        gameConnection.registerReceiver(gameFacadeReceiver);
    }

    public function onLeaveGame():void {
        log.add("onLeaveGame");

        view.removeGame();
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
}
}
