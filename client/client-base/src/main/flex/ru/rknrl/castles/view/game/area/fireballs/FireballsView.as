package ru.rknrl.castles.view.game.area.fireballs {
import flash.display.Sprite;
import flash.utils.Dictionary;

import ru.rknrl.castles.utils.points.Point;

public class FireballsView extends Sprite {
    private const fireballs:Dictionary = new Dictionary();

    private function byId(id:int):FireballMC {
        const fireball:FireballMC = fireballs[id];
        if (!fireball) throw new Error("can't find fireball " + id);
        return fireball;
    }

    public function addFireball(id:int, pos:Point):void {
        const fireball:FireballMC = new FireballMC();
        if (fireballs[id]) throw new Error("fireball " + id + " already exists");
        fireballs[id] = fireball;
        addChild(fireball);
        setFireballPos(id, pos);
    }

    public function setFireballPos(id:int, pos:Point):void {
        byId(id).x = pos.x;
        byId(id).y = pos.y;
    }

    public function removeFireball(id:int):void {
        removeChild(byId(id));
        delete fireballs[id];
    }
}
}
