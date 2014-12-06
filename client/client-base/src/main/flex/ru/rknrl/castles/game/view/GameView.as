package ru.rknrl.castles.game.view {
import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.CapsStyle;
import flash.display.Graphics;
import flash.display.LineScaleMode;
import flash.display.Shape;
import flash.display.Sprite;
import flash.geom.Point;
import flash.utils.Dictionary;

import ru.rknrl.DashLine;
import ru.rknrl.Points;
import ru.rknrl.castles.game.BuildingOwner;
import ru.rknrl.castles.game.view.items.BulletView;
import ru.rknrl.castles.game.view.items.FireballView;
import ru.rknrl.castles.game.view.items.TornadoView;
import ru.rknrl.castles.game.view.items.VolcanoView;
import ru.rknrl.castles.utils.Colors;

public class GameView extends Sprite {
    private var groundLayer:Sprite;
    private var volcanoLayer:Sprite;
    private var buildingsLayer:Sprite;
    private var unitsLayer:Sprite;
    private var bulletsLayer:Sprite;
    private var effectsLayer:Sprite;
    private var tornadoPathLayer:Shape;
    private var arrowLayer:Sprite;

    public function GameView(h:int, v:int) {
        addChild(groundLayer = new Sprite());
        addChild(volcanoLayer = new Sprite());
        addChild(buildingsLayer = new Sprite());
        addChild(unitsLayer = new Sprite());
        addChild(bulletsLayer = new Sprite());
        addChild(effectsLayer = new Sprite());
        addChild(tornadoPathLayer = new Shape());
        addChild(arrowLayer = new Sprite());

        addGrounds(h, v);
    }

    public function addFireball(fireball:FireballView):void {
        effectsLayer.addChild(fireball);
    }

    public function removeFireball(fireball:FireballView):void {
        effectsLayer.removeChild(fireball);
    }

    public function addVolcano(volcano:VolcanoView):void {
        volcanoLayer.addChild(volcano);
    }

    public function removeVolcano(volcano:VolcanoView):void {
        volcanoLayer.removeChild(volcano);
    }

    public function addTornado(tornado:TornadoView):void {
        effectsLayer.addChild(tornado);
    }

    public function removeTornado(tornado:TornadoView):void {
        effectsLayer.removeChild(tornado);
    }

    public function addBuilding(building:Building):void {
        buildingsLayer.addChild(building);
    }

    public function addUnit(unit:Unit):void {
        unitsLayer.addChild(unit);
    }

    public function removeUnit(unit:Unit):void {
        unitsLayer.removeChild(unit);
    }

    public function addBullet(bullet:BulletView):void {
        bulletsLayer.addChild(bullet);
    }

    public function removeBullet(bullet:BulletView):void {
        bulletsLayer.removeChild(bullet);
    }

    public function addArrow(arrow:Arrow):void {
        arrowLayer.addChild(arrow);
    }

    public function removeArrow(arrow:Arrow):void {
        arrowLayer.removeChild(arrow);
    }

    public function drawTornadoPoints(tornadoPoints:Vector.<Point>):void {
        const g:Graphics = tornadoPathLayer.graphics;
        g.clear();
        g.lineStyle(5, 0xffffff, 1, false, LineScaleMode.NORMAL, CapsStyle.NONE);

        if (tornadoPoints.length >= 2) {
            const points:Points = new Points(tornadoPoints);
            DashLine.drawPath(g, points);
        }
    }

    public function clearTornadoPoints():void {
        tornadoPathLayer.graphics.clear();
    }

    private const grounds:Dictionary = new Dictionary();

    private function getGround(i:int, j:int):Bitmap {
        return grounds[i + "_" + j];
    }

    private function addGrounds(h:int, v:int):void {
        const gap:int = 2;

        for (var i:int = 0; i < h; i++) {
            for (var j:int = 0; j < v; j++) {
                const bitmap:Bitmap = new Bitmap(Colors.groundColor);
                bitmap.width = GameConstants.cellSize - gap;
                bitmap.height = GameConstants.cellSize - gap;
                bitmap.x = i * GameConstants.cellSize + gap / 2;
                bitmap.y = j * GameConstants.cellSize + gap / 2;
                grounds[i + "_" + j] = bitmap;
                groundLayer.addChild(bitmap);
            }
        }
    }

    // todo
    public function updateGroundColor(i:int, j:int, owner:BuildingOwner):void {
        const color:BitmapData = owner.hasOwner ? Colors.groundColors[owner.ownerId] : Colors.noOwnerGroundColor;
//        getGround(i, j).bitmapData = color;
    }
}
}
