package ru.rknrl.castles.menu.screens.skills {
import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;
import flash.utils.Dictionary;

import ru.rknrl.castles.menu.screens.MenuScreen;
import ru.rknrl.castles.menu.screens.main.SkillUpgradePrices;
import ru.rknrl.castles.rmi.AccountFacadeSender;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.Utils;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.dto.SkillType;
import ru.rknrl.dto.UpgradeSkillDTO;
import ru.rknrl.funnyUi.GoldTextField;
import ru.rknrl.utils.centerize;
import ru.rknrl.utils.changeTextFormat;

public class SkillsScreen extends MenuScreen {
    private var sender:AccountFacadeSender;
    private var skillUpgradePrices:SkillUpgradePrices;

    private var titleHolder:Sprite;
    private var title:GoldTextField;
    private var completeTitle:Label;

    private const typeToSkillView:Dictionary = new Dictionary();

    private const animated:Vector.<DisplayObject> = new <DisplayObject>[];

    public function SkillsScreen(id:String, skillLevels:SkillLevels, skillUpgradePrices:SkillUpgradePrices, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.skillUpgradePrices = skillUpgradePrices;

        titleHolder = new Sprite();
        animated.push(titleHolder);
        addChild(titleHolder);

        title = new GoldTextField(locale.skillsTitle, layout.skillsTitleTextFormat, 0, Colors.randomColor());
        titleHolder.addChild(title);

        completeTitle = createTextField(layout.skillsTitleTextFormat, locale.skillsTitleComplete);
        completeTitle.textColor = Colors.randomColor();
        titleHolder.addChild(completeTitle);

        for (var i:int = 0; i < SkillType.values.length; i++) {
            const skillType:SkillType = SkillType.values[i];
            const skillView:SkillView = createSkillView(i, layout, locale);
            skillView.addEventListener(SkillView.UPGRADE, onUpgrade);
            animated.push(skillView);
            typeToSkillView[skillType] = skillView;
            addChild(skillView);
        }

        this.skillLevels = skillLevels;
        updateLayout(layout);

        super(id);
    }

    private function getPrice():int {
        return skillUpgradePrices.getPrice(_skillLevels.getNextTotalLevel());
    }

    public function updateLayout(layout:Layout):void {
        titleHolder.x = layout.titleCenterX;
        titleHolder.y = layout.titleCenterY;

        title.textFormat = layout.skillsTitleTextFormat;
        centerize(title);

        changeTextFormat(completeTitle, layout.skillsTitleTextFormat);
        centerize(completeTitle);

        for (var i:int = 0; i < SkillType.values.length; i++) {
            const skillType:SkillType = SkillType.values[i];

            const skillWidth:int = layout.skillWidth;
            const skillHeight:int = layout.skillHeight;

            const top:int = layout.bodyCenterY;
            const gap:int = (layout.bodyWidth - SkillType.values.length * skillWidth) / (SkillType.values.length + 1);

            const skillView:SkillView = typeToSkillView[skillType];
            skillView.updateLayout(skillWidth, skillHeight, layout);
            skillView.x = gap + i * (gap + skillWidth) + skillWidth / 2;
            skillView.y = top;
        }
    }

    private static function createSkillView(i:int, layout:Layout, locale:CastlesLocale):SkillView {
        const skillWidth:int = layout.skillWidth;
        const skillHeight:int = layout.skillHeight;

        const skillType:SkillType = SkillType.values[i];
        const skillView:SkillView = new SkillView(skillType, skillWidth, skillHeight, Colors.randomColor(), layout, locale);
        return skillView;
    }

    override public function changeColors():void {
        title.color = Colors.randomColor();
        completeTitle.textColor = Colors.randomColor();
        for each(var skillView:SkillView in typeToSkillView) {
            skillView.color = Colors.randomColor();
        }
    }

    override public function set transition(value:Number):void {
        for each(var displayObject:DisplayObject in animated) {
            displayObject.scaleX = displayObject.scaleY = 0.6 + value * 0.4;
        }
    }

    private var _skillLevels:SkillLevels;

    public function set skillLevels(value:SkillLevels):void {
        _skillLevels = value;
        unlock();

        for each(var skillType:SkillType in SkillType.values) {
            SkillView(typeToSkillView[skillType]).skillLevel = value.getLevel(skillType);
        }

        if (_skillLevels.isLastTotalLevel) {
            completeTitle.visible = true;
            title.visible = false;
        } else {
            completeTitle.visible = false;
            title.visible = true;
            title.gold = getPrice();
        }

        centerize(title);
    }

    private function onUpgrade(event:Event):void {
        if (gold < getPrice()) {
            title.animate();
            dispatchEvent(new Event(Utils.NOT_ENOUGH_GOLD))
        } else {
            const skillView:SkillView = SkillView(event.target);
            const dto:UpgradeSkillDTO = new UpgradeSkillDTO();
            dto.type = skillView.skillType;
            sender.upgradeSkill(dto);

            lock();
            skillView.animate();
        }
    }

    private function lock():void {
        for each(var skillView:SkillView in typeToSkillView) {
            skillView.lock();
        }
    }

    private function unlock():void {
        for each(var skillView:SkillView in typeToSkillView) {
            skillView.unlock();
        }
    }
}
}
