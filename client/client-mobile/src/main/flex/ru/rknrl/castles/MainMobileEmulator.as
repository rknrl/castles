//       ___       ___       ___       ___       ___
//      /\  \     /\__\     /\__\     /\  \     /\__\
//     /::\  \   /:/ _/_   /:| _|_   /::\  \   /:/  /
//    /::\:\__\ /::-"\__\ /::|/\__\ /::\:\__\ /:/__/
//    \;:::/  / \;:;-",-" \/|::/  / \;:::/  / \:\  \
//     |:\/__/   |:|  |     |:/  /   |:\/__/   \:\__\
//      \|__|     \|__|     \/__/     \|__|     \/__/

package ru.rknrl.castles {
[SWF(frameRate="60", quality="high")]
public class MainMobileEmulator extends MainMobileBase {
    public function MainMobileEmulator() {
        super("castles.rknrl.ru", 2335, 2336, 80);
//        super("127.0.0.1", 2335, 2336, 8080);
    }
}
}
