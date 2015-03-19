//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package {
import flash.display.Sprite;
import flash.events.Event;
import flash.events.MouseEvent;
import flash.utils.getTimer;

import ru.rknrl.castles.view.utils.dust.Dust;

public class DustDemo extends Sprite {
    private var dusts:Vector.<Dust> = new <Dust>[];

    public function DustDemo() {
        stage.addEventListener(MouseEvent.CLICK, onClick);
        addEventListener(Event.ENTER_FRAME, onEnterFrame);
    }

    private var lastTime:int;

    private function onEnterFrame(event:Event):void {
        const time:int = getTimer();
        const deltaTime:int = time - lastTime;
        lastTime = time;
        for each(var dust:Dust in dusts) dust.enterFrame(deltaTime);
    }

    private function onClick(event:MouseEvent):void {
        const dust:Dust = new Dust(getTimer());
        dust.x = mouseX;
        dust.y = mouseY;
        addChild(dust);
        dusts.push(dust);
    }
}
}

