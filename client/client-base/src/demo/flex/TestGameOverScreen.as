package {
import flash.display.Sprite;

import ru.rknrl.castles.view.game.gameOver.GameOverScreen;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.castles.view.utils.LoadImageManager;
import ru.rknrl.dto.PlayerInfoDTO;

[SWF(width="1024", height="768", frameRate="60")]
public class TestGameOverScreen extends Sprite {
    private var gameOverScreen:GameOverScreen;

    public function TestGameOverScreen() {
        const winner:PlayerInfoDTO = new PlayerInfoDTO();
        winner.photoUrl = "1";

        const losers:Vector.<PlayerInfoDTO> = new <PlayerInfoDTO>[];
        for (var i:int = 1; i <= 3; i++) {
            const loser:PlayerInfoDTO = new PlayerInfoDTO();
            loser.photoUrl = i.toString();
            losers.push(loser);
        }

        gameOverScreen = new GameOverScreen(
                new LayoutPortrait(stage.stageWidth, stage.stageHeight, stage.contentsScaleFactor),
                new CastlesLocale(""),
                new LoadImageManager(),
                winner,
                losers,
                true,
                2);
        addChild(gameOverScreen);
    }
}
}
