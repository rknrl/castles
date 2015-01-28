package {
import flash.display.Sprite;

import ru.rknrl.castles.controller.mock.DtoMock;
import ru.rknrl.castles.controller.mock.LoadImageManagerMock;
import ru.rknrl.castles.model.userInfo.PlayerInfo;
import ru.rknrl.castles.view.game.gameOver.GameOverScreen;
import ru.rknrl.castles.view.layout.LayoutPortrait;
import ru.rknrl.castles.view.locale.CastlesLocale;
import ru.rknrl.dto.PlayerInfoDTO;

[SWF(width="1024", height="768", frameRate="60")]
public class TestGameOverScreen extends Sprite {
    private var gameOverScreen:GameOverScreen;

    public function TestGameOverScreen() {
        const winners:Vector.<PlayerInfoDTO> = new <PlayerInfoDTO>[DtoMock.playerInfo1];
        const losers:Vector.<PlayerInfoDTO> = DtoMock.playerInfosLandscape();

        gameOverScreen = new GameOverScreen(
                PlayerInfo.fromDtoVector(winners),
                PlayerInfo.fromDtoVector(losers),
                true,
                2,
                new LayoutPortrait(stage.stageWidth, stage.stageHeight, 1),
                new CastlesLocale(""),
                new LoadImageManagerMock(1000)
        );
        addChild(gameOverScreen);
    }
}
}
