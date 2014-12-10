package ru.rknrl.castles.menu.screens.gameOver {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;

import ru.rknrl.castles.menu.screens.*;
import ru.rknrl.castles.utils.Colors;
import ru.rknrl.castles.utils.Label;
import ru.rknrl.castles.utils.createTextField;
import ru.rknrl.castles.utils.layout.Layout;
import ru.rknrl.castles.utils.locale.CastlesLocale;
import ru.rknrl.funnyUi.buttons.RectButton;
import ru.rknrl.utils.centerize;
import ru.rknrl.utils.changeTextFormat;

public class GameOverScreen extends Screen {
    public static const TO_MENU:String = "toMenu";
    public static const PLAY_AGAIN:String = "playAgain";

    private var titleHolder:Sprite;
    private var title:Label;
    private var rewardHolder:Sprite;
    private var rewardLabel:Label;
    private var toMenuButton:RectButton;

    private var againButton:RectButton;

    public function GameOverScreen(win:Boolean, reward:int, layout:Layout, locale:CastlesLocale) {
        addChild(titleHolder = new Sprite());
        titleHolder.addChild(title = createTitle(locale.gameOverTitle(win), layout));

        addChild(rewardHolder = new Sprite());
        rewardHolder.addChild(rewardLabel = createRewardLabel(locale.gameOverReward(reward), layout));

        addChild(toMenuButton = layout.createRectButton(locale.toMenu, Colors.randomColor()));
        toMenuButton.addEventListener(MouseEvent.CLICK, toMenuClick);

        addChild(againButton = layout.createRectButton(locale.playAgain, Colors.randomColor()));
        againButton.addEventListener(MouseEvent.CLICK, onAgainClick);

        updateLayout(layout);
        super();
    }

    public function updateLayout(layout:Layout):void {
        titleHolder.x = layout.gameOverTitleCenterX;
        titleHolder.y = layout.gameOverTitleCenterY;

        changeTextFormat(title, layout.gameOverTitleTextFormat);
        centerize(title);

        rewardHolder.x = layout.gameOverRewardCenterX;
        rewardHolder.y = layout.gameOverRewardCenterY;

        changeTextFormat(rewardLabel, layout.gameOverRewardTextFormat);
        centerize(rewardLabel);

        layout.updateRectButton(againButton);
        againButton.x = layout.gameOverAgainButtonCenterX;
        againButton.y = layout.gameOverAgainButtonCenterY;

        layout.updateRectButton(toMenuButton);
        toMenuButton.x = layout.gameOverToMenuButtonCenterX;
        toMenuButton.y = layout.gameOverToMenuButtonCenterY;
    }

    private static function createTitle(text:String, layout:Layout):Label {
        const title:Label = createTextField(layout.gameOverTitleTextFormat, text);
        title.textColor = Colors.randomColor();
        return title;
    }

    private static function createRewardLabel(text:String, layout:Layout):Label {
        const label:Label = createTextField(layout.gameOverRewardTextFormat, text);
        label.textColor = Colors.randomColor();
        return label;
    }

    private function onAgainClick(event:MouseEvent):void {
        dispatchEvent(new Event(PLAY_AGAIN));
    }

    private function toMenuClick(event:MouseEvent):void {
        dispatchEvent(new Event(TO_MENU));
    }

    override protected function set inTransition(value:Number):void {
        titleHolder.scaleX = titleHolder.scaleY = value;
        rewardHolder.scaleX = rewardHolder.scaleY = value;
        toMenuButton.scaleX = toMenuButton.scaleY = value;
        againButton.scaleX = againButton.scaleY = value;
    }
}
}











