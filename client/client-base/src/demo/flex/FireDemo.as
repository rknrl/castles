//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package {
import flash.display.DisplayObject;
import flash.display.Sprite;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.Fla;
import ru.rknrl.castles.view.utils.dust.FireDust;
import ru.rknrl.dto.BuildingLevel;
import ru.rknrl.dto.BuildingType;

public class FireDemo extends Sprite {
    private var dust:FireDust;

    public function FireDemo() {
        dust = new FireDust();
        dust.x = 300;
        dust.y = 300 - 32;
        addChild(dust);

        const tower:DisplayObject = Fla.createBuilding(BuildingType.TOWER, BuildingLevel.LEVEL_3);
        tower.x = 300;
        tower.y = 300;
        tower.transform.colorTransform = Colors.transform(Colors.yellow);
        addChild(tower);
    }
}
}

