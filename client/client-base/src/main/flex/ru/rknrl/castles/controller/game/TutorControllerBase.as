//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.controller.game {
import flash.events.EventDispatcher;

import ru.rknrl.castles.view.utils.tutor.commands.Exec;
import ru.rknrl.castles.view.utils.tutor.commands.ITutorCommand;
import ru.rknrl.castles.view.utils.tutor.commands.InfinityWait;
import ru.rknrl.castles.view.utils.tutor.commands.TutorParallelCommands;
import ru.rknrl.castles.view.utils.tutor.commands.TutorSequenceCommands;
import ru.rknrl.castles.view.utils.tutor.commands.Wait;

public class TutorControllerBase extends EventDispatcher {
    protected static function exec(func:Function):ITutorCommand {
        return new Exec(func);
    }

    protected static function parallel(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorParallelCommands(commands);
    }

    protected static function sequence(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, false);
    }

    protected static function loop(commands:Vector.<ITutorCommand>):ITutorCommand {
        return new TutorSequenceCommands(commands, true);
    }

    protected static function wait(duration:int):ITutorCommand {
        return new Wait(duration);
    }

    protected static function get infinityWait():ITutorCommand {
        return new InfinityWait();
    }
}
}
