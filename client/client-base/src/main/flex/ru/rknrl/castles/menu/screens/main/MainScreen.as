package ru.rknrl.castles.menu.screens.main {
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.menu.PopupManager;
import ru.rknrl.castles.menu.screens.MenuScreen;
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
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuyBuildingDTO;
import ru.rknrl.dto.RemoveBuildingDTO;
import ru.rknrl.dto.StartLocationDTO;
import ru.rknrl.dto.SwapSlotsDTO;
import ru.rknrl.dto.UpgradeBuildingDTO;
import ru.rknrl.jnb.rmi.AccountFacadeSender;
import ru.rknrl.utils.changeTextFormat;

public class MainScreen extends MenuScreen {
    private var sender:AccountFacadeSender;
    private var layout:Layout;

    private var startLocationView:StartLocationView;

    private var title:Label;
    private var points:Points;
    private var playLabel:Label;
    private var locale:CastlesLocale;
    private var popupManager:PopupManager;

    public function MainScreen(id:String, startLocation:StartLocationDTO, buildingsPrices:BuildingPrices, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale, popupManager:PopupManager) {
        this.buildingsPrices = buildingsPrices;
        this.sender = sender;
        this.locale = locale;
        this.popupManager = popupManager;

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

    // build popup

    private var buildPopup:BuildPopup;

    private function openBuildPopup(event:OpenBuildPopupEvent):void {
        buildPopup = new BuildPopup(event.slotId, _buildingsPrices.getBuildPrice(), layout, locale);
        buildPopup.addEventListener(BuildEvent.BUILD, onBuild);

        popupManager.addEventListener(PopupManager.START_CLOSE, onStartCloseBuildPopup);
        popupManager.openPopup(buildPopup);
    }

    private function onStartCloseBuildPopup(event:Event = null):void {
        buildPopup.removeEventListener(BuildEvent.BUILD, onBuild);
        popupManager.removeEventListener(PopupManager.START_CLOSE, onStartCloseBuildPopup);
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

            popupManager.closePopup();
        }
    }

    // upgrade popup

    private var upgradePopup:UpgradePopup;

    private function openUpgradePopup(event:OpenUpgradePopupEvent):void {
        const canUpgrade:Boolean = event.buildingLevel != BuildingLevel.LEVEL_3;
        const canRemove:Boolean = event.buildingsCount > 1;
        if (!canUpgrade && !canRemove) return;

        const price:int = event.buildingLevel != BuildingLevel.LEVEL_3 ? _buildingsPrices.getPrice(Utils.nextBuildingLevel(event.buildingLevel)) : 0;

        upgradePopup = new UpgradePopup(event.slotId, event.buildingType, event.buildingLevel, event.buildingsCount, price, layout, locale);
        upgradePopup.addEventListener(UpgradeEvent.UPGRADE, onUpgrade);
        upgradePopup.addEventListener(RemoveEvent.REMOVE, onRemove);

        popupManager.addEventListener(PopupManager.START_CLOSE, onStartCloseUpgradePopup);
        popupManager.openPopup(upgradePopup);
    }

    private function onStartCloseUpgradePopup(event:Event = null):void {
        popupManager.removeEventListener(PopupManager.START_CLOSE, onStartCloseUpgradePopup);

        upgradePopup.removeEventListener(UpgradeEvent.UPGRADE, onUpgrade);
        upgradePopup.removeEventListener(RemoveEvent.REMOVE, onRemove);
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

            popupManager.closePopup();
        }
    }

    private function onRemove(event:RemoveEvent):void {
        startLocationView.lockSlot(event.slotId);

        const dto:RemoveBuildingDTO = new RemoveBuildingDTO();
        dto.id = event.slotId;
        sender.removeBuilding(dto);

        popupManager.closePopup();
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
