//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller {
import ru.rknrl.castles.controller.game.TutorControllerBase;
import ru.rknrl.castles.model.points.Point;
import ru.rknrl.castles.view.menu.MenuTutorView;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.dto.SlotId;

public class MenuTutorController extends TutorControllerBase {
    private var view:MenuTutorView;

    public function MenuTutorController(view:MenuTutorView) {
        this.view = view;
        super(view);
    }

    public function playNavigate():ITutorCommand {
        if (view.touchable) {
            return swipe();
        } else {
            return tweenAndClick(view.middleNavigationPointPos);
        }
    }

    private function swipe():ITutorCommand {
        return sequence(new <ITutorCommand>[
            wait(500),
            exec(view.addArrow),
            showCursor,
            cursorPos(view.swipeStartPos),
            wait(500),
            view.tween(view.swipeStartPos, view.swipeEndPos),
            wait(1000),
            hideCursor,
            clear
        ]);
    }

    public function playSlot(slotId:SlotId):ITutorCommand {
        return tweenAndClick(view.slotPos(slotId));
    }

    public function playMagicItem():ITutorCommand {
        return tweenAndClick(view.firstMagicItemPos);
    }

    public function playFlask():ITutorCommand {
        return tweenAndClick(view.firstFlaskPos);
    }

    private function tweenAndClick(clickPos:Point):ITutorCommand {
        return sequence(new <ITutorCommand>[
            wait(500),
            showCursor,
            view.tween(view.screenCorner, clickPos),
            wait(100),
            click,
            wait(1000),
            hideCursor
        ]);
    }
}
}
