//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package {
import flash.display.Sprite;
import flash.events.MouseEvent;

import ru.rknrl.castles.view.Colors;
import ru.rknrl.castles.view.game.area.units.UnitKill;

[SWF(frameRate=60)]
public class UnitsKillDemo extends Sprite {
    public function UnitsKillDemo() {
        stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
    }

    private function onMouseDown(event:MouseEvent):void {
        const unitKill:UnitKill = new UnitKill(Colors.yellow);
        unitKill.x = mouseX;
        unitKill.y = mouseY;
        addChild(unitKill);
    }
}
}
