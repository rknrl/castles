//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package {
import flash.display.Sprite;

import ru.rknrl.castles.view.game.ui.magicItems.GameMagicItem;
import protos.ItemType;

public class TestGameMagicItem extends Sprite {
    public function TestGameMagicItem() {
        const item1:GameMagicItem = new GameMagicItem(ItemType.FIREBALL, 2);
        item1.x = 50;
        item1.y = 50;
        item1.cooldownProgress = 0;
        addChild(item1);

        const item2:GameMagicItem = new GameMagicItem(ItemType.STRENGTHENING, 2);
        item2.x = 100;
        item2.y = 50;
        item2.cooldownProgress = 0.3;
        addChild(item2);

        const item3:GameMagicItem = new GameMagicItem(ItemType.TORNADO, 2);
        item3.x = 150;
        item3.y = 50;
        item3.cooldownProgress = 0.5;
        addChild(item3);

        const item4:GameMagicItem = new GameMagicItem(ItemType.ASSISTANCE, 2);
        item4.x = 200;
        item4.y = 50;
        item4.cooldownProgress = 0.8;
        addChild(item4);

        const item5:GameMagicItem = new GameMagicItem(ItemType.VOLCANO, 2);
        item5.x = 250;
        item5.y = 50;
        item5.cooldownProgress = 1;
        addChild(item5);
    }
}
}
