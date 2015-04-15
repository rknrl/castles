//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import flash.events.Event;

import org.flexunit.asserts.fail;
import org.flexunit.async.Async;

import ru.rknrl.castles.model.events.GameViewEvents;
import ru.rknrl.core.kit.plusMinus;

public class VolcanoViewTest {
    [Test("scale changed")]
    public function t1():void {
        // originalSize = 64
        const view:VolcanoView = new VolcanoView();
        view.radius = 20;
        plusMinus(0.31, view.scaleX, 0.01);
        plusMinus(0.31, view.scaleY, 0.01);
        view.radius = 30;
        plusMinus(0.468, view.scaleX, 0.01);
        plusMinus(0.468, view.scaleY, 0.01);
    }

    [Test(async, "shake event dispatch")]
    public function t2():void {
        const view:VolcanoView = new VolcanoView();
        view.addEventListener(GameViewEvents.SHAKE, Async.asyncHandler(this, onShake, 100, null, onTimeout), false, 0, true);
        view.radius = 20;

        function onShake(event:Event, passThroughData:Object):void {
            // ok
        }

        function onTimeout(passThroughData:Object):void {
            fail("timeout");
        }
    }

    [Test(async, "shake event dispatch")]
    public function t3():void {
        const view:VolcanoView = new VolcanoView();
        view.radius = 20;
        view.addEventListener(GameViewEvents.SHAKE, Async.asyncHandler(this, onShake, 100, null, onTimeout), false, 0, true);
        view.radius = 20;

        function onShake(event:Event, passThroughData:Object):void {
            fail("don't shake");
        }

        function onTimeout(passThroughData:Object):void {
        }
    }
}
}
