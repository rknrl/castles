//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles.view {
import flash.events.Event;

public class LoginEvent extends Event {
    public static const LOGIN:String = "login";

    private var _socialName:String;

    public function get socialName():String {
        return _socialName;
    }

    public function LoginEvent(socialName:String) {
        super(LOGIN);
        _socialName = socialName;
    }
}
}
