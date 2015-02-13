//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.utils {
import flash.display.Sprite;

public class AnimatedShadow extends Sprite {
    private var holder:Animated;

    public function AnimatedShadow() {
        addChild(holder = new Animated());
        holder.addChild(new Shadow());
    }

    public function bounce():void {
        holder.bounce();
    }
}
}
