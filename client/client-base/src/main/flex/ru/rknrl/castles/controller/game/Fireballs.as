package ru.rknrl.castles.controller.game {
import flash.utils.getTimer;

import ru.rknrl.castles.model.game.Explosion;
import ru.rknrl.castles.model.game.Fireball;
import ru.rknrl.castles.utils.points.Point;
import ru.rknrl.castles.view.game.area.explosions.ExplosionsView;
import ru.rknrl.castles.view.game.area.fireballs.FireballsView;
import ru.rknrl.dto.FireballDTO;

public class Fireballs {
    private var view:FireballsView;
    private var explosionsView:ExplosionsView;
    private var areaWidth:int;
    private var areaHeight:int;

    public function Fireballs(view:FireballsView, explosionsView:ExplosionsView, areaWidth:int, areaHeight:int) {
        this.view = view;
        this.explosionsView = explosionsView;
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
    }

    private const fireballs:Vector.<Fireball> = new <Fireball>[];
    private var fireballIterator:int;

    public function add(dto:FireballDTO):void {
        const time:int = getTimer();

        const fromLeft:Boolean = dto.x > areaWidth / 2;
        const fromTop:Boolean = dto.y > areaHeight / 2;
        const dx:Number = fromLeft ? dto.x : areaWidth - dto.x;
        const dy:Number = fromTop ? dto.y : areaHeight - dto.y;
        const d:Number = Math.max(dx, dy);

        const startPos:Point = new Point(fromLeft ? dto.x - d : dto.x + d, fromTop ? dto.y - d : dto.y + d);
        const endPos:Point = new Point(dto.x, dto.y);

        const fireball:Fireball = new Fireball(fireballIterator++, startPos, endPos, time, dto.millisTillSplash);
        fireballs.push(fireball);
        view.addFireball(fireball.id, fireball.pos(time))
    }

    public function update(time:int):void {
        const fireballsToRemove:Vector.<Fireball> = new <Fireball>[];
        for each(var fireball:Fireball in fireballs) {
            view.setFireballPos(fireball.id, fireball.pos(time));
            if (fireball.needRemove(time)) fireballsToRemove.push(fireball);
        }

        for each(fireball in fireballsToRemove) {
            const index:int = fireballs.indexOf(fireball);
            fireballs.splice(index, 1);
            view.removeFireball(fireball.id);
            addExplosion(fireball.pos(time), time);
        }

        const explosionsToRemove:Vector.<Explosion> = new <Explosion>[];
        for each(var explosion:Explosion in explosions) {
            if (explosion.needRemove(time)) explosionsToRemove.push(explosion);
        }

        for each(explosion in explosionsToRemove) {
            explosions.splice(explosions.indexOf(explosion), 1);
            explosionsView.removeExplosion(explosion.id);
        }
    }

    private const explosions:Vector.<Explosion> = new <Explosion>[];
    private var explosionIterator:int;

    private function addExplosion(point:Point, time:int):void {
        const explosion:Explosion = new Explosion(explosionIterator++, time);
        explosions.push(explosion);
        explosionsView.addExplosion(explosion.id, point);
    }
}
}
