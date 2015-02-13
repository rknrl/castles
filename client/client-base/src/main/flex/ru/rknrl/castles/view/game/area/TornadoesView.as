//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view.game.area {
import ru.rknrl.castles.model.points.Point;

public class TornadoesView extends MovableView {
    public function TornadoesView():void {
        super("tornado");
    }

    public function addTornado(id:int, pos:Point):void {
        add(id, pos, new TornadoMC());
    }
}
}
