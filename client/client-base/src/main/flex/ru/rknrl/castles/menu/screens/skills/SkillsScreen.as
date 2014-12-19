package ru.rknrl.castles.menu.screens.skills {
import flash.display.DisplayObject;
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

    private var title:GoldTextField;
    private var completeTitle:Label;

    private const typeToSkillView:Dictionary = new Dictionary();

    private const animated:Vector.<DisplayObject> = new <DisplayObject>[];

    private static const colors:Vector.<uint> = new <uint>[
        Colors.magenta,
        Colors.red,
        Colors.yellow
    ];

    public function SkillsScreen(id:String, skillLevels:SkillLevels, skillUpgradePrices:SkillUpgradePrices, sender:AccountFacadeSender, layout:Layout, locale:CastlesLocale) {
        this.sender = sender;
        this.skillUpgradePrices = skillUpgradePrices;

        title = new GoldTextField(locale.skillsTitle, layout.skillsTitleTextFormat, 0, Colors.randomColor());
        addChild(title);

        completeTitle = createTextField(layout.skillsTitleTextFormat, locale.skillsTitleComplete);
        completeTitle.textColor = Colors.randomColor();
        addChild(completeTitle);

        for (var i:int = 0; i < SkillType.values.length; i++) {
            const skillType:SkillType = SkillType.values[i];
            const skillView:FlaskView = createSkillView(i, layout, locale);
            skillView.addEventListener(FlaskView.UPGRADE, onUpgrade);
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
        title.textFormat = layout.skillsTitleTextFormat;
        title.x = layout.stageWidth - layout.minigap - title.width;
        title.y = layout.titleY;

        changeTextFormat(completeTitle, layout.skillsTitleTextFormat);
        completeTitle.x = layout.stageWidth - layout.minigap - completeTitle.width;
        completeTitle.y = layout.titleY;

        const flaskWidth:int = 75;
        const flaskHeight:int = 182;

        const count:int = SkillType.values.length;
        const top:int = layout.bodyCenterY - flaskHeight / 2;
        const gap:int = layout.shopItemGap;
        const left:int = layout.stageCenterX - ((flaskWidth + gap) * count - gap) / 2 + flaskWidth / 2;

        for (var i:int = 0; i < count; i++) {
            const skillType:SkillType = SkillType.values[i];

            const skillView:FlaskView = typeToSkillView[skillType];
            skillView.updateLayout(flaskWidth, flaskHeight, layout);
            skillView.x = left + i * (gap + flaskWidth);
            skillView.y = top;
        }
    }

    private static function createSkillView(i:int, layout:Layout, locale:CastlesLocale):FlaskView {
        const skillWidth:int = layout.skillWidth;
        const skillHeight:int = layout.skillHeight;

        const skillType:SkillType = SkillType.values[i];
        const skillView:FlaskView = new FlaskView(skillType, skillWidth, skillHeight, colors[i], layout, locale);
        return skillView;
    }

    override public function changeColors():void {
        title.color = Colors.randomColor();
        completeTitle.textColor = Colors.randomColor();
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
            FlaskView(typeToSkillView[skillType]).skillLevel = value.getLevel(skillType);
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
            const skillView:FlaskView = FlaskView(event.target);
            const dto:UpgradeSkillDTO = new UpgradeSkillDTO();
            dto.type = skillView.skillType;
            sender.upgradeSkill(dto);

            lock();
            skillView.animate();
        }
    }

    private function lock():void {
        for each(var skillView:FlaskView in typeToSkillView) {
            skillView.lock();
        }
    }

    private function unlock():void {
        for each(var skillView:FlaskView in typeToSkillView) {
            skillView.unlock();
        }
    }
}
}
