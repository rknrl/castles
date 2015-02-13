//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.events {
import flash.events.Event;

public class AddBalanceEvent extends Event {
    public static const ADD_BALANCE:String = "addBalance";

    private var _amount:int;

    public function get amount():int {
        return _amount;
    }

    public function AddBalanceEvent(amount:int) {
        super(ADD_BALANCE, true);
        _amount = amount;
    }
}
}
