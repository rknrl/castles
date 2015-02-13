//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.model {
import ru.rknrl.LocalStorage;
import ru.rknrl.castles.model.tutor.GameTutorState;
import ru.rknrl.castles.model.tutor.MenuTutorState;
import ru.rknrl.log.Log;

public class CastlesLocalStorage {
    private static const APP_RUN_COUNT:String = "appRunCount";
    private static const MENU_TUTOR_STATE:String = "menuTutorState";
    private static const GAME_TUTOR_STATE:String = "gameTutorState";

    private var storage:LocalStorage;

    public function CastlesLocalStorage(log:Log) {
        storage = new LocalStorage(log);
    }

    public function get incAndGetAppRunCount():int {
        if (storage.isAvailable) {
            const count:int = storage.data[APP_RUN_COUNT];
            storage.data[APP_RUN_COUNT] = count + 1;
            storage.flush();
            return count + 1;
        }
        return 0;
    }

    public function get menuTutorState():MenuTutorState {
        const data:Object = storage.isAvailable ? storage.data[MENU_TUTOR_STATE] : null;
        return data ? MenuTutorState.parse(data) : new MenuTutorState();
    }

    public function saveMenuTutorState(tutorState:MenuTutorState):void {
        if (storage.isAvailable) {
            storage.data[MENU_TUTOR_STATE] = MenuTutorState.write(tutorState);
            storage.flush();
        }
    }

    public function get gameTutorState():GameTutorState {
        const data:Object = storage.isAvailable ? storage.data[GAME_TUTOR_STATE] : null;
        return data ? GameTutorState.parse(data) : new GameTutorState();
    }

    public function saveGameTutorState(tutorState:GameTutorState):void {
        if (storage.isAvailable) {
            storage.data[GAME_TUTOR_STATE] = GameTutorState.write(tutorState);
            storage.flush();
        }
    }
}
}
