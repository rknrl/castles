package ru.rknrl.castles.menu.screens {
import ru.rknrl.castles.menu.screens.Screen;

public class MenuScreen extends Screen {
    private var _id:String;

    public function get id():String {
        return _id;
    }

    public function MenuScreen(id:String) {
        _id = id;
    }
}
}
