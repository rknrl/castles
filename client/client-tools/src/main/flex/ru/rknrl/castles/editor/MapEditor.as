//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.editor {
import flash.display.Sprite;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.filesystem.File;
import flash.ui.Keyboard;

[SWF(width=700, height=700)]
public class MapEditor extends Sprite {
    private var ground:EditorGround;

    public function MapEditor() {
        const h:int = 15;
        const v:int = 15;
        ground = new EditorGround(h, v);
        addChild(ground);

        ground.slot(0, 0);
        ground.slot(2, 0);
        ground.slot(4, 0);
        ground.slot(1, 1);
        ground.slot(3, 1);

        stage.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
        addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
    }

    private function onMouseDown(event:MouseEvent):void {
        ground.click(mouseX, mouseY);
    }

    private function onKeyDown(event:KeyboardEvent):void {
        switch (event.keyCode) {
            case Keyboard.S:
                const file:File = new File();
                file.save(ground.getMap(), "map");
                break;
        }
    }
}
}
