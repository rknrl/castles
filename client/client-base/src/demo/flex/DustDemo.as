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
import flash.utils.getTimer;

import ru.rknrl.castles.view.utils.dust.Dust;

public class DustDemo extends Sprite {
    public function DustDemo() {
        stage.addEventListener(MouseEvent.CLICK, onClick);
    }

    private function onClick(event:MouseEvent):void {
        const dust:Dust = new Dust(getTimer());
        dust.x = mouseX;
        dust.y = mouseY;
        addChild(dust);
    }
}
}

