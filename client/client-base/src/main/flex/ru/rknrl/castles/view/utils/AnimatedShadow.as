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
