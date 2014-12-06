package ru.rknrl.castles.menu.screens.main {
import flash.events.Event;
import flash.events.MouseEvent;
import flash.text.TextField;

import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.menu.screens.main.popup.PopupScreen;
import ru.rknrl.castles.menu.screens.main.popup.build.BuildEvent;
import ru.rknrl.castles.menu.screens.main.popup.build.BuildPopup;
import ru.rknrl.castles.menu.screens.main.popup.upgrade.RemoveEvent;
import ru.rknrl.castles.menu.screens.main.popup.upgrade.UpgradeEvent;
import ru.rknrl.castles.menu.screens.main.popup.upgrade.UpgradePopup;
import ru.rknrl.castles.menu.screens.main.startLocation.StartLocationView;
import ru.rknrl.castles.menu.screens.main.startLocation.events.OpenBuildPopupEvent;
import ru.rknrl.castles.menu.screens.main.startLocation.events.OpenUpgradePopupEvent;
import ru.rknrl.castles.menu.screens.main.startLocation.events.SwapEvent;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;
import ru.rknrl.dto.BuyBuildingDTO;
import ru.rknrl.dto.RemoveBuildingDTO;
import ru.rknrl.dto.SlotId;
import ru.rknrl.dto.StartLocationDTO;
import ru.rknrl.dto.SwapSlotsDTO;
import ru.rknrl.dto.UpgradeBuildingDTO;
import ru.rknrl.jnb.rmi.AccountFacadeSender;
import ru.rknrl.utils.changeTextFormat;
import ru.rknrl.utils.createTextField;

public class MainScreen extends MenuScreen {
    private var sender:AccountFacadeSender;
    private var layout:Layout;

    private var startLocationView:StartLocationView;

    private var title:TextField;
    private var points:Points;
    private var playLabel:TextField;
    private var locale:CastlesLocale;

    public function MainScreen(id:String, startLocation:StartLocationDTO, buildingsPrices:BuildingPrices, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.buildingsPrices = buildingsPrices;
        this.sender = sender;
        this.locale = locale;

        startLocationView = new StartLocationView(startLocation, layout);
        startLocationView.addEventListener(OpenBuildPopupEvent.OPEN_BUILD_POPUP, openBuildPopup);
        startLocationView.addEventListener(OpenUpgradePopupEvent.OPEN_UPGRADE_POPUP, openUpgradePopup);
        startLocationView.addEventListener(SwapEvent.SWAP_SLOTS, swapSlots);
        addChild(startLocationView);

        addChild(title = createTextField(layout.mainTitleTextFormat, locale.mainTitle));
        addChild(points = new Points());

        addChild(playLabel = createTextField(layout.playTextFormat, locale.play));
        playLabel.addEventListener(MouseEvent.CLICK, onPlayClick);

        title.visible = points.visible = playLabel.visible = !layout.onlyStartLocationInMainScreen;

        updateLayout(layout);

        super(id);
    }

    public function updateLayout(layout:Layout):void {
        this.layout = layout;

        startLocationView.scaleX = startLocationView.scaleY = layout.scale;
        startLocationView.x = layout.locationCenterX;
        startLocationView.y = layout.locationCenterY;

        changeTextFormat(title, layout.mainTitleTextFormat);
        title.x = (layout.stageWidth - title.width) / 2;
        title.y = layout.bodyTop - title.height / 2;

        changeTextFormat(playLabel, layout.playTextFormat);
        playLabel.x = (layout.stageWidth - playLabel.width) / 2;
        playLabel.y = layout.playCenterY - playLabel.height / 2;

        points.scaleX = points.scaleY = layout.scale;
        points.x = layout.stageCenterX;
        points.y = layout.pointsCenterY;

        if (popupScreen) popupScreen.updateLayout(layout);
        if (buildPopup) buildPopup.updateLayout(layout);
        if (upgradePopup) upgradePopup.updateLayout(layout);
    }

    private function onPlayClick(event:MouseEvent):void {
        dispatchEvent(new Event(Utils.PLAY, true));
    }

    private var _buildingsPrices:BuildingPrices;

    public function set buildingsPrices(value:BuildingPrices):void {
        _buildingsPrices = value;
    }

    public function set startLocation(value:StartLocationDTO):void {
        startLocationView.startLocation = value;
    }

    override public function changeColors():void {
        startLocationView.color = Colors.randomColor();
        title.textColor = Colors.randomColor();
        playLabel.textColor = Colors.randomColor();
    }

    private var popupScreen:PopupScreen;

    // build popup

    private var buildPopup:BuildPopup;

    private function openBuildPopup(event:OpenBuildPopupEvent):void {
        if (buildPopup) throw new Error("Build popup already open");

        popupScreen = new PopupScreen(layout);
        popupScreen.addEventListener(MouseEvent.MOUSE_DOWN, closeBuildPopup);
        addChild(popupScreen);

        buildPopup = createBuildPopup(event.slotId, _buildingsPrices.getBuildPrice());
        buildPopup.addEventListener(BuildEvent.BUILD, onBuild);
        addChild(buildPopup);
    }

    private function createBuildPopup(slotId:SlotId, buildPrice:int):BuildPopup {
        return new BuildPopup(slotId, buildPrice, layout, locale);
    }

    private function closeBuildPopup(event:Event = null):void {
        buildPopup.removeEventListener(BuildEvent.BUILD, onBuild);
        buildPopup.close();

        popupScreen.removeEventListener(MouseEvent.MOUSE_DOWN, closeBuildPopup);
        popupScreen.addEventListener(Event.COMPLETE, onBuildCloseComplete);
        popupScreen.close();
    }

    private function onBuildCloseComplete(event:Event):void {
        popupScreen.removeEventListener(Event.COMPLETE, onBuildCloseComplete);
        removeChild(buildPopup);
        buildPopup = null;
        removeChild(popupScreen);
        popupScreen = null;
    }

    private function onBuild(event:BuildEvent):void {
        if (gold < _buildingsPrices.getBuildPrice()) {
            buildPopup.animate();
            dispatchEvent(new Event(Utils.NOT_ENOUGH_GOLD))
        } else {
            startLocationView.lockSlot(event.slotId);

            const dto:BuyBuildingDTO = new BuyBuildingDTO();
            dto.id = event.slotId;
            dto.buildingType = event.buildingType;
            sender.buyBuilding(dto);

            closeBuildPopup();
        }
    }

    // upgrade popup

    private var upgradePopup:UpgradePopup;

    private function openUpgradePopup(event:OpenUpgradePopupEvent):void {
        if (upgradePopup) throw new Error("upgrade popup already open");

        const canUpgrade:Boolean = event.buildingLevel != BuildingLevel.LEVEL_3;
        const canRemove:Boolean = event.buildingsCount > 1;
        if (!canUpgrade && !canRemove) return;

        popupScreen = new PopupScreen(layout);
        popupScreen.addEventListener(MouseEvent.MOUSE_DOWN, closeUpgradePopup);
        addChild(popupScreen);

        const price:int = event.buildingLevel != BuildingLevel.LEVEL_3 ? _buildingsPrices.getPrice(Utils.nextBuildingLevel(event.buildingLevel)) : 0;

        upgradePopup = createUpgradePopup(event.slotId, event.buildingType, event.buildingLevel, event.buildingsCount, price);
        upgradePopup.addEventListener(UpgradeEvent.UPGRADE, onUpgrade);
        upgradePopup.addEventListener(RemoveEvent.REMOVE, onRemove);
        addChild(upgradePopup);
    }

    private function createUpgradePopup(slotId:SlotId, buildingType:BuildingType, buildingLevel:BuildingLevel, buildingsCount:int, price:int):UpgradePopup {
        return new UpgradePopup(slotId, buildingType, buildingLevel, buildingsCount, price, layout, locale);
    }

    private function closeUpgradePopup(event:Event = null):void {
        upgradePopup.removeEventListener(UpgradeEvent.UPGRADE, onUpgrade);
        upgradePopup.removeEventListener(RemoveEvent.REMOVE, onRemove);
        upgradePopup.close();

        popupScreen.removeEventListener(MouseEvent.MOUSE_DOWN, closeUpgradePopup);
        popupScreen.addEventListener(Event.COMPLETE, onUpgradeCloseComplete);
        popupScreen.close();
    }

    private function onUpgradeCloseComplete(event:Event):void {
        popupScreen.removeEventListener(Event.COMPLETE, onUpgradeCloseComplete);
        removeChild(upgradePopup);
        upgradePopup = null;
        removeChild(popupScreen);
        popupScreen = null;
    }

    private function onUpgrade(event:UpgradeEvent):void {
        if (gold < _buildingsPrices.getPrice(event.toLevel)) {
            upgradePopup.animate();
            dispatchEvent(new Event(Utils.NOT_ENOUGH_GOLD))
        } else {
            startLocationView.lockSlot(event.slotId);

            const dto:UpgradeBuildingDTO = new UpgradeBuildingDTO();
            dto.id = event.slotId;
            sender.upgradeBuilding(dto);

            closeUpgradePopup();
        }
    }

    private function onRemove(event:RemoveEvent):void {
        startLocationView.lockSlot(event.slotId);

        const dto:RemoveBuildingDTO = new RemoveBuildingDTO();
        dto.id = event.slotId;
        sender.removeBuilding(dto);

        closeUpgradePopup();
    }

    // swap

    private function swapSlots(event:SwapEvent):void {
        startLocationView.lockSlot(event.slotId1);
        startLocationView.lockSlot(event.slotId2);

        const dto:SwapSlotsDTO = new SwapSlotsDTO();
        dto.id1 = event.slotId1;
        dto.id2 = event.slotId2;
        sender.swapSlots(dto)
    }

    public function removeListeners():void {
        startLocationView.removeListeners();
    }
}
}
