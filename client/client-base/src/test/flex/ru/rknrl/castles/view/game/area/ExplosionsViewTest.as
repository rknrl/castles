//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.display.Sprite;
import flash.events.Event;

import org.flexunit.asserts.fail;
import org.flexunit.async.Async;

import ru.rknrl.castles.model.events.GameViewEvents;

public class ExplosionsViewTest {
    [Test(async, "shake event dispatch")]
    public function t2():void {
        const view:ExplosionsView = new ExplosionsView();
        view.addEventListener(GameViewEvents.SHAKE, Async.asyncHandler(this, onShake, 100, null, onTimeout), false, 0, true);
        view.addChild(new Sprite());

        function onShake(event:Event, passThroughData:Object):void {
            // ok
        }

        function onTimeout(passThroughData:Object):void {
            fail("timeout");
        }
    }
}
}
